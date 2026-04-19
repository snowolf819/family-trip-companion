# kotlinx-serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.familytrip.companion.data.model.**$$serializer { *; }
-keepclassmembers class com.familytrip.companion.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.familytrip.companion.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
