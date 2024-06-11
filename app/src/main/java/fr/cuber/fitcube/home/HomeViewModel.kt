package fr.cuber.fitcube.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.dao.HomeWorkout
import fr.cuber.fitcube.data.db.entity.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RoomRepository
) : ViewModel() {
    fun getWorkouts(): Flow<List<HomeWorkout>> {
        return repository.getWorkouts()
    }

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
}