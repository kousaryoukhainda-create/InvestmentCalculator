# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep the BuildConfig
-keep class com.example.investmentcalculator.BuildConfig { *; }

# Keep R class members
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep custom application classes
-keep public class com.example.investmentcalculator.**
-keep class com.example.investmentcalculator.** { *; }

# Preserve all native method names and the names of their classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve custom application exceptions
-keep public class * extends java.lang.Exception
