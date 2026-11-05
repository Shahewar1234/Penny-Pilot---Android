<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=0:1F3A5F,100:0E7C86&height=180&section=header&text=PennyPilot&fontSize=50&fontColor=ffffff&fontAlignY=38&desc=AI-Powered%20Expense%20Tracker%20for%20Android&descSize=18&descAlignY=60" width="100%" alt="PennyPilot banner"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
  <img src="https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=materialdesign&logoColor=white"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/Gemini_2.5_Flash-8E75B2?style=for-the-badge&logo=googlegemini&logoColor=white"/>
  <img src="https://img.shields.io/badge/Hilt-34A853?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Min_SDK-25-0E7C86?style=for-the-badge&logo=android&logoColor=white"/>
</p>

<p align="center">
  A self-designed personal finance app built natively with <b>Kotlin and Jetpack Compose</b>.<br/>
  Track expenses, see where your money goes, manage trips and shared wallets, and ask an <b>AI assistant</b> for spending insights.
</p>

---

## ✨ Features

| | Feature | What it does |
| --- | --- | --- |
| 🔐 | **Authentication** | Email/password and Google Sign-In with Firebase Authentication |
| 🏠 | **Dashboard & Home** | Balance overview, quick actions and recent activity at a glance |
| ➕ | **Add Expense** | Log expenses with category, amount, date and notes |
| 🧠 | **Smart Expenses** | Expenses grouped by day, category icons, date-range filters and summary insight cards |
| 📊 | **Analytics** | Spending by category with pie and bar charts, plus key insight cards |
| 🤖 | **AI Assistant** | Chat with a Gemini 2.5 Flash–powered finance assistant (Firebase AI Logic), with saved conversation history |
| ✈️ | **Travel Mode** | Create trips and convert spending with live currency exchange rates |
| 👥 | **Split Bills & Shared Wallets** | Split expenses and manage wallets shared with others |
| 👤 | **Profile** | Manage personal details and app preferences |

---

## 🏗️ Architecture

PennyPilot follows **MVVM with a clean, layered structure**, keeping UI, business logic and data sources separate.

```
app/src/main/java/com/example/pennypilot/
├── presentation/
│   ├── screens/        # Compose screens (home, dashboard, expenses, analytics, ai, travel, wallets, profile, auth)
│   ├── viewmodel/      # @HiltViewModel classes exposing UI state
│   └── navigation/     # Navigation Compose graph
├── data/
│   ├── repository/     # Repository interfaces + implementations
│   ├── local/          # Room database, DAOs, entities, converters
│   ├── model/          # Domain and UI models
│   └── mapper/         # API → domain mappers
├── di/                 # Hilt modules (App, Repository, Network, Ktor, AI)
└── ui/theme/           # Material 3 colors, typography and theme
```

---

## 🛠️ Tech Stack

| Layer | Technologies |
| --- | --- |
| **UI** | Jetpack Compose, Material 3, Compose Animation, Navigation Compose, Coil |
| **Architecture** | MVVM, Repository pattern, Kotlin Coroutines & Flow |
| **Dependency Injection** | Hilt |
| **Local Storage** | Room |
| **Networking** | Ktor Client, Kotlinx Serialization |
| **Backend** | Firebase Authentication, Cloud Firestore, Realtime Database, Cloud Messaging, Analytics |
| **AI** | Firebase AI Logic with Gemini 2.5 Flash |
| **Charts** | MPAndroidChart |
| **Logging** | Timber |

---

## 📸 Screenshots

<!-- Add your screenshots to a /screenshots folder, then remove this comment line and the one at the end.
<p align="center">
  <img src="screenshots/home.png" width="22%"/>
  <img src="screenshots/analytics.png" width="22%"/>
  <img src="screenshots/ai_assistant.png" width="22%"/>
  <img src="screenshots/travel_mode.png" width="22%"/>
</p>
-->

*Screenshots coming soon.*

---

## 🚀 Getting Started

**Requirements:** Android Studio (latest stable), JDK 17, Android device or emulator running Android 7.1+ (API 25).

```bash
git clone https://github.com/Shahewar1234/Penny-Pilot---Android.git
```

1. Open the project in Android Studio and let Gradle sync.
2. Create a Firebase project, add an Android app with package name `com.example.pennypilot`, and place your own `google-services.json` in the `app/` folder.
3. In the Firebase console, enable **Authentication** (Email/Password and Google), **Cloud Firestore** and **Firebase AI Logic**.
4. Run the app on an emulator or device.

---

## 👩‍💻 Author

**Shahewar Khan** — React Native & Android Developer

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Shahewar1234)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/shahewar-khan-8a8365215)

<p align="center"><i>A React Native version of PennyPilot is coming soon.</i></p>
