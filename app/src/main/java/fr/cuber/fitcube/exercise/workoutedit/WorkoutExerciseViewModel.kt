package fr.cuber.fitcube.exercise.workoutedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.entity.WorkoutExercise
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutExerciseViewModel @Inject constructor(
    private val repository: RoomRepository

): ViewModel() {

    fun saveWorkoutExercise(ex: WorkoutExercise) = viewModelScope.launch {
        repository.saveWorkoutExercise(ex)
    }

    suspend fun getExercise(id: Int) = repository.getWorkoutExercise(id)

    fun deleteExercise(ex: WorkoutExercise) = viewModelScope.launch {
        repository.deleteWorkoutExercise(ex)
    }

}