package fr.cuber.fitcube.workout.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.success
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import javax.inject.Inject

@HiltViewModel
class WorkoutInfoViewModel @Inject constructor(
    private val repository: RoomRepository
): ViewModel() {

    fun getWorkout(workoutId: Int) = repository.getWorkout(workoutId).success()

    fun getSessions(workoutId: Int) = repository.getSessions(workoutId).success()

    fun deleteExercises(exerciseIds: List<Int>) = viewModelScope.launch {
        repository.deleteExercises(exerciseIds)
    }

    fun deleteWorkout(workoutId: Int) = viewModelScope.launch {
        repository.deleteWorkout(workoutId)
    }

    fun updateWarmup(workoutId: Int, it: Int) = viewModelScope.launch {
        repository.updateWorkoutWarmup(workoutId, it)
    }

    fun moveOrder(order: List<Int>, id: Int) = viewModelScope.launch {
        repository.moveOrder(order, id)
    }


}