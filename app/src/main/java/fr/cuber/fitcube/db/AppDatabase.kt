package fr.cuber.fitcube.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.cuber.fitcube.db.dao.ExerciseDao
import fr.cuber.fitcube.db.dao.WorkoutDao
import fr.cuber.fitcube.db.entity.ExerciseType
import fr.cuber.fitcube.db.entity.Workout

@Database(
    entities = [ExerciseType::class, Workout::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getExerciseDao(): ExerciseDao
    abstract fun getWorkoutDao(): WorkoutDao

}