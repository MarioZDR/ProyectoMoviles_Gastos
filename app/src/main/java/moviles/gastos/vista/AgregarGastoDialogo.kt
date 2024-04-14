package moviles.gastos.vista

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import moviles.gastos.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.Gasto

class AgregarGastoDialogo(private val context: Context, private val listener: GastoAgregadoListener) {
    private var alertDialog: AlertDialog? = null

        fun mostrar(categoriasList: List<String>){
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Agregar Gasto")
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_gasto, null)
            builder.setView(dialogView)

            val descripcionEditText = dialogView.findViewById<EditText>(R.id.descripcionEditText)
            val categoriaSpinner = dialogView.findViewById<Spinner>(R.id.categoriaSpinner)
            val totalEditText = dialogView.findViewById<EditText>(R.id.totalEditText)

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoriasList)
            categoriaSpinner.adapter = adapter
            categoriaSpinner.setSelection(0)

            builder.setPositiveButton("Confirmar") { dialog, _ ->
                val descripcion = descripcionEditText.text.toString().trim()
                val categoria = categoriaSpinner.selectedItem.toString()
                val total = totalEditText.text.toString().toFloatOrNull()

                if (descripcion.isNotBlank() && total != null && categoria.isNotBlank()) {
                    val gasto = Gasto(descripcion, total, categoria, System.currentTimeMillis())
                    val gastoDao = BaseDatosGastos.getInstance(context).gastoDao
                    GlobalScope.launch {
                        gastoDao.agregarGasto(gasto)
                    }
                    listener.onGastoAgregado()
                    Toast.makeText(context, "Gasto agregado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Por favor ingrese una descripción, una categoría y un total válidos", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancelar", null)

            alertDialog = builder.create()
            alertDialog?.show()
        }

    fun obtenerAlertDialog(): AlertDialog? {
        return alertDialog
    }

    fun estaAbierto(): Boolean {
        return alertDialog?.isShowing ?: false
    }

    fun cerrarDialogo() {
        alertDialog?.dismiss()
    }

}