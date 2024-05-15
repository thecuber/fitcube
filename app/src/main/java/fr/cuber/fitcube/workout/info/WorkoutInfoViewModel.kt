package fr.cuber.fitcube.workout.info

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import javax.inject.Inject

@HiltViewModel
class WorkoutInfoViewModel @Inject constructor(
    private val repository: RoomRepository
): ViewModel() {

    fun getWorkout(workoutId: Int) = repository.getWorkout(workoutId)

    fun getSessions(workoutId: Int) = repository.getSessions(workoutId)


}