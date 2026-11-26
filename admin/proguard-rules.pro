# Keep Firebase Firestore model classes for Admin APK
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName *;
}
