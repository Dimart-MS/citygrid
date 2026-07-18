# 🔧 Guía de Organización y Soldadura - CityGrid ESP32

## 📋 Concepto Clave: Cables Directos vs Rieles Compartidos

### 🔌 Dos Tipos de Conexiones

#### 1️⃣ **CABLES DIRECTOS** (GPIO → Componente)

Van **directamente** del pin del ESP32 al componente. Son únicos y no se comparten.

**Lista completa (15 cables):**

```
ESP32 GPIO 4  ──→ TRIG HC-SR04 #1 (Basura Plástico)
ESP32 GPIO 5  ──→ ECHO HC-SR04 #1
ESP32 GPIO 18 ──→ TRIG HC-SR04 #2 (Basura Inorgánico)
ESP32 GPIO 19 ──→ ECHO HC-SR04 #2
ESP32 GPIO 21 ──→ TRIG HC-SR04 #3 (Basura Orgánico)
ESP32 GPIO 22 ──→ ECHO HC-SR04 #3
ESP32 GPIO 25 ──→ TRIG HC-SR04 #4 (Agua)
ESP32 GPIO 26 ──→ ECHO HC-SR04 #4
ESP32 GPIO 13 ──→ Ánodo LED indicador #1
ESP32 GPIO 14 ──→ Ánodo LED indicador #2
ESP32 GPIO 27 ──→ Ánodo LED indicador #3
ESP32 GPIO 34 ──→ Punto medio LDR
ESP32 GPIO 32 ──→ Gate MOSFET bomba
ESP32 GPIO 33 ──→ Gate MOSFET LEDs exteriores
ESP32 GPIO 23 ──→ IN Relevador
```

#### 2️⃣ **RIELES COMPARTIDOS** (Alimentación general)

Estos van a **puntos comunes** que alimentan múltiples componentes.

**Rail 5V (positivo):**

```
ESP32 5V ──→ Punto central de distribución 5V
             ├─→ VCC HC-SR04 #1
             ├─→ VCC HC-SR04 #2
             ├─→ VCC HC-SR04 #3
             ├─→ VCC HC-SR04 #4
             ├─→ VCC Relevador
             └─→ (+) Bomba de agua (si es 5V)
```

**Rail GND (negativo/tierra):**

```
ESP32 GND ──→ Punto central de distribución GND
              ├─→ GND HC-SR04 #1
              ├─→ GND HC-SR04 #2
              ├─→ GND HC-SR04 #3
              ├─→ GND HC-SR04 #4
              ├─→ GND Relevador
              ├─→ Source MOSFET bomba
              ├─→ Source MOSFET LEDs
              ├─→ Resistencia LDR (10kΩ a GND)
              └─→ Cátodos LEDs indicadores (vía 220Ω)
```

---

## 🎯 Estrategia de Soldadura: "Árbol de Distribución"

### Paso 1: Crear el Tronco Principal

Soldar primero los **2 cables maestros** del ESP32:

```
                    ESP32
                   ┌─────┐
                   │     │
        5V ────────┤     ├─────── GND
                   └─────┘
                     │         │
                     │         │
                  [Cable      [Cable
                   rojo]       negro]
                     │         │
                     ▼         ▼
              ╔═════════╗ ╔═════════╗
              ║ PUNTO   ║ ║ PUNTO   ║
              ║ 5V      ║ ║ GND     ║
              ║ CENTRAL ║ ║ CENTRAL ║
              ╚═════════╝ ╚═════════╝
```

**Recomendación:** Usar una **regleta de terminales** o **tira de pines hembra** como punto central.

---

### Paso 2: Ramificar desde los Puntos Centrales

Desde cada punto central, soldar cables hacia los componentes:

```
PUNTO 5V CENTRAL:
    ├─ Cable rojo corto → VCC HC-SR04 #1
    ├─ Cable rojo corto → VCC HC-SR04 #2
    ├─ Cable rojo corto → VCC HC-SR04 #3
    ├─ Cable rojo corto → VCC HC-SR04 #4
    └─ Cable rojo corto → VCC Relevador

PUNTO GND CENTRAL:
    ├─ Cable negro corto → GND HC-SR04 #1
    ├─ Cable negro corto → GND HC-SR04 #2
    ├─ Cable negro corto → GND HC-SR04 #3
    ├─ Cable negro corto → GND HC-SR04 #4
    ├─ Cable negro corto → GND Relevador
    ├─ Cable negro corto → Source MOSFETs
    └─ Cable negro corto → Resistencia LDR
```

---

### Paso 3: Conectar Señales GPIO (Cables Directos)

Estos van **individualmente** sin pasar por puntos centrales:

```
ESP32 GPIO 4  ──────────────→ TRIG HC-SR04 #1
ESP32 GPIO 5  ──────────────→ ECHO HC-SR04 #1
ESP32 GPIO 18 ──────────────→ TRIG HC-SR04 #2
... (y así sucesivamente)
```

**Tip:** Agrupar estos cables en un **mazo** para mantener orden.

---

## 📐 Layout Físico Recomendado para Soldadura

### Opción A: PCB Personalizado (Ideal)

```
┌──────────────────────────────────────────────────────┐
│                                                      │
│  [ESP32]                                             │
│     │                                                │
│     ├──[Regleta 5V]──┬──[HC#1]                      │
│     │                ├──[HC#2]                      │
│     │                ├──[HC#3]                      │
│     │                └──[HC#4]                      │
│     │                                                │
│     ├──[Regleta GND]─┬──[HC#1]                      │
│     │                ├──[HC#2]                      │
│     │                ├──[HC#3]                      │
│     │                ├──[HC#4]                      │
│     │                ├──[MOSFETs]                   │
│     │                └──[Relevador]                 │
│     │                                                │
│     ├── GPIOs directos (mazo de 15 cables)          │
│     │                                                │
└──────────────────────────────────────────────────────┘
```

### Opción B: Protoboard Permanente (Más fácil)

Usar un protoboard grande y soldar **tiras de pines**:

```
┌──────────────────────────────────────────────────┐
│ Tira de pines macho soldada al protoboard:       │
│                                                  │
│ Pin 1: 5V  ──→ Todos los VCC                    │
│ Pin 2: GND ──→ Todos los GND                    │
│ Pin 3-17: GPIOs individuales                    │
│                                                  │
│ ESP32 se conecta con jumper wires a esta tira   │
└──────────────────────────────────────────────────┘
```

---

## 🎨 Código de Colores para Soldadura

| Color       | Uso                                   | Longitud sugerida |
| ----------- | ------------------------------------- | ----------------- |
| 🔴 Rojo     | 5V (alimentación positiva)            | Corto (3-5 cm)    |
| ⚫ Negro    | GND (tierra)                          | Corto (3-5 cm)    |
| 🟡 Amarillo | Señales sensores (TRIG/ECHO)          | Medio (7-10 cm)   |
| 🟢 Verde    | Señales actuadores (MOSFET/Relevador) | Medio (7-10 cm)   |
| 🔵 Azul     | LEDs indicadores                      | Corto (3-5 cm)    |
| 🟠 Naranja  | LDR (analógico)                       | Medio (7-10 cm)   |
| ⚪ Blanco   | Puentes auxiliares                    | Variable          |

---

## 🔥 Técnicas de Soldadura Recomendadas

### 1. Preparar los Cables

```
Antes de soldar:
1. Cortar cable a longitud exacta (+1 cm extra)
2. Pelar 3-4 mm de aislante
3. Estañar la punta (aplicar soldadura al cable solo)
4. Insertar en terminal/componente
5. Soldar unión
```

### 2. Puntos de Soldadura Críticos

**✅ Bien soldado:**

- Brilloso y cóncavo
- Cubre completamente la unión
- No hay exceso de soldadura

**❌ Mal soldado:**

- Opaco o grisáceo (soldadura fría)
- Forma de bola (exceso)
- No cubre todo el pad

### 3. Refuerzo Mecánico

Para cables que sufrirán tensión:

```
1. Soldar normalmente
2. Aplicar gota de silicona caliente en la base
3. O usar termoencogible sobre la unión
```

---

## 📦 Materiales Adicionales para Soldadura

| Item                          | Cantidad | Uso                     |
| ----------------------------- | -------- | ----------------------- |
| Cable AWG 22-24               | 2 metros | Conexiones generales    |
| Regleta de terminales 2x8     | 1        | Puntos centrales 5V/GND |
| Tira de pines hembra 40 pines | 1        | Para conectar ESP32     |
| Tubo termoencogible 3mm       | 1 metro  | Aislar uniones          |
| Silicona caliente             | 1 barra  | Refuerzo mecánico       |
| Cinta aislante                | 1 rollo  | Agrupar cables          |

---

## ✅ Checklist de Soldadura

### Antes de empezar:

- [ ] Tener soldador calentado (350-370°C)
- [ ] Tener estaño de buena calidad (60/40 o lead-free)
- [ ] Cortar todos los cables a medida
- [ ] Etiquetar cables con cinta masking tape

### Durante la soldadura:

- [ ] Soldar primero puntos centrales (5V y GND)
- [ ] Verificar continuidad con multímetro después de cada soldadura
- [ ] No mover cables hasta que enfríen (10-15 segundos)
- [ ] Limpiar punta del soldador regularmente

### Después de soldar:

- [ ] Probar continuidad 5V → todos los VCC
- [ ] Probar continuidad GND → todos los GND
- [ ] Verificar que NO haya cortos entre 5V y GND
- [ ] Revisar visualmente todas las uniones
- [ ] Probar con multímetro voltaje en cada punto

---

## 🧪 Prueba Paso a Paso Post-Soldadura

### Prueba 1: Continuidad de Alimentación

```
Multímetro en modo continuidad:
1. Punta roja en 5V del ESP32
2. Punta negra en VCC de cada HC-SR04
3. Debe sonar/beep en cada uno
4. Repetir para GND
```

### Prueba 2: Sin Cortocircuitos

```
Multímetro en modo resistencia:
1. Medir entre 5V y GND (sin ESP32 conectado)
2. Debe marcar infinito (OL) o >1MΩ
3. Si marca menos de 100Ω → hay corto, revisar
```

### Prueba 3: Señales GPIO

```
Con ESP32 conectado y programa de prueba:
1. Subir código simple que active GPIO 13
2. Medir con multímetro en ánodo LED #1
3. Debe leer ~3.3V cuando está activo
```

---

## 🛠️ Solución de Problemas Comunes en Soldadura

| Problema            | Causa                | Solución                       |
| ------------------- | -------------------- | ------------------------------ |
| Soldadura no pega   | Superficie oxidada   | Limpiar con lija fina          |
| Unión opaca         | Soldadura fría       | Recalentar y agregar estaño    |
| Corto entre pistas  | Exceso de estaño     | Retirar con soldador limpio    |
| Cable se desprende  | Mala adhesión        | Lijar ligeramente y resoldar   |
| Aislante se derrite | Temperatura muy alta | Bajar a 350°C, trabajar rápido |

---

## 💡 Consejos Finales

1. **Trabaja con calma:** Mejor 1 hora bien hecha que 15 minutos mal hechos
2. **Prueba frecuentemente:** No esperes a terminar todo para verificar
3. **Etiqueta mientras sueldas:** Es más difícil identificar cables después
4. **Deja margen:** Cables 10% más largos de lo necesario por si necesitas ajustar
5. **Documenta:** Toma fotos antes de cerrar/cubrir las conexiones

---

## 📸 Referencia Visual (Descripción)

Imagina tu proyecto terminado así:

```
VISTA FRONTAL:

    [ESP32]
       │
       ├──[Regleta 5V]──┬──[HC#1]──[HC#2]──[HC#3]──[HC#4]
       │                └──[Relevador]
       │
       ├──[Regleta GND]─┬──[HC#1]──[HC#2]──[HC#3]──[HC#4]
       │                ├──[MOSFET bomba]
       │                ├──[MOSFET LEDs]
       │                └──[LDR+Resistencia]
       │
       └──[Mazo 15 cables GPIO]──→ Cada uno a su sensor/actuador

VISTA LATERAL:
- Cables rojos/negros cortos y ordenados hacia arriba
- Cables de señales en mazo horizontal hacia la derecha
- Todo sujeto con bridas o cinta cada 5 cm
```

---

**¿Listo para soldar? Sigue el orden: Puntos centrales → Alimentación → Señales → Pruebas**
