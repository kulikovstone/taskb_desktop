# Room instantiates generated database implementations through reflection.
-keep class * extends androidx.room3.RoomDatabase { <init>(); }

# SQLite's JNI library registers native methods, including unused ones.
-keep class androidx.sqlite.driver.bundled.** { *; }
