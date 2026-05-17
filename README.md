# WeatherSnap

A weather reporting app built for the Android Intern Assignment. Search live weather by city, capture a photo using a custom CameraX screen, compress it, add field notes, and save it as a report — all stored locally with Room.

No API key needed. No splash screen. No fluff.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| State | StateFlow + Coroutines |
| DI | Hilt |
| Navigation | Navigation Compose |
| Networking | Retrofit 3 + OkHttp + Gson |
| Local DB | Room |
| Camera | CameraX |
| Image Loading | Coil 3 |

---

## Setup & Run

### Requirements

- Android Studio Hedgehog or newer
- JDK 17
- Android device or emulator running API 26+
- Physical device recommended for CameraX (emulator camera can be unreliable)

### Steps

1. Clone the repository

```bash
git clone https://github.com/yourusername/weathersnap.git
cd weathersnap
```

2. Open in Android Studio

   File → Open → select the project folder

3. Let Gradle sync finish. It will download all dependencies automatically.

4. Run on a device or emulator

   Press **Run ▶** or use:

```bash
./gradlew installDebug
```

That's it. No API keys, no `.env` files, no extra config. Open-Meteo is completely free and keyless.

---

## App Screens

**Weather Screen** — Search a city by name. Suggestions appear after 3 characters and are cached in memory so repeated queries don't hit the network again. Select a city to load temperature, condition, humidity, wind speed, and pressure.

**Create Report Screen** — Shows a frozen snapshot of the selected weather. Capture a photo using the custom camera, add field notes, and save. The image is compressed before saving and both original and compressed sizes are shown.

**Custom Camera Screen** — Built entirely with CameraX. No system camera intent. Shows a live preview, a Capture button, and a Close button. Handles its own camera permission request.

**Saved Reports Screen** — Lists all saved reports from Room DB, newest first. Shows the captured image, weather details, image sizes, timestamp, and notes. Displays an empty state when nothing has been saved yet.

---

## Project Structure

```
com.example.weathersnap/
├── data/
│   ├── local/          # Room entity, DAO, database
│   ├── remote/         # Retrofit API interfaces + response models
│   └── repository/     # WeatherRepository — single source of truth
├── di/                 # Hilt modules (NetworkModule, DatabaseModule)
├── domain/
│   └── model/          # Clean UI models (WeatherData, City)
└── ui/
    ├── camera/         # CameraScreen
    ├── createreport/   # CreateReportScreen + ViewModel
    ├── navigation/     # NavGraph, Routes, SharedViewModel
    ├── savedreports/   # SavedReportsScreen + ViewModel
    ├── theme/          # Material 3 colors, typography
    └── weather/        # WeatherScreen + ViewModel
```

---

## Developer Judgment Challenge

### The Problem

The report creation flow spans multiple steps — select weather, open the screen, capture a photo, type notes — and any of these can be interrupted by rotation, a phone call, or Android killing the process in the background.

### Approach: `SavedStateHandle` in `CreateReportViewModel`

All in-progress state in `CreateReportViewModel` is backed by `SavedStateHandle`:

```kotlin
var notes: String
    get() = savedStateHandle[KEY_NOTES] ?: ""
    set(value) { savedStateHandle[KEY_NOTES] = value }

var capturedImagePath: String?
    get() = savedStateHandle[KEY_IMAGE_PATH]
    set(value) { savedStateHandle[KEY_IMAGE_PATH] = value }
```

`SavedStateHandle` is persisted to Android's saved instance state bundle. Unlike a regular `MutableStateFlow` which only survives configuration changes (rotation), `SavedStateHandle` also survives process death — the case where Android kills the app while it's in the background and the user returns via the recents screen.

Every keystroke in the notes field and every photo capture writes to `SavedStateHandle` immediately. If the process is killed and restored, the ViewModel reconstructs from the bundle and the UI shows exactly where the user left off.

The weather snapshot is stored in `SharedViewModel` and scoped to the `WEATHER` back stack entry, which also survives rotation. The snapshot is frozen at the moment the user taps "Create Report" and is never silently re-fetched — the report always reflects exactly what the user saw when they started.

### Preventing Duplicate Saves

The Save button is disabled while `SaveReportState.Saving` is active, preventing double-taps from inserting duplicate rows. After a successful save, `SavedStateHandle` is cleared so the screen resets cleanly for the next report.

### Temp File Cleanup

When a photo is captured, a compressed JPEG is written to the app's `filesDir`. If the user saves the report, the file is kept (the path is stored in Room). If the user discards — navigates back without saving — `onCleared()` in the ViewModel deletes the file:

```kotlin
override fun onCleared() {
    super.onCleared()
    if (_saveState.value !is SaveReportState.Success) {
        capturedImagePath?.let { File(it).takeIf { f -> f.exists() }?.delete() }
    }
}
```

This ensures temp files never accumulate on the device indefinitely.

### Tradeoffs

`SavedStateHandle` only supports primitive types and `Parcelable` objects. `WeatherData` is not stored there — it lives in `SharedViewModel` instead, which is sufficient for rotation but technically won't survive process death in the rare edge case where the system kills the app between the user selecting weather and opening the Create Report screen. In practice this window is extremely short, and the user would just search again. A more complete solution would serialize `WeatherData` as JSON into `SavedStateHandle` directly, but that adds complexity without meaningful user-visible benefit for this assignment.

---

## Debug Logging

Network request and response bodies are logged to Logcat in debug builds only via `HttpLoggingInterceptor`. This is controlled by a `BuildConfig` flag:

```kotlin
if (BuildConfig.ENABLE_LOGGING) {
    addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
}
```

Release builds have logging disabled and will never expose raw API responses.

---

## Notes

- Open-Meteo requires no API key and has no rate limiting for reasonable usage
- Minimum SDK is 26 (Android 8.0) — covers the vast majority of active devices
- The app uses a single Activity with Navigation Compose handling all screen transitions
- All database operations run on `Dispatchers.IO`; image compression runs on `Dispatchers.Default`
