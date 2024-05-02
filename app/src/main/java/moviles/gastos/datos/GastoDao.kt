package moviles.gastos.datos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
@Dao
interface GastoDao {

    @Upsert
    suspend fun agregarGasto(gasto:Gasto)

    @Delete
    suspend fun eliminarGasto(gasto:Gasto)

    @Update
    suspend fun editarGasto(gasto: Gasto)

    @Query("SELECT * FROM gasto WHERE categoria = :categoriaBuscar")
    fun obtenerGastosPorCategoria(categoriaBuscar: String): Flow<List<Gasto>>

    @Query("SELECT COALESCE(SUM(total),0) FROM gasto")
    suspend fun obtenerTotalGastos(): Float

    @Query("SELECT COALESCE(SUM(total), 0) FROM gasto WHERE categoria=:categoriaBuscar")
    suspend fun obtenerTotalGastosPorCategoria(categoriaBuscar: String): Float

    data class CategoriaConteo(val categoria: String, val conteo: Int)

    @Query("SELECT categoria, COUNT(*) AS conteo FROM gasto GROUP BY categoria")
    suspend fun obtenerNumeroGastosPorCategoria(): List<CategoriaConteo>

    suspend fun obtenerMapaDeGastosPorCategoria(): Map<String, Int> {
        return obtenerNumeroGastosPorCategoria().associateBy({ it.categoria }, { it.conteo })
    }
}