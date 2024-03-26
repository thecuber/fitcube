package fr.cuber.fitcube.old.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import fr.cuber.fitcube.R
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import java.util.Date

class Converters {
    @TypeConverter
    fun fromLong(value : List<Long>) = Json.encodeToString(value)

    @TypeConverter
    fun toLong(value: String) = Json.decodeFromString<List<Long>>(value)

    @TypeConverter
    fun fromDate(value : Date) = value.toString()

    @TypeConverter
    fun toDate(value: String) = Date(value)

    @TypeConverter
    fun fromInt(value : List<Int>) = Json.encodeToString(value)

    @TypeConverter
    fun toInt(value: String) = Json.decodeFromString<List<Int>>(value)

    @TypeConverter
    fun fromMap(value : Map<Int, List<Long>>) = Json.encodeToString(value)

    @TypeConverter
    fun toMap(value: String) = Json.decodeFromString<Map<Int, List<Long>>>(value)
}

@Database(entities = [WorkoutDay::class, WorkoutHistory::class, BaseExercise::class, WorkoutExercise::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDAO(): WorkoutDAO
    abstract fun exerciseDAO(): ExerciseDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitcube-db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val file = context.resources.openRawResource(R.raw.exercises)
                            val exercises = JSONArray(file.bufferedReader().use { it.readText() })
                            for(i in (0..<exercises.length())) {
                                val exercise = exercises.getJSONObject(i)
                                db.execSQL("INSERT INTO exercises (uid, name, description) VALUES (${exercise.getString("id")}, '${exercise.getString("name").replace("'", " ")}', '${exercise.getString("description").replace("'", " ")}')")
                            }
                        }
                    })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
