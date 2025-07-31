# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
-keep public class com.google.android.gms.* { public *; }
-keep public class com.google.firebase.** { public *; }
-keep class com.google.firebase.** { *; }
-keep class org.apache.** { *; }
-keep class android.arch.lifecycle.** { *; }
-keep class com.mapbox.android.core.location.** { *; }
-keep class com.mapbox.** { *; }
-keepclassmembers class com.mapbox.maps.renderer.** { *; }
-keep class com.mapbox.maps.renderer.** { *; }
-keep class com.mapbox.maps.extension.style.** { *; }
-keep class com.mapbox.mapboxsdk.** { *; }
-keep class com.mapbox.android.telemetry.**
-keep class com.mapbox.android.core.location.**
-keep class com.mapbox.android.core.location.** { *; }

-keep class beh59.aber.ac.uk.cs39440.mmp.models.** { *; }
