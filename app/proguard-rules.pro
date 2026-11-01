-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class com.sarashina.study.data.api.** { *; }
-keep class com.sarashina.study.data.db.** { *; }
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.mlkit.**
-keep class androidx.security.crypto.** { *; }
