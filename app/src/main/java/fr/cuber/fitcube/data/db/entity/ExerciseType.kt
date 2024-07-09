package fr.cuber.fitcube.data.db.entity

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

@Entity(tableName = "exercise_types")
data class ExerciseType(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val description: String,
    var image: List<String>
)

fun defaultExerciseType(id: Int) = ExerciseType(
    id = id,
    name = "Exercise $id",
    description = "Description for exercise $id",
    image = listOf()
)

fun voidExerciseType() = ExerciseType(
    id = 0,
    name = "",
    description = "",
    image = emptyList()
)

fun ExerciseType.imagePreview(): String {
    return if (image.isNotEmpty()) {
        image[0]
    } else {
        ""
    }
}

fun ExerciseType.imagePagerPreview(index: Int): String {
    return if (image.isNotEmpty() && index < image.size) {
        image[index]
    } else {
        ""
    }
}

fun imageStream(image: String, context: Context): InputStream? {
    if(image.isEmpty()) return null
    val type = image.subSequence(0, 3)
    val fileName = image.substring(4)
    val file = when (type) {
        "png" -> {
            context.assets.open("images/$fileName")
        }
        "fld" -> {
            FileInputStream(
                File(context.getDir("images", Context.MODE_PRIVATE), fileName)
            )
        }
        else -> {
            null
        }
    }
    return file
}