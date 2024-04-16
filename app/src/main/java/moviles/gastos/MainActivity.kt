package moviles.gastos

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.GastoDao
import moviles.gastos.vista.AgregarCategoriaDialogo
import moviles.gastos.vista.AgregarGastoDialogo
import moviles.gastos.vista.CategoriaAgregadaListener
import moviles.gastos.vista.GastoAgregadoListener

class MainActivity : AppCompatActivity(), CategoriaAgregadaListener, GastoAgregadoListener {
    private var recyclerView: RecyclerView? = null
    private lateinit var comboBox: Spinner
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var gastoDao: GastoDao
    private var categoriaSeleccionadaLista: String = "Todas"
    private var agregarGastoDialogo: AgregarGastoDialogo = AgregarGastoDialogo(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.recyclerView)
        comboBox = findViewById(R.id.comboBox)
        sharedPreferences = getSharedPreferences("categorias", MODE_PRIVATE)
        gastoDao = BaseDatosGastos.getInstance(this).gastoDao

        val categoriasList = obtenerCategoriasDesdeSharedPreferences()
        categoriasList.add(0,"Todas")

        configurarSpinner(categoriasList)

        actualizarVista()

        comboBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val seleccion = comboBox.getItemAtPosition(position)?.toString() ?: "Ninguna"
                if(!seleccion.equals("Ninguna")){
                    categoriaSeleccionadaLista = seleccion;
                    actualizarVista()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun actualizarVista(){
        actualizarTotalGastos(categoriaSeleccionadaLista)

        val categoriasList = obtenerCategoriasDesdeSharedPreferences()

        if(categoriaSeleccionadaLista.equals("Todas")){
            cargarGastos(categoriasList)
        }else{
            val categorias = ArrayList<String>()
            categorias.add(categoriaSeleccionadaLista)
            cargarGastos(categorias)
        }
    }

    fun actualizarTotalGastos(categoriaSeleccionada: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val total: Float = if (categoriaSeleccionada == "Todas") {
                gastoDao.obtenerTotalGastos() ?: 0f // Manejar el caso cuando es null
            } else {
                gastoDao.obtenerTotalGastosPorCategoria(categoriaSeleccionada) ?: 0f // Manejar el caso cuando es null
            }

            runOnUiThread {
                val textView = findViewById<TextView>(R.id.tvTotalGastos)
                textView.text = "Total de gastos: $$total"
            }
        }
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
        AgregarCategoriaDialogo.mostrar(this, sharedPreferences, this)
    }

    override fun onCategoriaAgregada() {
        val categoriasList = obtenerCategoriasDesdeSharedPreferences()

        if(categoriaSeleccionadaLista.equals("Todas")){
            cargarGastos(categoriasList)
        }else{
            val categorias = ArrayList<String>()
            categorias.add(categoriaSeleccionadaLista)
            cargarGastos(categorias)
        }
        categoriasList.add(0,"Todas")
        categoriasList.remove(categoriaSeleccionadaLista)
        categoriasList.add(0, categoriaSeleccionadaLista)
        configurarSpinner(categoriasList)

        if(agregarGastoDialogo.estaAbierto()){
            agregarGastoDialogo.cerrarDialogo()
            agregarGastoDialogo.mostrar(obtenerCategoriasDesdeSharedPreferences())
        }
    }

    override fun onGastoAgregado() {
        actualizarTotalGastos(categoriaSeleccionadaLista)
    }

    fun mostrarDialogoAgregarGasto(view: View?) {
        agregarGastoDialogo.mostrar(obtenerCategoriasDesdeSharedPreferences())
    }

}
