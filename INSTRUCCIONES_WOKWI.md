# 📖 Instrucciones de Uso - Diagrama Wokwi CityGrid

## ✅ Correcciones Realizadas

### 1. **Sensor LDR (Fotoresistor)**

**Error anterior:**

- Usaba pin `OUT` (no existe en Wokwi)
- Conectado a 3.3V con resistencia externa de 10kΩ

**Corrección:**

- ✅ Pin correcto: **AO** (Analog Output)
- ✅ Alimentación: **5V** (no 3.3V)
- ✅ **Sin resistencia externa**: El módulo `wokwi-photoresistor-sensor` ya incluye divisor interno de 10kΩ
- ✅ Atributo agregado: `"lux": "500"` (valor inicial de luz)

### 2. **Conexiones Verificadas**

Todos los demás componentes están correctamente conectados:

- ✅ HC-SR04: VCC→5V, GND→GND, TRIG/ECHO→GPIOs correctos
- ✅ LEDs indicadores: Ánodo→GPIO, Cátodo→Resistencia 220Ω→GND
- ✅ MOSFETs: Gate→GPIO, Source→GND, Drain→Carga
- ✅ Relevador: IN→GPIO, VCC→5V, GND→GND

---

## 🚀 Cómo Usar el Diagrama en Wokwi

### Paso 1: Crear Proyecto

1. Ve a [https://wokwi.com](https://wokwi.com)
2. Haz clic en **"Start Creating"** o **"New Project"**
3. Selecciona **"ESP32"** como placa

### Paso 2: Cargar el Diagrama

**Opción A - Copiar y Pegar:**

1. Abre el archivo `DIAGRAMA_WOKWI.json` en VS Code
2. Selecciona todo el contenido (Ctrl+A) y copia (Ctrl+C)
3. En Wokwi, abre el archivo `diagram.json` (panel izquierdo)
4. Reemplaza TODO el contenido con lo copiado
5. Guarda (Ctrl+S)

**Opción B - Subir Archivo:**

1. En Wokwi, ve al menú **File** → **Open**
2. Selecciona el archivo `DIAGRAMA_WOKWI.json`
3. Confirma que quieres reemplazar el diagrama actual

### Paso 3: Agregar el Código Arduino

1. En Wokwi, abre el archivo `sketch.ino` (o créalo si no existe)
2. Copia el contenido de tu sketch real: `sketch_cityGrid/sketch_cityGrid.ino`
3. Pega en el `sketch.ino` de Wokwi
4. Guarda

### Paso 4: Ejecutar Simulación

1. Haz clic en el botón verde **"Play"** (▶️) arriba a la derecha
2. Espera a que compile y cargue
3. ¡Verás el circuito completo funcionando!

---

## 🎮 Controles Interactivos en la Simulación

### Sensores HC-SR04

- **Haz clic** en cualquier sensor HC-SR04
- Aparecerá un **slider** para ajustar la distancia (2-400 cm)
- Útil para simular diferentes niveles de llenado

### Sensor LDR

- **Haz clic** en el módulo LDR
- Usa el slider para cambiar el valor de **lux** (iluminación)
- Valores bajos = oscuridad, valores altos = luz brillante

### Motor (Bomba)

- Girará cuando el MOSFET esté activado (GPIO 32 HIGH)

### LEDs

- Se encenderán/apagarán según el estado de los GPIOs

---

## 🔍 Verificación de Conexiones

### Lista de Verificación Visual

| Componente       | Conexión Correcta                   | Color Cable |
| ---------------- | ----------------------------------- | ----------- |
| **HC-SR04 #1**   | VCC→5V, GND→GND, TRIG→D4, ECHO→D5   | 🔴⚫🟡🟡    |
| **HC-SR04 #2**   | VCC→5V, GND→GND, TRIG→D18, ECHO→D19 | 🔴⚫🟡🟡    |
| **HC-SR04 #3**   | VCC→5V, GND→GND, TRIG→D21, ECHO→D22 | 🔴⚫🟡🟡    |
| **HC-SR04 #4**   | VCC→5V, GND→GND, TRIG→D25, ECHO→D26 | 🔴⚫🟡🟡    |
| **LED Basura 1** | A→D13, C→220Ω→GND                   | 🔵⚫        |
| **LED Basura 2** | A→D14, C→220Ω→GND                   | 🔵⚫        |
| **LED Basura 3** | A→D27, C→220Ω→GND                   | 🔵⚫        |
| **LDR**          | VCC→5V, GND→GND, AO→D34             | 🔴⚫🟠      |
| **MOSFET Bomba** | G→D32, S→GND, D→Motor(-)            | 🟢⚫🟢      |
| **MOSFET LEDs**  | G→D33, S→GND, D→LEDs(-)             | 🟢⚫🟢      |
| **Relevador**    | IN→D23, VCC→5V, GND→GND             | 🟢🔴⚫      |

---

## ⚠️ Limitaciones de la Simulación

### Lo que SÍ funciona:

✅ Lectura de sensores HC-SR04 (ajustando distancia manualmente)
✅ Lectura analógica del LDR (ajustando lux)
✅ Control de LEDs por GPIOs
✅ Activación de MOSFETs y relevador
✅ Monitor Serial para debug

### Lo que NO funciona:

❌ **WiFi/MQTT**: Wokwi no simula conexión WiFi real
❌ **Supabase**: No hay acceso a internet desde la simulación
❌ **Certificados SSL**: No se pueden probar conexiones seguras
❌ **Consumo real de energía**: La simulación no mide amperaje

### Workaround para MQTT:

Puedes agregar código condicional para pruebas:

```cpp
#ifdef WOKWI_SIMULATION
  // Código de prueba sin MQTT
  Serial.println("Modo simulación - MQTT deshabilitado");
#else
  // Código real con MQTT
  client.connect(...);
#endif
```

---

## 🐛 Solución de Problemas Comunes

### Problema: "No aparece el diagrama"

**Solución:**

1. Verifica que copiaste TODO el JSON (incluye llaves `{}` externas)
2. Revisa la consola del navegador (F12) por errores de sintaxis
3. Intenta pegar en [jsonlint.com](https://jsonlint.com) para validar

### Problema: "Los sensores no responden"

**Solución:**

1. Asegúrate de haber hecho clic en el sensor para ver el slider
2. Verifica que el código usa los pines correctos (D4, D5, etc.)
3. Revisa el Monitor Serial para ver lecturas

### Problema: "Error de compilación"

**Solución:**

1. Tu sketch real usa librerías que Wokwi no tiene (PubSubClient, WiFiManager)
2. Comenta temporalmente esas secciones para probar solo sensores
3. O crea una versión simplificada del sketch para simulación

### Problema: "El LDR siempre lee 0 o 1023"

**Solución:**

1. Haz clic en el módulo LDR
2. Ajusta el valor de lux con el slider
3. Verifica que estás leyendo el pin D34 (analógico)

---

## 📝 Ejemplo de Sketch Simplificado para Wokwi

Crea este archivo como `sketch_wokwi.ino` para probar solo los sensores:

```cpp
// Pins de sensores
const int TRIG_BASURA1 = 4;
const int ECHO_BASURA1 = 5;
const int PIN_LDR = 34;

void setup() {
  Serial.begin(9600);
  pinMode(TRIG_BASURA1, OUTPUT);
  pinMode(ECHO_BASURA1, INPUT);
}

long medirDistancia(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duracion = pulseIn(echoPin, HIGH);
  return duracion * 0.034 / 2;
}

void loop() {
  // Leer distancia
  long distancia = medirDistancia(TRIG_BASURA1, ECHO_BASURA1);
  Serial.print("Distancia Basura 1: ");
  Serial.print(distancia);
  Serial.println(" cm");

  // Leer LDR
  int valorLuz = analogRead(PIN_LDR);
  Serial.print("Valor LDR: ");
  Serial.println(valorLuz);

  delay(1000);
}
```

---

## 🎯 Flujo de Trabajo Recomendado

1. **Primero:** Prueba sensores individuales en Wokwi
2. **Segundo:** Verifica lógica de control (LEDs, MOSFETs)
3. **Tercero:** Implementa en hardware real
4. **Cuarto:** Agrega WiFi/MQTT en el ESP32 físico

---

## 📚 Recursos Adicionales

- [Documentación oficial de Wokwi](https://docs.wokwi.com/)
- [Referencia de componentes](https://docs.wokwi.com/parts/)
- [Ejemplos de proyectos ESP32](https://wokwi.com/learning-platform/esp32)
- [Discord de Wokwi (comunidad)](https://wokwi.com/discord)

---

**¿Necesitas ayuda con algo específico del diagrama?**
