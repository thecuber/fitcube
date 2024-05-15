package fr.cuber.fitcube.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import fr.cuber.fitcube.data.db.dao.ExerciseDao
import fr.cuber.fitcube.data.db.dao.SessionDao
import fr.cuber.fitcube.data.db.dao.WorkoutDao
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise

@Database(
    entities = [ExerciseType::class, Workout::class, WorkoutExercise::class, Session::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getExerciseDao(): ExerciseDao
    abstract fun getWorkoutDao(): WorkoutDao

    abstract fun getSessionDao(): SessionDao


}