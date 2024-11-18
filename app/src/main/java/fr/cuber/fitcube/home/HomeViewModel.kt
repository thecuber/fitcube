package fr.cuber.fitcube.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {
    fun getWorkouts() = repository.getWorkouts().success()

    fun addWorkout(name: String) {
        viewModelScope.launch {
            repository.addWorkout(
                Workout(
                    0,
                    name,
                    false
                )
            )
        }
    }

    fun getExercises() = repository.getExerciseTypes().success()

    fun getSessions() = repository.getSessions().success()
    suspend fun deleteWorkout(workoutId: Int) = viewModelScope.launch {
        delay(250)
        repository.deleteWorkout(workoutId)
    }

}