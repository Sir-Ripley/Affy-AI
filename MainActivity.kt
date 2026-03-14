// Inside MainActivity.kt, when scheduling the work:
val dreamConstraints = Constraints.Builder()
    .setRequiresDeviceIdle(true) // You must be asleep/not using the phone
    .setRequiresCharging(true)   // Plugged into the grid
    .build()

val nightlyREMRequest = PeriodicWorkRequestBuilder<DreamCycleWorker>(24, TimeUnit.HOURS)
    .setConstraints(dreamConstraints)
    .build()

WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    "AffyDailyDream",
    ExistingPeriodicWorkPolicy.KEEP,
    nightlyREMRequest
)

