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
    val sets: Map<Int, Int>,
    val order: List<Int>,
    val currentExercise: Int,
    val timer: Int,
    val workout: WorkoutWithExercises,
    val started: Long,
    val predictions: Map<Int, List<Double>>,
    val elapsedTime: Long,
    val paused: Boolean,
    val rest: Int
)

fun SessionState.exerciseCount() = workout.exercises.size

fun SessionState.current() = workout.exercises.find { it.exercise.id == order[currentExercise] }!!

fun SessionState.ordered() = workout.exercises.sortedBy { order.indexOf(it.exercise.id)}

fun SessionState.currentSet(): Int {
    return sets[order[currentExercise]]!!
}

fun defaultSessionState(size: Int, id: Int = 0) = SessionState(
    status = SessionStatus.START,
    List(size) { it }
        .associateWith { 0 }
        .toMutableMap(),
    List(size) { it },
    0,
    10,
    defaultWorkoutWithExercises(size, id),
    0L,
    List(size) { it }.associateWith { List(4) { it * 10.0 } },
    0L,
    true,
    1
)