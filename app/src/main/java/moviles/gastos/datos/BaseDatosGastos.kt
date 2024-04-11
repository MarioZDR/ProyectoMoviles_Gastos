package moviles.gastos.datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [Gasto::class], version = 1)
abstract class BaseDatosGastos : RoomDatabase() {

    abstract val gastoDao: GastoDao
    companion object {
        private var INSTANCE: BaseDatosGastos? = null
        fun getInstance(context: Context): BaseDatosGastos {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDatosGastos::class.java,
                    "gastos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}