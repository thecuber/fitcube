package fr.cuber.fitcube.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrainingSet::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDAO(): TrainingSetDAO

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
                            println("Creating database")
                            db.execSQL("INSERT INTO training_sets (uid, name, color) VALUES (1, 'Full Body', '#FF0000')")
                            db.execSQL("INSERT INTO training_sets (uid, name, color) VALUES (2, 'Upper Body', '#00FF00')")
                            db.execSQL("INSERT INTO training_sets (uid, name, color) VALUES (3, 'Lower Body', '#0000FF')")
                        }
                    })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
