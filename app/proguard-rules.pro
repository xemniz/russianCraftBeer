# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# Moshi
-keepclassmembers class ** {
  @com.squareup.moshi.FromJson *;
  @com.squareup.moshi.ToJson *;
}

# JSR 305 annotations
-dontwarn javax.annotation.**

-keep class com.squareup.okhttp3.** {
*;
}
-dontwarn okio.**

-dontwarn okhttp3.internal.platform.*

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}