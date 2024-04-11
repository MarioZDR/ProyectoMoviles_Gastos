package moviles.gastos

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var comboBox: Spinner? = null
    private var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        comboBox = findViewById(R.id.comboBox)
        sharedPreferences = getSharedPreferences("categorias", MODE_PRIVATE)
        val categoriasList = obtenerCategoriasDesdeSharedPreferences()
        cargarGastos(categoriasList)
        categoriasList.add(0,"Todas")
        configurarSpinner(categoriasList)
    }

    private fun cargarGastos(categoriasList: List<String>) {
        val customAdapter = CustomAdapter(categoriasList.toTypedArray<String>())
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = customAdapter
    }

    private fun configurarSpinner(categoriasList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoriasList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        comboBox!!.adapter = adapter
    }

    private fun obtenerCategoriasDesdeSharedPreferences(): MutableList<String> {
        val categoriasString = sharedPreferences!!.getStringSet("categorias", HashSet())
        return ArrayList(categoriasString)
    }

    fun openNewActivity(view: View?) {
        val intent = Intent(this, ActividadResumen::class.java)
        startActivity(intent)
    }

    fun mostrarDialogoAgregarCategoria(view: View?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar Categoría")
        val dialogView = layoutInflater.inflate(R.layout.dialog_layout, null)
        builder.setView(dialogView)
        builder.setPositiveButton("Confirmar") { dialog, _ ->
            val editText = dialogView.findViewById<EditText>(R.id.editText)
            val categoria = editText.text.toString().trim()

            if (categoria.isNotEmpty()) {
                val categorias: MutableSet<String> = HashSet(sharedPreferences!!.getStringSet("categorias", HashSet()))
                categorias.add(categoria)
                sharedPreferences!!.edit().putStringSet("categorias", categorias).apply()

                val categoriasList = obtenerCategoriasDesdeSharedPreferences()
                cargarGastos(categoriasList)
                categoriasList.add(0,"Todas")
                configurarSpinner(categoriasList)
            } else {
                Toast.makeText(this, "Por favor ingrese una categoría válida", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }
}
