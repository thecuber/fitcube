package fr.cuber.fitcube.exercise.workoutedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import fr.cuber.fitcube.data.db.success
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutExerciseViewModel @Inject constructor(
    private val repository: RoomRepository

): ViewModel() {

    fun saveWorkoutExercise(ex: WorkoutExercise) = viewModelScope.launch {
        if(ex.prediction.isNotEmpty()) {
            repository.updateWorkoutStatus(ex.workoutId, true)
        }
        repository.saveWorkoutExercise(ex)
    }

    fun getExercise(id: Int) = repository.getWorkoutExercise(id).success()

}