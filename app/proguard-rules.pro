# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.clustertracker.app.data.remote.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.clustertracker.app.**$$serializer { *; }
-keepclassmembers class com.clustertracker.app.** { *** Companion; }
-keepclasseswithmembers class com.clustertracker.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# commons-suncalc
-keep class org.shredzone.commons.suncalc.** { *; }
-dontwarn edu.umd.cs.findbugs.annotations.**
