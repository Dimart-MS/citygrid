# 📐 Diagrama de Conexiones - CityGrid ESP32

## 🔌 Resumen de Componentes

| Componente            | Cantidad | Tipo                      | Función                         |
| --------------------- | -------- | ------------------------- | ------------------------------- |
| HC-SR04 (ultrasónico) | 4        | Sensor                    | 3 para basura + 1 para agua     |
| LED indicador         | 3        | LED 5mm                   | Indican botes llenos (local)    |
| LEDs exteriores       | Variable | LED individuales          | Iluminación exterior automática |
| LDR                   | 1        | Fotoresistencia 2 patitas | Detecta oscuridad               |
| Resistencia           | 1        | 10kΩ                      | Divisor de voltaje para LDR     |
| Bomba de agua         | 1        | Sumergible 5V DC          | Riego automático                |
| Relevador             | 1        | Módulo 1 canal 5V         | Controla foco 120V              |
| MOSFET/Transistor     | 2        | IRF520 o similar          | Control LEDs exteriores y bomba |

---

## 🎯 Esquema General de Pines ESP32

```
┌─────────────────────────────────────────────────────┐
│                  ESP32 DEVKIT V1                     │
│                                                      │
│  GPIO 4  ─── TRIG Basura 1 (HC-SR04 #1)             │
│  GPIO 5  ─── ECHO Basura 1 (HC-SR04 #1)             │
│                                                      │
│  GPIO 18 ─── TRIG Basura 2 (HC-SR04 #2)             │
│  GPIO 19 ─── ECHO Basura 2 (HC-SR04 #2)             │
│                                                      │
│  GPIO 21 ─── TRIG Basura 3 (HC-SR04 #3)             │
│  GPIO 22 ─── ECHO Basura 3 (HC-SR04 #3)             │
│                                                      │
│  GPIO 25 ─── TRIG Agua (HC-SR04 #4)                 │
│  GPIO 26 ─── ECHO Agua (HC-SR04 #4)                 │
│                                                      │
│  GPIO 13 ─── LED Indicador Basura 1 (+)             │
│  GPIO 14 ─── LED Indicador Basura 2 (+)             │
│  GPIO 27 ─── LED Indicador Basura 3 (+)             │
│                                                      │
│  GPIO 34 ─── LDR (con resistencia 10kΩ a GND)       │
│                                                      │
│  GPIO 32 ─── MOSFET Gate → Bomba de agua            │
│  GPIO 33 ─── MOSFET Gate → LEDs exteriores          │
│  GPIO 23 ─── Relevador IN (control foco 120V)       │
│                                                      │
│  5V    ─── VCC todos los HC-SR04                    │
│  5V    ─── VCC módulo relevador                     │
│  GND   ─── GND común de TODOS los componentes       │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## 🗑️ Sensores de Basura (3x HC-SR04)

### Conexión individual por sensor:

```
HC-SR04 #1 (Bote Plástico)
┌──────────────┐
│   HC-SR04    │
│              │
│  VCC  ──────→ 5V ESP32
│  GND  ──────→ GND ESP32
│  TRIG ──────→ GPIO 4
│  ECHO ──────→ GPIO 5
└──────────────┘

HC-SR04 #2 (Bote Inorgánico)
┌──────────────┐
│   HC-SR04    │
│              │
│  VCC  ──────→ 5V ESP32
│  GND  ──────→ GND ESP32
│  TRIG ──────→ GPIO 18
│  ECHO ──────→ GPIO 19
└──────────────┘

HC-SR04 #3 (Bote Orgánico)
┌──────────────┐
│   HC-SR04    │
│              │
│  VCC  ──────→ 5V ESP32
│  GND  ──────→ GND ESP32
│  TRIG ──────→ GPIO 21
│  ECHO ──────→ GPIO 22
└──────────────┘
```

**Nota:** Los LEDs indicadores (GPIO 13, 14, 27) se conectan:

- **Ánodo (+)** → Pin GPIO correspondiente
- **Cátodo (-)** → Resistencia 220Ω → GND

---

## 💧 Sensor de Agua (HC-SR04 #4)

```
HC-SR04 #4 (Depósito de Agua)
┌──────────────┐
│   HC-SR04    │
│              │
│  VCC  ──────→ 5V ESP32
│  GND  ──────→ GND ESP32
│  TRIG ──────→ GPIO 25
│  ECHO ──────→ GPIO 26
└──────────────┘
```

**Instalación física:** Montar el sensor en la tapa del depósito apuntando hacia abajo para medir distancia al nivel del agua.

---

## 💡 Sistema de Iluminación

### A) LEDs Exteriores (GPIO 33)

Como son LEDs individuales de 2 pines, necesitas un **MOSFET** para controlarlos:

```
                    ┌─────────────┐
ESP32 GPIO 33 ─────→│ Gate (G)    │
                    │   MOSFET    │
GND ESP32     ─────→│ Source (S)  │
                    │             │
                    │ Drain (D)   │──→ Cátodos (-) de TODOS los LEDs
                    └─────────────┘

Fuente 5V externa ──────────────────→ Ánodos (+) de TODOS los LEDs
                                      (conectados en PARALELO)
```

**Configuración recomendada para LEDs:**

- Conectar **todos los ánodos (+)** juntos a 5V
- Conectar **todos los cátodos (-)** juntos al Drain del MOSFET
- Cada LED necesita su propia resistencia de **220Ω** en serie

**Ejemplo con 5 LEDs:**

```
5V ──[220Ω]──>|LED1|──┐
5V ──[220Ω]──>|LED2|──┤
5V ──[220Ω]──>|LED3|──┼──→ Drain MOSFET
5V ──[220Ω]──>|LED4|──┤
5V ──[220Ω]──>|LED5|──┘
```

### B) Foco 120V (GPIO 23 vía Relevador)

```
                    ┌──────────────────┐
ESP32 GPIO 23 ─────→│ IN (entrada)     │
                    │   RELEVADOR      │
GND ESP32     ─────→│ GND              │
5V ESP32      ─────→│ VCC              │
                    │                  │
                    │  NO ─────────────┤──→ Fase del foco 120V
                    │  NC              │
                    │  COM ────────────┤──→ Fase de la corriente 120V
                    └──────────────────┘

Neutro 120V ──────────────────────────→ Otro terminal del foco
```

**⚠️ ADVERTENCIA DE SEGURIDAD:**

- Trabajar con 120V es PELIGROSO
- Usar caja eléctrica aislada
- Verificar que el relevador soporte 120V AC
- Si no tienes experiencia, consulta con un electricista

---

## 🌞 Sensor LDR (GPIO 34)

El LDR de 2 patitas necesita una **resistencia de 10kΩ** para formar un divisor de voltaje:

```
        3.3V ESP32
           │
           ├──┐
           │  │
          ┌┴┐ │
          │ │ │  LDR (fotoresistencia)
          └┬┘ │
           │  │
           ├──┼──────────→ GPIO 34 (lectura analógica)
           │  │
          ┌┴┐ │
          │ │ │  Resistencia 10kΩ
          └┬┘ │
           │  │
           └──┘
           │
          GND
```

**Alternativa más simple en protoboard:**

```
3.3V ── LDR ──┬── GPIO 34
              │
             [10kΩ]
              │
             GND
```

---

## 💦 Bomba de Agua (GPIO 32)

La bomba sumergible 5V también necesita un MOSFET:

```
                    ┌─────────────┐
ESP32 GPIO 32 ─────→│ Gate (G)    │
                    │   MOSFET    │
GND ESP32     ─────→│ Source (S)  │
                    │             │
                    │ Drain (D)   │──→ Terminal negativo (-) de la bomba
                    └─────────────┘

Fuente 5V ────────────────────────→ Terminal positivo (+) de la bomba
```

**Nota:** Si la bomba consume más de 500mA, usa una fuente externa de 5V (no la del ESP32).

---

## 🔋 Alimentación Recomendada

### Opción 1: USB + Fuente externa (RECOMENDADA)

```
┌─────────────┐         ┌──────────────┐
│  Power Bank │         │ Fuente 5V 2A │
│  o USB PC   │         │  externa     │
└──────┬──────┘         └──────┬───────┘
       │                       │
       │ 5V                    │ 5V
       ▼                       ▼
   ┌────────┐           ┌─────────────┐
   │ ESP32  │           │ Actuadores: │
   │        │           │ - Bomba     │
   │        │           │ - LEDs ext. │
   └────────┘           └─────────────┘

GND de ambas fuentes deben estar CONECTADOS
```

### Opción 2: Solo USB (limitado)

- Solo funciona si NO usas muchos LEDs ni la bomba simultáneamente
- El ESP32 puede entregar máximo ~500mA por USB
- Riesgo de reinicios si hay picos de consumo

---

## 📋 Lista de Materiales Completa

| Item                     | Cantidad | Notas                                       |
| ------------------------ | -------- | ------------------------------------------- |
| ESP32 DevKit V1          | 1        | Cualquier variante con WiFi                 |
| HC-SR04                  | 4        | Sensores ultrasónicos                       |
| LED 5mm                  | 3+3      | 3 indicadores + LEDs exteriores             |
| Resistencia 220Ω         | 6+       | Para LEDs (mínimo 6)                        |
| Resistencia 10kΩ         | 1        | Para divisor LDR                            |
| LDR                      | 1        | Fotoresistencia básica 2 patitas            |
| MOSFET IRF520            | 2        | O equivalente (IRLZ44N, TIP120)             |
| Módulo relevador 1 canal | 1        | 5V, soporta 120V AC                         |
| Protoboard               | 1        | Para pruebas iniciales                      |
| Cables jumper            | 30+      | Macho-macho y macho-hembra                  |
| Fuente 5V 2A             | 1        | Para actuadores (opcional pero recomendado) |

---

## ⚡ Pasos de Conexión (Orden Recomendado)

1. **Primero:** Conectar solo ESP32 por USB y verificar que enciende
2. **Sensores HC-SR04:** Conectar uno por uno y probar con Serial Monitor
3. **LEDs indicadores:** Probar cada LED individualmente
4. **LDR:** Conectar con resistencia 10kΩ y leer valores en Serial
5. **MOSFETs:** Probar con un solo LED antes de conectar todos
6. **Bomba:** Probar brevemente (2-3 segundos) para verificar dirección
7. **Relevador:** ÚLTIMO paso, con precaución por 120V

---

## 🧪 Pruebas Básicas

### Verificar sensores HC-SR04:

Subir este código temporal al ESP32:

```cpp
void setup() {
  Serial.begin(9600);
  pinMode(4, OUTPUT); // TRIG Bote 1
  pinMode(5, INPUT);  // ECHO Bote 1
}

void loop() {
  digitalWrite(4, LOW);
  delayMicroseconds(2);
  digitalWrite(4, HIGH);
  delayMicroseconds(10);
  digitalWrite(4, LOW);

  long duracion = pulseIn(5, HIGH);
  float distancia = duracion * 0.034 / 2;

  Serial.print("Distancia Bote 1: ");
  Serial.print(distancia);
  Serial.println(" cm");

  delay(1000);
}
```

### Verificar LDR:

```cpp
void setup() {
  Serial.begin(9600);
}

void loop() {
  int valor = analogRead(34);
  Serial.print("Valor LDR: ");
  Serial.println(valor);
  delay(500);
}
```

---

## 🔧 Solución de Problemas Comunes

| Problema             | Causa probable             | Solución                            |
| -------------------- | -------------------------- | ----------------------------------- |
| HC-SR04 devuelve 0   | Mal cableado o sin 5V      | Verificar VCC y GND                 |
| LDR siempre lee 4095 | Sin resistencia pull-down  | Agregar 10kΩ a GND                  |
| LEDs no encienden    | MOSFET mal conectado       | Verificar Gate/Source/Drain         |
| Relevador no activa  | Necesita 5V externo        | Conectar VCC del relevador a 5V     |
| ESP32 se reinicia    | Consumo excesivo           | Usar fuente externa para actuadores |
| MQTT no conecta      | Certificado SSL incorrecto | Verificar hora NTP sincronizada     |

---

## 📱 Flujo de Datos Final

```
┌─────────────┐      MQTT       ┌──────────────┐
│   ESP32     │ ──────────────→ │  App Android  │
│             │   citygrid/     │   CityGrid    │
│ • Residuos  │   residuos      │               │
│ • Agua      │   agua          │ • Dashboard   │
│ • Alumbrado │   alumbrado     │ • Gráficos    │
│ • Alertas   │   alertas       │ • Controles   │
└─────────────┘                 └──────────────┘
       ↑                              ↓
       │                        control-luces
       │                        control-bomba
       └────────────────────────────────┘
```

---

**¿Necesitas que genere algún diagrama adicional o aclarar alguna conexión específica?**
