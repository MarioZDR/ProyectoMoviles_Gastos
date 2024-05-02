package moviles.gastos.vista

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
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
    private val LIMITE_CIFRA_GRANDE = 1000 // Define tu límite aquí

    fun mostrar(categoriasList: List<String>, quitar: Boolean) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Agregar Gasto")
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_gasto, null)
        builder.setView(dialogView)

        val descripcionEditText = dialogView.findViewById<EditText>(R.id.descripcionEditText)
        val categoriaSpinner = dialogView.findViewById<Spinner>(R.id.categoriaSpinner)
        val totalEditText = dialogView.findViewById<EditText>(R.id.totalEditText)
        val btnOculto = dialogView.findViewById<Button>(R.id.botonAgregarCategoria)
        if (quitar) {
            btnOculto?.visibility = View.GONE
            categoriaSpinner.visibility = View.GONE
        }

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoriasList)
        categoriaSpinner.adapter = adapter
        categoriaSpinner.setSelection(0)

        builder.setPositiveButton("Confirmar") { dialog, _ ->
            val descripcion = descripcionEditText.text.toString().trim()
            val categoria = categoriaSpinner.selectedItem.toString()
            val totalText = totalEditText.text.toString()

            if (descripcion.length >= 3 && categoria.isNotBlank() && totalText.isNotBlank()) {
                val total = totalText.toFloatOrNull()
                if (total != null && total >= 0) {
                    if (total <= LIMITE_CIFRA_GRANDE) {
                        agregarGasto(descripcion, total, categoria)
                    } else {
                        mostrarConfirmacionCifraGrande(descripcion, total, categoria)
                    }
                } else {
                    Toast.makeText(context, "Por favor ingrese un total válido y no negativo", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "La descripción debe tener al menos 3 caracteres y todos los campos deben estar completos", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar", null)

        alertDialog = builder.create()
        alertDialog?.show()
    }

    private fun agregarGasto(descripcion: String, total: Float, categoria: String) {
        val gasto = Gasto(descripcion, total, categoria, System.currentTimeMillis())
        val gastoDao = BaseDatosGastos.getInstance(context).gastoDao
        GlobalScope.launch {
            gastoDao.agregarGasto(gasto)
            listener.onGastoAgregado()
        }
        Toast.makeText(context, "Gasto agregado correctamente", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarConfirmacionCifraGrande(descripcion: String, total: Float, categoria: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmación de cifra grande")
        builder.setMessage("Está seguro de que la cifra es correcta?")

        builder.setPositiveButton("Sí") { dialog, _ ->
            agregarGasto(descripcion, total, categoria)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->

            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
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
