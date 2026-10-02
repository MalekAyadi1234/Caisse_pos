package com.caisse.pos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.caisse.pos.data.model.PrintStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE printStatus IN (:statuses) ORDER BY createdAtEpochMs ASC")
    suspend fun getByPrintStatuses(statuses: List<PrintStatus>): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE synced = 0 ORDER BY createdAtEpochMs ASC")
    suspend fun getUnsynced(): List<SaleEntity>

    @Query("SELECT * FROM sales WHERE saleId = :saleId LIMIT 1")
    suspend fun getById(saleId: String): SaleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sale: SaleEntity)

    @Update
    suspend fun update(sale: SaleEntity)

    @Query("UPDATE sales SET printStatus = :status, printAttempts = printAttempts + 1 WHERE saleId = :saleId")
    suspend fun updatePrintStatus(saleId: String, status: PrintStatus)

    @Query("UPDATE sales SET synced = 1 WHERE saleId = :saleId")
    suspend fun markSynced(saleId: String)

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun count(): Int
}

@Dao
interface TicketCounterDao {

    @Query("SELECT * FROM ticket_counter WHERE id = 'local' LIMIT 1")
    suspend fun get(): TicketCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(counter: TicketCounterEntity)

    @Transaction
    suspend fun allocateNextSequence(): Long {
        val current = get() ?: TicketCounterEntity()
        val allocated = current.nextSequence
        upsert(current.copy(nextSequence = allocated + 1))
        return allocated
    }
}
