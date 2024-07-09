package fr.cuber.fitcube.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.cuber.fitcube.data.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "fitcube_database"
    ).createFromAsset("db/database.db").build()

    @Singleton
    @Provides
    fun provideExerciseDao(db: AppDatabase) =
        db.getExerciseDao() // The reason we can implement a Dao for the database

    @Singleton
    @Provides
    fun provideWorkoutDao(db: AppDatabase) =
        db.getWorkoutDao() // The reason we can implement a Dao for the database

    @Singleton
    @Provides
    fun provideSessionDao(db: AppDatabase) =
        db.getSessionDao()

}