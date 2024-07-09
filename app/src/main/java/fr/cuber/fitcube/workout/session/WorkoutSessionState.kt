package fr.cuber.fitcube.workout.session

import android.os.Build
import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultWorkoutWithExercises

enum class SessionStatus {
    START,
    REST,
    EXERCISE,
    DONE
}

data class SessionState(
    val status: SessionStatus,
    val currentSet: Int,
    val currentExercise: Int,
    val timer: Int,
    val workout: WorkoutWithExercises,
    val started: Long,
    val predictions: List<List<Double>>,
    val elapsedTime: Long,
    val paused: Boolean,
    val rest: Int
)

val isProbablyRunningOnEmulator: Boolean by lazy {
    // Android SDK emulator
    return@lazy ((Build.MANUFACTURER == "Google" && Build.BRAND == "google" &&
            ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                    && Build.FINGERPRINT.endsWith(":user/release-keys")
                    && Build.PRODUCT.startsWith("sdk_gphone_")
                    && Build.MODEL.startsWith("sdk_gphone_"))
                    //alternative
                    || (Build.FINGERPRINT.startsWith("google/sdk_gphone64_")
                    && (Build.FINGERPRINT.endsWith(":userdebug/dev-keys") || Build.FINGERPRINT.endsWith(":user/release-keys"))
                    && Build.PRODUCT.startsWith("sdk_gphone64_")
                    && Build.MODEL.startsWith("sdk_gphone64_"))))
            //
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            //bluestacks
            || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
            //bluestacks
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build")
            //MSI App Player
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.PRODUCT == "google_sdk")
}

fun defaultSessionState(size: Int) = SessionState(
    status = SessionStatus.START,
    0,
    0,
    10,
    defaultWorkoutWithExercises(size),
    0L,
    List(size) { List(4) { 10.0 } },
    0L,
    true,
    if (isProbablyRunningOnEmulator) {
        1
    } else {
        120
    }
)