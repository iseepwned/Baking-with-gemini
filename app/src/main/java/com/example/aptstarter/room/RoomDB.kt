package com.example.aptstarter.room
import ChatMessage
import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity
data class HistoryEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "history") val history: MutableList<ChatMessage>
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM HistoryEntity")
    fun getAll(): List<HistoryEntity>

    @Insert
    fun insert(historyEntity: HistoryEntity)
}

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)

abstract class RoomDB : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: RoomDB? = null

        fun getDatabase(context: Context): RoomDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
