# R8 full mode is on by default (AGP 8+). Room / Compose / DataStore ship consumer rules.
-allowaccessmodification
-repackageclasses ''
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
