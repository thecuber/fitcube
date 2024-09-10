package fr.cuber.fitcube.workout.session

import fr.cuber.fitcube.data.db.dao.WorkoutWithExercises
import fr.cuber.fitcube.data.db.dao.defaultWorkoutWithExercises

enum class SessionStatus {
    START,
    REST,
    EXERCISE,
    TIMING,
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

fun SessionState.exerciseCount() = workout.exercises.size

fun SessionState.current() = workout.exercises[currentExercise]

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
    1
)