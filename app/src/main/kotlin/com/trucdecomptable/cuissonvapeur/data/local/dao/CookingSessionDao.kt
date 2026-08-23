package com.trucdecomptable.cuissonvapeur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity
import kotlinx.coroutines.flow.Flow

/** §12.2: persistence backing reconstruction of the active cooking session. */
@Dao
interface CookingSessionDao {

    @Query("SELECT * FROM cooking_session WHERE id = ${CookingSessionEntity.SESSION_ROW_ID}")
    fun observe(): Flow<CookingSessionEntity?>

    @Query("SELECT * FROM cooking_session WHERE id = ${CookingSessionEntity.SESSION_ROW_ID}")
    suspend fun getOnce(): CookingSessionEntity?

    @Upsert
    suspend fun upsert(session: CookingSessionEntity)

    /** EF-21 "Arrêt": clears the active session entirely. */
    @Query("DELETE FROM cooking_session")
    suspend fun clear()
}
