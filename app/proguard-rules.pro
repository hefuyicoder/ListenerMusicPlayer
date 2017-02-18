# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/hefuyi/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# retrofit
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit2.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class io.hefuyi.listener.api.model.** { *; }
-keep class io.hefuyi.listener.mvp.model.** { *; }

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn rx.**

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepattributes Exceptions
-keepattributes Signature
-keepattributes *Annotation*

#renderscript
-keep class android.support.v8.renderscript.** { *; }

#searchview
-keep class android.support.v7.widget.SearchView { *; }

# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
