package moviles.gastos.datos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Gasto(

    val descripcion: String,
    val total: Float,
    val categoria: String,
    val fecha: Long,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)