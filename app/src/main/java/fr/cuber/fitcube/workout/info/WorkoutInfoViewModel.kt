package fr.cuber.fitcube.workout.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.success
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutInfoViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {

    fun getWorkout(workoutId: Int) = repository.getWorkout(workoutId).success()

    fun getSessions(workoutId: Int) = repository.getSessions(workoutId).success()

    fun moveOrder(order: List<Int>, id: Int) = viewModelScope.launch {
        repository.moveOrder(order, id)
    }

    fun archiveExercise(id: Int, archived: Boolean) = viewModelScope.launch {
        repository.archiveExercise(id, archived)
    }


}