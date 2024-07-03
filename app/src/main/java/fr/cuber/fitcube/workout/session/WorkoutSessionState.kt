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

data class SessionUiState(
    val status: SessionStatus,
    val currentSet: Int,
    val currentExercise: Int,
    val timer: Int,
    val workout: WorkoutWithExercises,
    val started: Long,
    val predictions: List<List<Double>>,
    val elapsedTime: Long,
    val paused: Boolean,
    val bufferTimer: Boolean,
    val rest: Int
)

fun defaultSessionUiState(size: Int) = SessionUiState(
    status = SessionStatus.START,
    0,
    0,
    10,
    defaultWorkoutWithExercises(size),
    0L,
    List(size) { List(4) { 10.0 } },
    0L,
    true,
    bufferTimer = false,
    if (Build.FINGERPRINT.contains("generic")) {
        5
    } else {
        120
    }
)