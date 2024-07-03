package fr.cuber.fitcube.exercise.type.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cuber.fitcube.data.db.RoomRepository
import fr.cuber.fitcube.data.db.entity.ExerciseType
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseTypeEditViewModel @Inject constructor(
    private val repository: RoomRepository
): ViewModel() {

    fun getExerciseType(id: Int) = repository.getExerciseType(id)

    fun updateImages(image: List<String>, id: Int) {
        viewModelScope.launch {
            repository.updateImages(image, id)
        }
    }

    fun updateExercise(type: ExerciseType) {
        viewModelScope.launch {
            repository.updateExerciseType(type)
        }
    }

    suspend fun createExerciseType(type: ExerciseType) = repository.createExerciseType(type)

}