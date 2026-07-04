//
// LIBRERÍAS REQUERIDAS (Gestor de Bibliotecas):
//   - "PubSubClient" de Nick O'Leary
//
// ============================================================
//  TOPICS QUE PUBLICA EL ESP32 (ESP32 -> App)
// ============================================================
// [CÓDIGO ORIGINAL COMENTADO]: 
//   basura-llena-1   → "1" cuando bote 1 lleva 10s lleno
//   basura-llena-2   → "1" cuando bote 2 lleva 10s lleno
//   basura-llena-3   → "1" cuando bote 3 lleva 10s lleno
//
//  [MODIFICACIÓN CITYGRID NUEVA]: En lugar de publicar "1" o "0" cuando el bote
//  está lleno, ahora publicamos la distancia exacta medida por el sensor en cm.
//  ¿Por qué?: Esto permite que la app Android calcule y muestre el porcentaje
//  exacto (ej. 90%, 62%, 45%) en la UI de "Residuos" dinámicamente, en vez
//  de solo tener un estado binario de Lleno/Vacío.
//
//   basura-llena-1   → distancia en cm del bote 1
//   basura-llena-2   → distancia en cm del bote 2
//   basura-llena-3   → distancia en cm del bote 3
//   luz-encendida    → "1" cuando detecta 20s de oscuridad y enciende luces | "0" apaga
//   bomba-activa     → "1" cuando activa la bomba de riego | "0" apaga
//
// ============================================================
//  TOPICS QUE RECIBE EL ESP32 (App -> ESP32)
// ============================================================
//   control-luces    → "1" enciende LEDs exteriores + relevador | "0" apaga
//   control-bomba    → "1" activa bomba de agua                 | "0" apaga

#include <WiFi.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>

// ============================================================
//  CREDENCIALES WiFi
// ============================================================
const char* ssid     = "MEGACABLE-CE9D";
const char* password = "uEVBbz8c";

// ============================================================
//  CREDENCIALES MQTT (HiveMQ Cloud)
// ============================================================
const char* mqtt_server   = "7616ccef7e334086bf74b6bb92340be3.s1.eu.hivemq.cloud";
const int   mqtt_port     = 8883;
const char* mqtt_username = "CityGrid";
const char* mqtt_password = "CityGridPasswordSec1";

// ============================================================
//  TOPICS — MODIFICA AQUÍ SI NECESITAS CAMBIARLOS
// ============================================================
// Topics que el ESP32 PUBLICA hacia la app:
const char* TOPIC_RESIDUOS   = "citygrid/residuos";
const char* TOPIC_LUCES      = "citygrid/alumbrado";
const char* TOPIC_BOMBA      = "citygrid/agua";

// Topics que el ESP32 RECIBE desde la app:
const char* TOPIC_CTRL_LUCES = "control-luces";
const char* TOPIC_CTRL_BOMBA = "control-bomba";

// ============================================================
//  DISTANCIAS Y TIEMPOS — MODIFICA AQUÍ SI ES NECESARIO
// ============================================================
const int  ALTURA_CONTENEDOR_CM = 20;    // Altura total del bote de basura en cm (usada para calcular el %)
const int  DISTANCIA_BASURA_CM  = 5;    // Considera "lleno" si objeto a <= 5 cm para el LED local
const int  DISTANCIA_AGUA_CM    = 6;    // Activa bomba si agua a <= 6 cm del sensor
const long TIEMPO_BASURA_MS     = 10000; // 10 segundos seguidos para confirmar basura llena
const long TIEMPO_OSCURIDAD_MS  = 20000; // 20 segundos de oscuridad para encender luces
const int  UMBRAL_LUZ_LDR      = 1500;  // Valor analógico por debajo = oscuridad (ajusta según LDR)

// ============================================================
//  PINES GPIO
// ============================================================
// Sensores ultrasónicos botes de basura
const int TRIG_BASURA1 = 4;
const int ECHO_BASURA1 = 5;
const int TRIG_BASURA2 = 18;
const int ECHO_BASURA2 = 19;
const int TRIG_BASURA3 = 21;
const int ECHO_BASURA3 = 22;

// Sensor ultrasónico depósito de agua
const int TRIG_AGUA = 25;
const int ECHO_AGUA = 26;

// LEDs indicadores de botes llenos
const int LED_BASURA1 = 13;
const int LED_BASURA2 = 14;
const int LED_BASURA3 = 27;

// LDR (fotoresistencia) — pin analógico
const int PIN_LDR = 34;

// Bomba de agua (controlada por MOSFET/transistor)
const int PIN_BOMBA = 32;

// LEDs exteriores (controlados por MOSFET)
const int PIN_LEDS_EXT = 33;

// Relevador para foco de 120V
const int PIN_RELEVADOR = 23;

// Configuración del tipo de relevador
// Cambiar a 'true' si tu relevador es Active-Low (se activa con LOW, se apaga con HIGH)
// Cambiar a 'false' si tu relevador es Active-High (se activa con HIGH, se apaga con LOW)
const bool RELEVADOR_ACTIVE_LOW = true;

// ============================================================
//  CONTROL DE RELEVADOR POR ALTA IMPEDANCIA (ESP32 3.3V -> Relevador 5V)
// ============================================================
void setRelevador(bool encender) {
  if (encender) {
    // Para encender: lo ponemos como salida y en LOW (si es active-low)
    pinMode(PIN_RELEVADOR, OUTPUT);
    digitalWrite(PIN_RELEVADOR, RELEVADOR_ACTIVE_LOW ? LOW : HIGH);
  } else {
    // Para apagar: lo configuramos como INPUT (Alta Impedancia / flotante).
    // Esto desconecta eléctricamente el pin y evita que circule corriente, 
    // lo cual apaga con total seguridad el optoacoplador del relevador de 5V.
    pinMode(PIN_RELEVADOR, INPUT);
  }
}

// ============================================================
//  CERTIFICADO CA RAÍZ (HiveMQ / Let's Encrypt)
// ============================================================
static const char* root_ca PROGMEM = R"EOF(
-----BEGIN CERTIFICATE-----
MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw
TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh
cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4
WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu
ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY
MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc
h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+
0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U
A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW
T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH
B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC
B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv
KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn
OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn
jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw
qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI
rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV
HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq
hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL
ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ
3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK
NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5
ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur
TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC
jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc
oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq
4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA
mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d
emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=
-----END CERTIFICATE-----
)EOF";

// ============================================================
//  OBJETOS WiFi y MQTT
// ============================================================
WiFiClientSecure espClient;
PubSubClient client(espClient);

// ============================================================
//  VARIABLES DE ESTADO INTERNAS
// ============================================================
unsigned long tiempoBasura1 = 0;
unsigned long tiempoBasura2 = 0;
unsigned long tiempoBasura3 = 0;

bool bote1Lleno = false;
bool bote2Lleno = false;
bool bote3Lleno = false;

unsigned long tiempoOscuridad = 0;
bool lucesEncendidas = false;

// Variables para control manual temporal (App -> ESP32)
bool modoManualLuz = false;
unsigned long tiempoUltimaOrdenLuz = 0;
bool modoManualAgua = false;
unsigned long tiempoUltimaOrdenAgua = 0;
const unsigned long TIEMPO_OVERRIDE_MANUAL_MS = 60000; // 1 minuto de control manual antes de volver a AUTO

// Variables para publicar periodicamente la distancia (cada 5s)
unsigned long tiempoUltimaPublicacionBasura = 0;
const long INTERVALO_PUBLICACION_BASURA = 5000;

// Historial de porcentajes para evitar publicar ante variaciones insignificantes
int ultimoPct1 = -1;
int ultimoPct2 = -1;
int ultimoPct3 = -1;
const int UMBRAL_CAMBIO_PCT = 2; // Publica si cambia al menos un 2% para ignorar ruido del HC-SR04
unsigned long tiempoUltimoReporteForzado = 0;
const long INTERVALO_REPORTE_FORZADO = 60000; // Si no hay cambios, publica al menos cada 60s

// Variables de no-bloqueo para loop y reconexión
unsigned long ultimoIntentoReconexion = 0;
unsigned long tiempoUltimaLecturaSensores = 0;
const long INTERVALO_LECTURA_SENSORES = 1000; // Leer sensores cada 1 segundo (no bloqueante)

// ============================================================
//  FUNCIÓN: Medir distancia con HC-SR04
// ============================================================
long medirDistancia(int pinTrig, int pinEcho) {
  digitalWrite(pinTrig, LOW);
  delayMicroseconds(2);
  digitalWrite(pinTrig, HIGH);
  delayMicroseconds(10);
  digitalWrite(pinTrig, LOW);

  long duracion = pulseIn(pinEcho, HIGH, 30000);
  long distancia = duracion * 0.034 / 2;
  
  // Filtro de ruido y error de lectura (distancias imposibles o timeout de pulseIn)
  if (distancia <= 0 || distancia > 100) {
    return -1; // -1 indica lectura inválida / fuera de rango
  }
  return distancia;
}

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Conectando a WiFi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi conectado. IP: ");
  Serial.println(WiFi.localIP());

  // Sincronizar hora para validación de certificados SSL de HiveMQ
  configTime(-6 * 3600, 0, "pool.ntp.org", "time.nist.gov"); // GMT-6
  Serial.print("Sincronizando hora NTP...");
  time_t now = time(nullptr);
  int intentos = 0;
  while (now < 86400 && intentos < 20) {
    delay(500);
    Serial.print(".");
    now = time(nullptr);
    intentos++;
  }
  Serial.println("\nHora sincronizada.");
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.println("\n--- MQTT CALLBACK ---");
  Serial.print("Mensaje recibido en topic: ");
  Serial.println(topic);

  // Copiar el payload a un buffer seguro terminando en null para imprimir y comparar
  char message[length + 1];
  memcpy(message, payload, length);
  message[length] = '\0';

  Serial.print("Payload (");
  Serial.print(length);
  Serial.print(" bytes): ");
  Serial.println(message);

  if (length == 0) {
    Serial.println("Advertencia: Payload vacío recibido.");
    return;
  }

  // Parsear valor numérico básico del primer caracter (ej. '1' -> 1, '0' -> 0)
  int valor = message[0] - '0';

  if (strcmp(topic, TOPIC_CTRL_LUCES) == 0) {
    if (valor == 1) {
      modoManualLuz = true;
      digitalWrite(PIN_LEDS_EXT, HIGH);   
      setRelevador(true);  
      lucesEncendidas = true;
      Serial.println(">> App: Comando Luces -> ENCENDER (MANUAL INDEFINIDO)");
    } else if (valor == 0) {
      modoManualLuz = true;
      digitalWrite(PIN_LEDS_EXT, LOW);    
      setRelevador(false);   
      lucesEncendidas = false;            
      tiempoOscuridad = 0;
      Serial.println(">> App: Comando Luces -> APAGAR (MANUAL INDEFINIDO)");
    } else if (valor == 2) {
      modoManualLuz = false;
      Serial.println(">> App: Comando Luces -> Regresar a AUTOMÁTICO");
    } else {
      Serial.print(">> App: Comando Luces -> Valor no reconocido: ");
      Serial.println(valor);
    }
  }

  if (strcmp(topic, TOPIC_CTRL_BOMBA) == 0) {
    modoManualAgua = true;
    tiempoUltimaOrdenAgua = millis();
    if (valor == 1) {
      digitalWrite(PIN_BOMBA, HIGH);  
      Serial.println(">> App: Comando Bomba -> ENCENDER (MANUAL)");
    } else if (valor == 0) {
      digitalWrite(PIN_BOMBA, LOW);   
      Serial.println(">> App: Comando Bomba -> APAGAR (MANUAL)");
    } else {
      Serial.print(">> App: Comando Bomba -> Valor no reconocido: ");
      Serial.println(valor);
    }
  }
  Serial.println("---------------------\n");
}

void reconnectNonBlocking() {
  if (client.connected()) return;

  unsigned long ahora = millis();
  if (ahora - ultimoIntentoReconexion >= 5000) {
    ultimoIntentoReconexion = ahora;
    Serial.print("Intentando reconexión a MQTT HiveMQ...");
    
    // Conectar usando un Client ID único combinando la MAC o un número aleatorio
    String clientId = "ESP32Client_CityGrid_" + String(random(1000, 9999));
    if (client.connect(clientId.c_str(), mqtt_username, mqtt_password)) {
      Serial.println(" ¡CONECTADO!");
      
      // Suscribirse a los tópicos de control de actuadores
      bool subLuces = client.subscribe(TOPIC_CTRL_LUCES);
      bool subBomba = client.subscribe(TOPIC_CTRL_BOMBA);
      
      Serial.print("Suscripción '");
      Serial.print(TOPIC_CTRL_LUCES);
      Serial.println(subLuces ? "': EXITOSA" : "': FALLIDA");
      
      Serial.print("Suscripción '");
      Serial.print(TOPIC_CTRL_BOMBA);
      Serial.println(subBomba ? "': EXITOSA" : "': FALLIDA");
    } else {
      Serial.print(" FALLÓ, rc=");
      Serial.print(client.state());
      Serial.println(" — Se reintentará en 5s sin bloquear.");
    }
  }
}

void setup() {
  Serial.begin(9600);

  // Semilla para generación de ID de cliente aleatorio
  randomSeed(analogRead(35));

  pinMode(TRIG_BASURA1, OUTPUT); pinMode(ECHO_BASURA1, INPUT);
  pinMode(TRIG_BASURA2, OUTPUT); pinMode(ECHO_BASURA2, INPUT);
  pinMode(TRIG_BASURA3, OUTPUT); pinMode(ECHO_BASURA3, INPUT);
  pinMode(TRIG_AGUA,    OUTPUT); pinMode(ECHO_AGUA,    INPUT);

  pinMode(LED_BASURA1,  OUTPUT);
  pinMode(LED_BASURA2,  OUTPUT);
  pinMode(LED_BASURA3,  OUTPUT);
  pinMode(PIN_BOMBA,    OUTPUT);
  pinMode(PIN_LEDS_EXT, OUTPUT);
  // NOTA: No definimos pinMode(PIN_RELEVADOR, OUTPUT) aquí porque setRelevador lo gestiona dinámicamente.

  digitalWrite(LED_BASURA1,  LOW);
  digitalWrite(LED_BASURA2,  LOW);
  digitalWrite(LED_BASURA3,  LOW);
  digitalWrite(PIN_BOMBA,    LOW);
  digitalWrite(PIN_LEDS_EXT, LOW);
  setRelevador(false); // Estado inicial apagado mediante alta impedancia

  setup_wifi();

  espClient.setCACert(root_ca);
  // NOTA: Si tienes problemas de conexión SSL, puedes comentar la línea de arriba 
  // y descomentar la siguiente para deshabilitar la verificación estricta del certificado:
  // espClient.setInsecure();

  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
  client.setBufferSize(1024); // Ampliar buffer de PubSubClient para TLS y JSONs
}

void loop() {
  // Manejo de la reconexión MQTT no bloqueante
  if (!client.connected()) {
    reconnectNonBlocking();
  } else {
    client.loop();
  }

  unsigned long ahora = millis();

  // Controlar la lectura de sensores y lógica asociada mediante temporizador no bloqueante (cada 1s)
  if (ahora - tiempoUltimaLecturaSensores >= INTERVALO_LECTURA_SENSORES) {
    tiempoUltimaLecturaSensores = ahora;

    // ----------------------------------------------------------
    // LECTURA DE SENSORES
    // ----------------------------------------------------------
    long distB1 = medirDistancia(TRIG_BASURA1, ECHO_BASURA1);
    long distB2 = medirDistancia(TRIG_BASURA2, ECHO_BASURA2);
    long distB3 = medirDistancia(TRIG_BASURA3, ECHO_BASURA3);

    // ----------------------------------------------------------
    // LOGICA LOCAL (LEDs indicadores si están llenos)
    // ----------------------------------------------------------
    // Bote 1
    if (distB1 > 0 && distB1 <= DISTANCIA_BASURA_CM) {
      if (tiempoBasura1 == 0) tiempoBasura1 = ahora;
      if (!bote1Lleno && (ahora - tiempoBasura1 >= TIEMPO_BASURA_MS)) {
        bote1Lleno = true;
        digitalWrite(LED_BASURA1, HIGH);
        Serial.println("Bote 1 LLENO — LED encendido");
      }
    } else {
      tiempoBasura1 = 0;
      if (bote1Lleno) { 
        bote1Lleno = false; 
        digitalWrite(LED_BASURA1, LOW); 
      }
    }

    // Bote 2
    if (distB2 > 0 && distB2 <= DISTANCIA_BASURA_CM) {
      if (tiempoBasura2 == 0) tiempoBasura2 = ahora;
      if (!bote2Lleno && (ahora - tiempoBasura2 >= TIEMPO_BASURA_MS)) {
        bote2Lleno = true;
        digitalWrite(LED_BASURA2, HIGH);
        Serial.println("Bote 2 LLENO — LED encendido");
      }
    } else {
      tiempoBasura2 = 0;
      if (bote2Lleno) { 
        bote2Lleno = false; 
        digitalWrite(LED_BASURA2, LOW); 
      }
    }

    // Bote 3
    if (distB3 > 0 && distB3 <= DISTANCIA_BASURA_CM) {
      if (tiempoBasura3 == 0) tiempoBasura3 = ahora;
      if (!bote3Lleno && (ahora - tiempoBasura3 >= TIEMPO_BASURA_MS)) {
        bote3Lleno = true;
        digitalWrite(LED_BASURA3, HIGH);
        Serial.println("Bote 3 LLENO — LED encendido");
      }
    } else {
      tiempoBasura3 = 0;
      if (bote3Lleno) { 
        bote3Lleno = false; 
        digitalWrite(LED_BASURA3, LOW); 
      }
    }

    // ----------------------------------------------------------
    // PUBLICAR DISTANCIAS AL MQTT PERIODICAMENTE
    // ----------------------------------------------------------
    if (ahora - tiempoUltimaPublicacionBasura >= INTERVALO_PUBLICACION_BASURA) {
      // Calcular porcentaje de llenado (0 a 100) para cada contenedor usando la constante ALTURA_CONTENEDOR_CM
      int pct1 = (distB1 > 0) ? constrain(100 - (distB1 * 100 / ALTURA_CONTENEDOR_CM), 0, 100) : 10;
      int pct2 = (distB2 > 0) ? constrain(100 - (distB2 * 100 / ALTURA_CONTENEDOR_CM), 0, 100) : 10;
      int pct3 = (distB3 > 0) ? constrain(100 - (distB3 * 100 / ALTURA_CONTENEDOR_CM), 0, 100) : 10;

      // Evaluar si hay un cambio significativo o si ha pasado el tiempo para un reporte forzado
      bool forzarReporte = (ahora - tiempoUltimoReporteForzado >= INTERVALO_REPORTE_FORZADO);
      bool cambioPct1 = (abs(pct1 - ultimoPct1) >= UMBRAL_CAMBIO_PCT);
      bool cambioPct2 = (abs(pct2 - ultimoPct2) >= UMBRAL_CAMBIO_PCT);
      bool cambioPct3 = (abs(pct3 - ultimoPct3) >= UMBRAL_CAMBIO_PCT);

      if ((cambioPct1 || cambioPct2 || cambioPct3 || forzarReporte) && client.connected()) {
        char jsonResiduo[128];

        // Publicar JSON de Plástico
        snprintf(jsonResiduo, sizeof(jsonResiduo), 
                 "{\"contenedor\":\"Plastico\",\"distancia\":%.2f,\"porcentaje\":%d}",
                 (distB1 > 0 ? (float)distB1 : 0.0), pct1);
        client.publish(TOPIC_RESIDUOS, jsonResiduo, true);

        // Publicar JSON de Inorgánico
        snprintf(jsonResiduo, sizeof(jsonResiduo), 
                 "{\"contenedor\":\"Inorganico\",\"distancia\":%.2f,\"porcentaje\":%d}",
                 (distB2 > 0 ? (float)distB2 : 0.0), pct2);
        client.publish(TOPIC_RESIDUOS, jsonResiduo, true);

        // Publicar JSON de Orgánico
        snprintf(jsonResiduo, sizeof(jsonResiduo), 
                 "{\"contenedor\":\"Organico\",\"distancia\":%.2f,\"porcentaje\":%d}",
                 (distB3 > 0 ? (float)distB3 : 0.0), pct3);
        client.publish(TOPIC_RESIDUOS, jsonResiduo, true);

        // Guardar el estado publicado para la siguiente comparación
        ultimoPct1 = pct1;
        ultimoPct2 = pct2;
        ultimoPct3 = pct3;
        tiempoUltimoReporteForzado = ahora;
        Serial.println("JSONs de telemetría de basura publicados a MQTT");
      }

      tiempoUltimaPublicacionBasura = ahora;
    }

    // ----------------------------------------------------------
    //  SENSOR DE LUZ (LDR) — Enciende LEDs exteriores y relevador
    // ----------------------------------------------------------
    int valorLuz = analogRead(PIN_LDR);
    bool cambioLuz = false;

    // El control manual de las luces ahora es indefinido (sin temporizador de desactivación)
    // El ESP32 se mantendrá en MANUAL u ordenará AUTO cuando el usuario cambie el interruptor de modo en la app.

    if (!modoManualLuz) {
      if (valorLuz < UMBRAL_LUZ_LDR) {
        if (tiempoOscuridad == 0) tiempoOscuridad = ahora;
        if (!lucesEncendidas && (ahora - tiempoOscuridad >= TIEMPO_OSCURIDAD_MS)) {
          lucesEncendidas = true;
          digitalWrite(PIN_LEDS_EXT, HIGH);            
          setRelevador(true);            
          cambioLuz = true;
          Serial.println("Oscuridad detectada — Luces ENCENDIDAS (AUTO)");
        }
      } else {
        tiempoOscuridad = 0;
        if (lucesEncendidas) {
          lucesEncendidas = false;
          digitalWrite(PIN_LEDS_EXT, LOW);             
          setRelevador(false);             
          cambioLuz = true;
          Serial.println("Hay luz — Luces APAGADAS (AUTO)");
        }
      }
    }

    // Publicar JSON de alumbrado periódicamente o al haber un cambio
    static unsigned long tiempoUltimoReporteLuz = 0;
    if ((cambioLuz || ahora - tiempoUltimoReporteLuz >= 10000) && client.connected()) {
      char jsonLuz[160];
      snprintf(jsonLuz, sizeof(jsonLuz), 
               "{\"ldrLux\":%d,\"estadoOn\":%s,\"condicionNoche\":%s,\"luminariasActivas\":%d,\"modo\":\"%s\"}",
               valorLuz, 
               lucesEncendidas ? "true" : "false", 
               lucesEncendidas ? "true" : "false", 
               lucesEncendidas ? 12 : 0,
               modoManualLuz ? "MANUAL" : "AUTO");
      client.publish(TOPIC_LUCES, jsonLuz, true);
      tiempoUltimoReporteLuz = ahora;
      Serial.print("Publicado JSON Alumbrado: ");
      Serial.println(jsonLuz);
    }

    // ----------------------------------------------------------
    //  SENSOR DE AGUA — Activa bomba de riego
    // ----------------------------------------------------------
    long distAgua = medirDistancia(TRIG_AGUA, ECHO_AGUA);
    bool bombaActivaAnterior = (digitalRead(PIN_BOMBA) == HIGH);
    bool bombaActiva = bombaActivaAnterior;

    // Si venció el tiempo de control manual, regresar a automático
    if (modoManualAgua && (ahora - tiempoUltimaOrdenAgua >= TIEMPO_OVERRIDE_MANUAL_MS)) {
      modoManualAgua = false;
      Serial.println("Control manual de bomba finalizado. Regresando a AUTOMÁTICO.");
    }

    if (!modoManualAgua) {
      // Control automático con histéresis:
      // Activa la bomba si el agua está baja (distancia al sensor grande, ej. >= 14 cm)
      // Apaga la bomba si el agua está alta (distancia pequeña, ej. <= DISTANCIA_AGUA_CM (6 cm))
      if (distAgua >= 14) {
        bombaActiva = true;
        digitalWrite(PIN_BOMBA, HIGH);
      } else if (distAgua > 0 && distAgua <= DISTANCIA_AGUA_CM) {
        bombaActiva = false;
        digitalWrite(PIN_BOMBA, LOW);
      }
    }

    // Publicar JSON de agua periódicamente o si cambia el estado de la bomba
    static unsigned long tiempoUltimoReporteAgua = 0;
    if ((bombaActiva != bombaActivaAnterior || ahora - tiempoUltimoReporteAgua >= 10000) && client.connected()) {
      int pctAgua = (distAgua > 0) ? constrain(100 - (distAgua * 100 / 20), 0, 100) : 0;
      char jsonAgua[96];
      snprintf(jsonAgua, sizeof(jsonAgua), 
               "{\"nivelTanque\":%d,\"bombaActiva\":%s}",
               pctAgua, 
               bombaActiva ? "true" : "false");
      client.publish(TOPIC_BOMBA, jsonAgua, true);
      tiempoUltimoReporteAgua = ahora;
      Serial.print("Publicado JSON Agua: ");
      Serial.println(jsonAgua);
    }
  }
}
