package fr.cuber.fitcube.exercise.choose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.entity.WorkoutMode
import javax.inject.Inject

@HiltViewModel
class ExerciseChooseViewModel @Inject constructor(
    private val repository: RoomRepository
): ViewModel() {

    fun getExercises() = repository.getExerciseTypes()
    suspend fun createWorkoutExercise(ex: ExerciseType, workout: Int): Long {
        repository.updateWorkoutStatus(workout, false)
        return repository.createWorkoutExercise(
            WorkoutExercise(
                0,
                ex.id,
                workout,
                WorkoutMode.LOADED_REPETITION,
                emptyList()
            )
        )
    }


}