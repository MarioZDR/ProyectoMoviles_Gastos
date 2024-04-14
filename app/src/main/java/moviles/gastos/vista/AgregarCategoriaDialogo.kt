package moviles.gastos.vista

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import java.util.HashSet
import moviles.gastos.R

class AgregarCategoriaDialogo(private val context: Context, private val listener: CategoriaAgregadaListener) {

    companion object {
        @JvmStatic
        fun mostrar(context: Context, sharedPreferences: SharedPreferences?, listener: CategoriaAgregadaListener) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Agregar Categoría")
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_categoria, null)
            builder.setView(dialogView)

            builder.setPositiveButton("Confirmar") { dialog, _ ->
                val editText = dialogView.findViewById<EditText>(R.id.editText)
                val categoria = editText.text.toString().trim()

                if (categoria.isNotEmpty()) {
                    val categorias: MutableSet<String> = HashSet(sharedPreferences!!.getStringSet("categorias", HashSet()))
                    categorias.add(categoria)
                    sharedPreferences!!.edit().putStringSet("categorias", categorias).apply()
                    listener.onCategoriaAgregada()
                } else {
                    Toast.makeText(context, "Por favor ingrese una categoría válida", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }
    }
}
