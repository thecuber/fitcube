package fr.cuber.fitcube.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fr.cuber.fitcube.data.db.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

@Query("SELECT * FROM sessions WHERE sessions.workoutId = :id")
fun getSessionsByWorkoutId(id: Int): Flow<List<Session>>

@Insert
suspend fun createSession(session: Session)

@Query("SELECT * FROM sessions")
fun getSessions(): Flow<List<Session>>

@Query("DELETE FROM sessions WHERE sessions.workoutId = :workoutId")
suspend fun deleteSession(workoutId: Int)

}