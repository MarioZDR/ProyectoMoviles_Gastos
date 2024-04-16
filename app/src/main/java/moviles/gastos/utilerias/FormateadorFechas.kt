package moviles.gastos.utilerias

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormateadorFechas {
    companion object {
        fun convertirMilisegundosAFecha(fechaEnMilisegundos: Long): String {
            val formato = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(formato, Locale.getDefault())
            val fecha = Date(fechaEnMilisegundos)
            return sdf.format(fecha)
        }
    }
}