package fr.cuber.fitcube.workout.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutInfoViewModel @Inject constructor(
    private val repository: RoomRepository
): ViewModel() {

    fun getWorkout(workoutId: Int) = repository.getWorkout(workoutId)

    fun getSessions(workoutId: Int) = repository.getSessions(workoutId)

    fun deleteExercises(exerciseIds: List<Int>) = viewModelScope.launch {
        repository.deleteExercises(exerciseIds)
    }

    fun deleteWorkout(workoutId: Int) = viewModelScope.launch {
        repository.deleteWorkout(workoutId)
    }


}