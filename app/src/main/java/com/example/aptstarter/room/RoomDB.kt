/*
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
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
@TypeConverters(ChatMessageListConverter::class)
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


class ChatMessageListConverter {
    @TypeConverter
    fun fromChatMessageList(value: MutableList<ChatMessage>): String {
        // Convertir la lista a un formato String que puedas almacenar en la base de datos
        // En este ejemplo, se convierte a JSON usando Gson (necesitarías la dependencia de Gson)
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toChatMessageList(value: String): MutableList<ChatMessage> {
        // Convertir la cadena almacenada en la base de datos de nuevo en MutableList<ChatMessage>
        return Gson().fromJson(value, object : TypeToken<MutableList<ChatMessage>>() {}.type)
    }
}
*/
