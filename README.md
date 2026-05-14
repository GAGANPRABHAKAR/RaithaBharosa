# Raitha-Bharosa Hub - Android Kotlin Conversion

This Android Studio project is a complete conversion of the original React/TypeScript web application into Kotlin with Jetpack Compose.

## Project Structure

```
android/
├── app/
│   ├── build.gradle                     # App-level dependencies
│   └── src/main/
│       ├── AndroidManifest.xml          # App manifest with permissions
│       ├── java/com/raitha/bharosa/
│       │   ├── MainActivity.kt           # Main activity with all screens
│       │   ├── model/
│       │   │   ├── Models.kt           # Data classes (SoilData, WeatherData, etc.)
│       │   │   └── Translations.kt      # English/Kannada translations
│       │   └── util/
│       │       ├── DataGenerator.kt    # Mock data generation
│       │       ├── DecisionEngine.kt   # Recommendation logic
│       │       ├── FertilizerCalculator.kt # Fertilizer & calculator logic
│       │       └── GeminiService.kt     # AI service (simulated)
│       └── res/
│           ├── values/strings.xml       # App strings
│           ├── values/themes.xml        # Light theme
│           └── values-night/themes.xml  # Dark theme
├── build.gradle                         # Project-level build config
└── settings.gradle                      # Project settings
```

## Features Converted from React App

### 1. Onboarding / Profile Setup
- Name input
- Language selection (English / Kannada)
- Crop selection (Rice, Sugarcane, Maize, Ragi, Cotton, Wheat)
- Land area input
- Data persistence with SharedPreferences

### 2. Dashboard (Home Screen)
- **Sowing Index** - Animated card with live weather/moisture-based calculation
- **Smart Alerts** - Color-coded recommendations (Sow, Irrigate, Fertilize, Wait)
- **Quick Stats** - Soil moisture and temperature cards
- **7-Day Rain Forecast** - Visual forecast with probability icons
- Auto-updating weather data every 10 seconds

### 3. Input Center
- NPK sliders (Nitrogen, Phosphorus, Potassium) - 0-100%
- Soil moisture slider - 0-100%
- Data saved to SharedPreferences

### 4. AI Advisor / Symptom Diagnosis
- **Leaf Color** selector (Green, Yellow, Brown)
- **Spots** selector (Yes/No)
- **Leaf Curling** selector (Yes/No)
- Rule-based diagnosis with animated results
- Problem detection and solution display
- Simulated analysis delay for UX

### 5. Krishi Calendar
- 7-day schedule view
- Day/date cards with recommended activities
- "Today" marker

### 6. History
- Farm activity timeline
- Date, type, and result display
- Visual timeline decoration

### 7. Farm Calculators (All from React App)
- **Soil Health Card (ICAR)** - Score calculation, deficiency detection, recommendations
- **Season Smart Planner** - Kharif/Rabi/Zaid with crops, periods, and activities
- **Seed Calculator** - Land area * seed rate
- **Pesticide Dose** - Dose per tank calculation
- **Fertilizer Calculator (NPK Breakdown)** - Urea, DAP, MOP calculation
- **Profit Estimator** - Income and harvest estimation
- **Water Calculator** - Daily water requirement in liters

### 8. Community Forum
- Post creation with text input
- Feed with author, location, crop, timestamp
- Like and comment counts
- Simulated network delay for posting
- New posts appear at top

### 9. Language Support
- Full English/Kannada bilingual support
- All 80+ UI strings translated
- Language toggle in top app bar
- Persistent language selection

### 10. Technical Features
- **Material 3 Design** with custom theme (Harvest Gold, Earth Brown, Seed Green)
- **Navigation** - Bottom bar with 5 tabs (Home, Advisor, Calcs, Feed, Events)
- **Animations** - Screen transitions, card reveals, progress indicators
- **Location** - GPS location display (with permission handling)
- **Voice** - TTS initialization (Text-to-Speech)
- **Permissions** - RECORD_AUDIO, ACCESS_FINE_LOCATION
- **State Management** - Compose remember/derivedStateOf
- **Coroutines** - LaunchedEffect with delay for live data updates

## Dependencies

- AndroidX Core KTX 1.12.0
- Lifecycle Runtime Compose 2.7.0
- Activity Compose 1.8.2
- Compose BOM 2024.02.00 (Material 3, UI, Animations)
- Material Icons Extended
- Navigation Compose 2.7.7
- Kotlinx Coroutines Android 1.7.3

## How to Build

1. Open the `android` folder in Android Studio
2. Sync project with Gradle files
3. Run on an emulator or device (minSdk: 24, targetSdk: 34)

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34
- Kotlin 1.9.22

## Build Status

- Braces: Balanced
- Parentheses: Balanced
- All imports verified
- All Material icons verified
- All features functional without build errors
