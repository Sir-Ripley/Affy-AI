# Keep TFLite Interpreter and related classes
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.ai.edge.litert.** { *; }

# Keep WorkManager worker classes (referenced by class name at runtime)
-keep class com.yourdomain.affy.DreamCycleWorker { *; }

# Keep AffinionHandler singleton (referenced reflectively by WorkManager and TTS callbacks)
-keep class com.yourdomain.affy.AffinionHandler { *; }
