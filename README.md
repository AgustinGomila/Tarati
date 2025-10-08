# 🎮 Tarati - Un Juego de Tablero por George Spencer Brown

<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.11.0-blue.svg)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://www.android.com)
[![License](https://img.shields.io/badge/Licencia-MIT-yellow.svg)](LICENSE)
[![🇺🇸](https://flagcdn.com/w20/gb.png)](README.en.md)

**Una implementación nativa en Android del fascinante juego de estrategia Tarati**

[Descargar APK](#-descargar) • [Cómo Jugar](#-cómo-jugar) • [Características](#-características)

</div>

## 📖 Sobre el Juego

Tarati es un juego de mesa estratégico creado por **George Spencer Brown**, el brillante autor de "Leyes de la Forma" (
*Laws of Form*). Este juego combina elementos de ajedrez y damas con mecánicas únicas de movimiento y captura, basadas
en el cálculo de distinciones de Spencer Brown.

> *"Un juego de distinciones y decisiones" - George Spencer Brown*

Esta aplicación es una adaptación completa a **Kotlin y Jetpack Compose** de
la [implementación original en React](https://github.com/adamblvck/tarati-react) creada por Adam Blvck.

## 🚀 Características

### 🎯 Jugabilidad

- **Dos jugadores**: Blanco vs Negro con 4 piezas cada uno
- **IA inteligente**: 4 niveles de dificultad configurable
- **Movimiento táctil**: Interfaz intuitiva de tocar y mover
- **Historial completo**: Sistema de deshacer/rehacer movimientos
- **Reglas auténticas**: Fiel al diseño original de Spencer Brown

### 🎨 Experiencia de Usuario

- **Interfaz moderna**: Diseñada completamente con Jetpack Compose
- **Tema adaptable**: Soporte para modo claro y oscuro
- **Diseño responsive**: Se adapta a cualquier tamaño de pantalla
- **Animaciones fluidas**: Transiciones suaves y feedback visual
- **Navegación intuitiva**: Drawer lateral con controles accesibles

### 🤖 Inteligencia Artificial

- **Algoritmo Minimax** con poda Alpha-Beta
- **Tabla de transposición** para optimización
- **4 niveles de dificultad**:
    - 🟢 Fácil (profundidad 3)
    - 🟡 Medio (profundidad 6)
    - 🟠 Difícil (profundidad 9)
    - 🔴 Campeón (profundidad 12)

## 📥 Descargar

### Última Versión: v1.0.0

[![Descargar APK](https://img.shields.io/badge/Descargar_APK-v1.0.0-success?style=for-the-badge&logo=android)](https://github.com/AgustinGomila/Tarati/blob/master/app/release/app-release.apk)

**Requisitos del sistema:**

- Android 8.0 (API 26) o superior
- 10-15 MB de espacio libre
- Pantalla táctil

**Instalación:**

1. Descarga el archivo APK desde el enlace anterior
2. Permite "Instalar desde fuentes desconocidas" si se solicita
3. Ejecuta el archivo APK y sigue las instrucciones
4. ¡Disfruta del juego!

## 🎮 Cómo Jugar

### Objetivo del Juego

Controlar el tablero convirtiendo las piezas del oponente mediante movimientos estratégicos.

### Movimientos Básicos

1. **Selecciona** una pieza de tu color tocándola
2. **Los vértices verdes** muestran movimientos válidos
3. **Toca un vértice vacío** para mover tu pieza
4. **Las piezas convertidas** cambian a tu color

### Reglas Especiales

- **Movimiento forward**: Las piezas no mejoradas solo avanzan
- **Mejoras**: Al entrar en la base del oponente, las piezas se actualizan
- **Capturas**: Mover adyacente a piezas enemigas las convierte
- **Fin del juego**: Cuando un jugador no puede mover o pierde todas sus piezas

### Controles

- **Nueva Partida**: Reinicia el juego actual
- **Activar/Desactivar IA**: Juega contra la IA o un amigo
- **Atrás/Adelante**: Navega por el historial de movimientos
- **Dificultad**: Ajusta el nivel de la IA

## 🏗️ Tecnologías

```kotlin
// Stack técnico completo
-Kotlin 2.0.21
-Jetpack Compose 1.11.0
-Material Design 3
-Corrutinas para operaciones asíncronas
        -Arquitectura MVVM
        -Algoritmo Minimax con Alpha -Beta pruning
```

### Estructura del Proyecto

```
app/
├── ui/
│   ├── components/       # Componentes reutilizables
│   ├── screens/          # Pantallas principales
│   └── theme/            # Sistema de diseño
├── game/
│   ├── AI.kt             # Lógica de inteligencia artificial
│   ├── GameBoard.kt      # Definición del tablero
│   ├── Difficulty.kt     # Clase de datos de dificultades
│   └── GameState.kt      # Modelo de estado
└── helpers/
    └── PositionHelper.kt # Cálculo de posiciones
```

## 🧠 Símbolos y Significado

El tablero de Tarati representa una estructura simbólica profunda:

| Elemento                | Cantidad      | Significado Simbólico              |
|-------------------------|---------------|------------------------------------|
| **Piezas**              | 4 por jugador | Los 4 elementos clásicos           |
| **Circunferencia (C)**  | 12 posiciones | Los 12 signos zodiacales           |
| **Frontera (B)**        | 6 posiciones  | 6 conceptos planetarios herméticos |
| **Centro Absoluto (A)** | 1 posición    | El Sol, Tiphereth                  |

## 🖼️ Capturas de Pantalla

| <img src="/screenshots/screenshot1.jpeg" alt="Tarati Screenshot" width="300"/> | <img src="/screenshots/screenshot2.jpeg" alt="Tarati Screenshot" width="300"/> |
|--------------------------------------------------------------------------------|--------------------------------------------------------------------------------|

* Interfaz intuitiva y sencilla diseñada con Jetpack Compose

## 👥 Créditos y Agradecimientos

### Contribuciones

- **Concepto Original**: George Spencer Brown - *Laws of Form*
- **Implementación React**:
  [Adam Blvck](https://github.com/adamblvck) - [tarati-react](https://github.com/adamblvck/tarati-react)
- **Port a Android**: Agustín Gomila - Adaptación completa a Kotlin/Jetpack Compose

### Inspiración Filosófica

Tarati está basado en el trabajo revolucionario de George Spencer Brown en *Laws of Form*, que introduce el Cálculo de
Distinciones - un sistema matemáticamente completo para notación y cálculo con distinciones.

**Para aprender más:**

- [📺 Video de Louis Kauffman sobre Laws of Form](https://youtu.be/UqMl_Wb04nU)
- [🎥 Conferencia LoF 2019](https://www.youtube.com/playlist?list=PLl8xLayCI7YcFU3huTvSPC11xBFioxtpo)
- [📚 Mini Curso LoF por Leon Conrad](https://www.youtube.com/playlist?list=PLoK3NtWr5NbqEOdjQrWaq1sDweF7NJ5NB)

## 📄 Licencia

Este proyecto está bajo la licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

```
MIT License

Copyright (c) 2025 Agustín Gomila

Se concede permiso, libre de cargos, a cualquier persona que obtenga una copia
de este software y de los archivos de documentación asociados (el "Software")...
```

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Haz fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📞 Contacto

**Desarrollador**: [Agustín Gomila](https://github.com/AgustinGomila)  
**Email**: [gomila.agustin@gmail.com]  
**Proyecto**: [Tarati](https://github.com/AgustinGomila/Tarati)

---

<div align="center">

### ⭐ ¿Disfrutas del juego? ¡Dale una estrella al repositorio!

_“To teach pride in knowledge is to put up an effective barrier against any advance upon what is already known.”_ —
**George Spencer-Brown**

</div>

---

*Nota: Este proyecto es una implementación educativa y no tiene fines comerciales. Todos los derechos del concepto
original pertenecen a George Spencer Brown.*