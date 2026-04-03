# Gson model classes — 保留序列化字段和默认值
-keepclassmembers class com.trip.family.data.** {
    <init>();
    <fields>;
}

# Gson 核心
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }

# 保留 @SerializedName 注解
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
