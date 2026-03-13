# Quick Start Guide - Investment Calculator

## Setting Up the Project

### Option 1: Using Android Studio (Recommended)

1. **Extract the source code** from the zip file

2. **Open Android Studio**
   - Click "File" → "Open"
   - Navigate to the extracted `InvestmentCalculator` folder
   - Click "Open"

3. **Wait for Gradle to sync**
   - Android Studio will automatically download dependencies
   - This may take 2-5 minutes on first run

4. **Connect an Android device or open an emulator**
   - Physical Device: Enable USB debugging (Settings → Developer Options)
   - Emulator: Create/start a virtual device from AVD Manager

5. **Run the app**
   - Click "Run" menu → "Run 'app'"
   - Or press `Shift + F10`

### Option 2: Using Command Line

```bash
# Navigate to project directory
cd InvestmentCalculator

# Build the debug APK
./gradlew assembleDebug

# Install and run on connected device
./gradlew installDebug
./gradlew executeDebugTests
```

## Project Features

### 1. Compound Interest Calculator
- Calculate future value with different compounding frequencies
- Frequencies: Annually, Semi-Annually, Quarterly, Monthly, Daily
- Real-time calculation and validation

### 2. Simple Interest Calculator
- Calculate returns using simple interest formula
- Straightforward interest calculation without compounding

### 3. SIP Calculator
- Systematic Investment Plan calculator
- Perfect for recurring monthly investments
- Shows total amount and gains separately

## App Architecture

```
MainActivity
    ↓
InvestmentCalculatorViewModel (MVVM Pattern)
    ↓
Calculation Logic (Kotlin Functions)
    ↓
LiveData (State Management)
    ↓
UI Layout (activity_main.xml)
```

## Key Files to Understand

1. **MainActivity.kt** - UI logic and user interactions
2. **InvestmentCalculatorViewModel.kt** - Business logic and calculations
3. **activity_main.xml** - Layout definition
4. **colors.xml, strings.xml, themes.xml** - Resources

## Building for Release

### Unsigned Release APK
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

### Signed Release APK (Required for Play Store)
```bash
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=path/to/keystore.jks \
  -Pandroid.injected.signing.store.password=storePassword \
  -Pandroid.injected.signing.key.alias=keyAlias \
  -Pandroid.injected.signing.key.password=keyPassword
```

## Troubleshooting

### Issue: Gradle Sync Failed
**Solution**: 
- File → Sync Now
- File → Invalidate Caches / Restart
- Delete .gradle folder and sync again

### Issue: Android SDK Not Found
**Solution**:
- Open SDK Manager (Tools → SDK Manager)
- Install API 34 and Build Tools 34.0.0

### Issue: Emulator Issues
**Solution**:
- Use Android Virtual Device Manager
- Ensure at least 4GB RAM allocated
- Check "Use Quick Boot" in emulator settings

## Modifying the App

### Adding a New Calculation Method
1. Add method to `InvestmentCalculatorViewModel.kt`
2. Add UI elements to `activity_main.xml`
3. Add button click listener in `MainActivity.kt`

### Customizing Colors
Edit `app/src/main/res/values/colors.xml`

### Changing Strings/Labels
Edit `app/src/main/res/values/strings.xml`

## Performance Tips

- Minimum device: Android 7.0 (API 24)
- Recommended: Android 10+ with 2GB+ RAM
- App size: ~5-8 MB (debug), ~3-5 MB (release)

## Support Libraries Used

- Material Design 3
- AndroidX (modern Android framework)
- LiveData & ViewModel (lifecycle management)
- ViewBinding (type-safe view access)

## Testing the App

### Manual Testing Checklist
- [ ] All three calculation methods work
- [ ] Input validation works (negative numbers, empty fields)
- [ ] Results display correctly
- [ ] Clear button resets all fields
- [ ] UI is responsive on different screen sizes
- [ ] No crashes during normal usage

## Next Steps

1. Customize the app colors in `colors.xml`
2. Add your own company branding
3. Extend with additional calculation methods
4. Add data persistence (SharedPreferences or Database)
5. Publish to Google Play Store

## Contact & Support

For Android development resources:
- Android Official Documentation: https://developer.android.com
- Kotlin Language: https://kotlinlang.org
- Material Design: https://m3.material.io

---

Happy Calculating! 🚀
