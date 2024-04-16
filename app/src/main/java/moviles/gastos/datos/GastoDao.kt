package moviles.gastos.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
@Dao
interface GastoDao {

    @Upsert
    suspend fun agregarGasto(gasto:Gasto)

    @Delete
    suspend fun eliminarGasto(gasto:Gasto)

    @Query("SELECT * FROM gasto WHERE categoria = :categoriaBuscar")
    fun obtenerGastosPorCategoria(categoriaBuscar: String): Flow<List<Gasto>>

    @Query("SELECT COALESCE(SUM(total),0) FROM gasto")
    suspend fun obtenerTotalGastos(): Float

    @Query("SELECT COALESCE(SUM(total), 0) FROM gasto WHERE categoria=:categoriaBuscar")
    suspend fun obtenerTotalGastosPorCategoria(categoriaBuscar: String): Float
}