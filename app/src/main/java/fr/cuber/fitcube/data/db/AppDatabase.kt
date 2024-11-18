package fr.cuber.fitcube.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import fr.cuber.fitcube.data.db.dao.ExerciseDao
import fr.cuber.fitcube.data.db.dao.SessionDao
import fr.cuber.fitcube.data.db.dao.WorkoutDao
import fr.cuber.fitcube.data.db.entity.ExerciseType
import fr.cuber.fitcube.data.db.entity.Session
import fr.cuber.fitcube.data.db.entity.Workout
import fr.cuber.fitcube.data.db.entity.WorkoutExercise

@Database(
    entities = [ExerciseType::class, Workout::class, WorkoutExercise::class, Session::class],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 5, to = 6, spec = AppDatabase.DeleteWarmupMigration::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    @DeleteColumn(tableName = "workouts", columnName = "warmup")
    class DeleteWarmupMigration: AutoMigrationSpec

    abstract fun getExerciseDao(): ExerciseDao
    abstract fun getWorkoutDao(): WorkoutDao

    abstract fun getSessionDao(): SessionDao

}