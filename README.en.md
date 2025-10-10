# 🎮 Tarati — A Board Game by George Spencer Brown

<div style="text-align: center;">

[![Logo](screenshots/logo.png)](screenshots/logo.png)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.11.0-blue.svg)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://www.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![🇪🇸](https://flagcdn.com/w20/es.png)](README.md)

**A native Android implementation of the fascinating strategy game Tarati**

[Download APK](#-download) • [How to Play](#-how-to-play) • [Features](#-features)

</div>

## 📖 About the Game

Tarati is a strategic board game created by **George Spencer Brown**, the brilliant author of "Laws of Form". This game
combines elements of chess and checkers with unique movement and capture mechanics, based on Spencer Brown's calculus of
distinctions.

> *"A game of distinctions and decisions" - George Spencer Brown*

This application is a complete adaptation to **Kotlin and Jetpack Compose** of
the [original React implementation](https://github.com/adamblvck/tarati-react) created by Adam Blvck.

## 🚀 Features

### 🎯 Gameplay

- **Two players**: White vs Black with 4 pieces each
- **Smart AI**: 4 configurable difficulty levels
- **Touch movement**: Intuitive tap-and-move interface
- **Complete history**: Undo/redo move system
- **Authentic rules**: Faithful to Spencer Brown's original design

### 🎨 User Experience

- **Modern interface**: Completely designed with Jetpack Compose
- **Adaptive theme**: Light and dark mode support
- **Responsive design**: Adapts to any screen size
- **Smooth animations**: Fluid transitions and visual feedback
- **Intuitive navigation**: Side drawer with accessible controls

### 🤖 Artificial Intelligence

- **Minimax algorithm** with Alpha-Beta pruning
- **Transposition table** for optimization
- **4 difficulty levels**:
    - 🟢 Easy (depth 3)
    - 🟡 Medium (depth 6)
    - 🟠 Hard (depth 9)
    - 🔴 Champion (depth 12)

## 📥 Download

### Latest Version: v1.0.0

[![Download APK](https://img.shields.io/badge/Download_APK-v1.0.0-success?style=for-the-badge&logo=android)](https://github.com/AgustinGomila/Tarati/releases)

**System Requirements:**

- Android 8.0 (API 26) or higher
- 15-20 MB free space
- Touch screen

**Installation:**

1. Download the APK file from the link above
2. Allow "Install from unknown sources" if prompted
3. Run the APK file and follow the instructions
4. Enjoy the game!

## 🎮 How to Play

### Game Objective

Control the board by converting your opponent's pieces through strategic moves.

### Basic Moves

1. **Select** a piece of your color by tapping it
2. **Green vertices** show valid moves
3. **Tap an empty vertex** to move your piece
4. **Converted pieces** change to your color

### Special Rules

- **Forward movement**: Non-upgraded pieces only move forward
- **Upgrades**: When entering the opponent's base, pieces are upgraded
- **Captures**: Moving adjacent to enemy pieces converts them
- **Game end**: When a player cannot move or loses all their pieces

### Controls

- **New Game**: Reset the current game
- **Enable/Disable AI**: Play against AI or a friend
- **Back/Forward**: Navigate through move history
- **Difficulty**: Adjust AI level

## 🏗️ Technologies

```kotlin
// Complete technical stack
-Kotlin 2.2.20
-Jetpack Compose 1.11.0
-Material Design 3
-Koin 4.1.1
-DataStore 1.1.7
-Coroutines for asynchronous operations
        -MVVM Architecture
        -Minimax algorithm with Alpha -Beta pruning
```

### Project Structure

```
├── game/
│   ├── core/           # Game definitions
│   ├── ai/             # Artificial intelligence logic
│   ├── logic/          # Position and state logic
│   └── utils/          # Coordinators and utilities
└── ui/
    ├── components/     # Reusable components
    │   ├── board/
    │   ├── sidebar/
    │   └── common/     # Common components
    ├── screens/        # Main screens
    │   ├── main/
    │   ├── settings/
    │   └── splash/
    ├── navigation/     # Navigation system
    ├── localization/   # Language manager
    ├── theme/          # Design system
    └── preview/        # Preview utilities
```

## 🧠 Symbols and Meaning

The Tarati board represents a deep symbolic structure:

| Element                 | Quantity     | Symbolic Meaning              |
|-------------------------|--------------|-------------------------------|
| **Pieces**              | 4 per player | The 4 classical elements      |
| **Circumference (C)**   | 12 positions | The 12 zodiac signs           |
| **Boundary (B)**        | 6 positions  | 6 hermetic planetary concepts |
| **Absolute Center (A)** | 1 position   | The Sun, Tiphereth            |

## 🖼️ Screenshots

| <img src="/screenshots/screenshot1.jpeg" alt="Tarati Screenshot" width="300"/> | <img src="/screenshots/screenshot3.jpeg" alt="Tarati Screenshot" width="300"/> |
|--------------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| <img src="/screenshots/screenshot4.jpeg" alt="Tarati Screenshot" width="300"/> | <img src="/screenshots/screenshot2.jpeg" alt="Tarati Screenshot" width="300"/> |
| <img src="/screenshots/screenshot5.jpeg" alt="Tarati Screenshot" width="300"/> | <img src="/screenshots/screenshot6.jpeg" alt="Tarati Screenshot" width="300"/> |

* Intuitive and simple interface designed with Jetpack Compose

## 👥 Credits and Acknowledgments

### Contributions

- **Original Concept**: George Spencer Brown - *Laws of Form*
- **React Implementation
  **: [Adam Blvck](https://github.com/adamblvck) - [tarati-react](https://github.com/adamblvck/tarati-react)
- **Android Port**: Agustín Gomila - Complete adaptation to Kotlin/Jetpack Compose

### Philosophical Inspiration

Tarati is based on George Spencer Brown's revolutionary work in *Laws of Form*, which introduces the Calculus of
Distinctions - a mathematically complete system for notation and calculation with distinctions.

**To learn more:**

- [📺 Louis Kauffman's Video about Laws of Form](https://youtu.be/UqMl_Wb04nU)
- [🎥 LoF Conference 2019](https://www.youtube.com/playlist?list=PLl8xLayCI7YcFU3huTvSPC11xBFioxtpo)
- [📚 LoF Mini Course by Leon Conrad](https://www.youtube.com/playlist?list=PLoK3NtWr5NbqEOdjQrWaq1sDweF7NJ5NB)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Agustín Gomila

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software")...
```

## 🤝 Contributing

Contributions are welcome. Please:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Contact

**Developer**: [Agustín Gomila](https://github.com/AgustinGomila)
**Email**: [gomila.agustin@gmail.com]
**Project**: [Tarati](https://github.com/AgustinGomila/Tarati)

---

<div style="text-align: center;">

### ⭐ Enjoying the game? Give the repository a star!

_"To teach pride in knowledge is to put up an effective barrier against any advance upon what is already known."_
—**George Spencer-Brown**

</div>

---

*Note: This project is an educational implementation and is not for commercial purposes. All rights to the original
concept belong to George Spencer Brown.*