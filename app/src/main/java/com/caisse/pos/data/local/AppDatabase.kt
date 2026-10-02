package com.caisse.pos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.caisse.pos.data.model.PrintStatus

class Converters {
    @TypeConverter
    fun fromPrintStatus(status: PrintStatus): String = status.name

    @TypeConverter
    fun toPrintStatus(value: String): PrintStatus = PrintStatus.valueOf(value)
}

@Database(
    entities = [SaleEntity::class, TicketCounterEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
    abstract fun ticketCounterDao(): TicketCounterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "caisse_pos.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
