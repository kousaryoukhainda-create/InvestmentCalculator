# Investment Calculator

A native Android application for calculating investment returns with support for multiple calculation methods.

## Features

- **Compound Interest Calculator**: Calculate future value with different compounding frequencies
  - Annually
  - Semi-Annually
  - Quarterly
  - Monthly
  - Daily

- **Simple Interest Calculator**: Calculate returns using simple interest formula

- **SIP Calculator**: Systematic Investment Plan calculator for recurring investments

- **Multi-Currency Support**: Choose from 10 different currencies
  - USD ($), EUR (€), GBP (£), INR (₹)
  - JPY (¥), CNY (¥), AUD (A$), CAD (C$)
  - CHF (Fr), KRW (₩)

- **Compact Material Design UI**: Optimized layout maximizing screen space with Material 3 design

- **Real-time Validation**: Input validation with helpful error messages

- **Responsive Layout**: Works seamlessly on various screen sizes

## Technical Stack

- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: AndroidX, Material Components
- **Data Binding**: LiveData, ViewModel

## Project Structure

```
InvestmentCalculator/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/investmentcalculator/
│   │       │   ├── MainActivity.kt
│   │       │   └── InvestmentCalculatorViewModel.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   ├── values/
│   │       │   │   ├── colors.xml
│   │       │   │   ├── strings.xml
│   │       │   │   └── themes.xml
│   │       │   └── drawable/
│   │       │       ├── ic_currency.xml
│   │       │       ├── ic_percent.xml
│   │       │       ├── ic_time.xml
│   │       │       └── ic_launcher.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── README.md
```

## Dependencies

- AndroidX Core KTX
- AndroidX AppCompat
- Material Components
- AndroidX ConstraintLayout
- AndroidX Lifecycle (ViewModel & LiveData)
- AndroidX Fragment

## Installation

### Prerequisites
- Android Studio (latest version)
- JDK 11 or higher
- Android SDK (API 34+)

### Steps

1. Clone the repository or extract the source code
2. Open Android Studio
3. Click "File" > "Open" and select the project directory
4. Wait for Gradle to sync
5. Click "Run" > "Run 'app'" or press Shift+F10

## Usage

1. **For Compound Interest**:
   - Enter Principal Amount
   - Enter Annual Interest Rate (%)
   - Enter Time Period (Years)
   - Select Compounding Frequency
   - Click "Calculate Compound Interest"

2. **For Simple Interest**:
   - Enter Principal Amount
   - Enter Annual Interest Rate (%)
   - Enter Time Period (Years)
   - Click "Calculate Simple Interest"

3. **For SIP**:
   - Enter Annual Interest Rate (%)
   - Enter Time Period (Years)
   - Enter Monthly Investment Amount
   - Click "Calculate SIP"

## Calculations

### Compound Interest Formula
```
A = P(1 + r/n)^(nt)
where:
  A = Future Value
  P = Principal Amount
  r = Annual Interest Rate (decimal)
  n = Compounding Frequency per year
  t = Time in years
```

### Simple Interest Formula
```
A = P(1 + rt)
where:
  A = Future Value
  P = Principal Amount
  r = Annual Interest Rate (decimal)
  t = Time in years
```

### SIP Formula
```
A = PMT × [((1 + r)^n - 1) / r] × (1 + r)
where:
  A = Future Value
  PMT = Monthly Investment Amount
  r = Monthly Interest Rate
  n = Number of Months
```

## Building APK

### Debug APK
```bash
./gradlew assembleDebug
```
The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
```bash
./gradlew assembleRelease
```
The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

## Color Theme

The app uses a Material Design dark theme with the following colors:
- Primary: #6200EE (Purple)
- Secondary: #03DAC6 (Cyan)
- Background: #121212 (Dark Gray)
- Surface: #1F1F1F (Slightly Lighter Gray)

## Future Enhancements

- [ ] Add data persistence (save/load calculations)
- [ ] Add calculation history
- [ ] Add inflation adjustment
- [ ] Add tax calculation
- [ ] Export results to PDF
- [ ] Dark/Light mode toggle
- [ ] Graph visualization of results

## License

This project is provided as-is for educational and personal use.

## Author

Developed as a native Android investment calculation application.

---

For questions or issues, please refer to the documentation or Android development resources.
