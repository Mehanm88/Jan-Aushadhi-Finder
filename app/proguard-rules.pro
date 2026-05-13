# Room rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase

# Firebase rules
-keep class com.google.firebase.** { *; }

# Google Maps / Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# App Specific: Keep models used for JSON/Serialization
-keepclassmembers class com.example.janna.data.** { *; }

# Maintain metadata for Compose
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}