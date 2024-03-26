package fr.cuber.fitcube.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.db.RoomRepository
import fr.cuber.fitcube.db.entity.Workout
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RoomRepository
): ViewModel() {
    fun getWorkouts() = repository.getWorkouts()
    fun addWorkout(name: String) {
        viewModelScope.launch {
            repository.addWorkout(Workout(
                0,
                name,
                lastSession = null
            ))
        }
    }
}