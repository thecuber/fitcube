package fr.cuber.fitcube.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.cuber.fitcube.db.AppDatabase
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
    ).build()

    @Singleton
    @Provides
    fun provideExerciseDao(db: AppDatabase) = db.getExerciseDao() // The reason we can implement a Dao for the database

    @Singleton
    @Provides
    fun provideWorkoutDao(db: AppDatabase) = db.getWorkoutDao() // The reason we can implement a Dao for the database
}