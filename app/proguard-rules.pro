# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.clairjour.app.**$$serializer { *; }
-keepclassmembers class com.clairjour.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.clairjour.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Tink (androidx.security.crypto) references JSR-305 annotations we don't ship.
-dontwarn javax.annotation.**
-dontwarn javax.annotation.concurrent.**

# SQLCipher (net.zetetic) native + reflection surface — keep everything.
-keep class net.zetetic.database.** { *; }
-dontwarn net.zetetic.database.**

# Tink internals accessed via reflection.
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
