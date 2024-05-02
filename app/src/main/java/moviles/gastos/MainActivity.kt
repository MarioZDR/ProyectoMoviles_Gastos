package moviles.gastos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.GastoDao
import moviles.gastos.vista.AdaptadorCategorias
import moviles.gastos.vista.AgregarCategoriaDialogo
import moviles.gastos.vista.AgregarGastoDialogo
import moviles.gastos.vista.CategoriaAgregadaListener
import moviles.gastos.vista.CategoriaEliminadaListener
import moviles.gastos.vista.GastoAgregadoListener

class MainActivity : AppCompatActivity(), CategoriaAgregadaListener,
    GastoAgregadoListener, SensorEventListener, CategoriaEliminadaListener {
    private var recyclerView: RecyclerView? = null
    private lateinit var comboBox: Spinner
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var gastoDao: GastoDao
    private var categoriaSeleccionadaLista: String = "Todas"
    private var agregarGastoDialogo: AgregarGastoDialogo = AgregarGastoDialogo(this, this)
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor

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
                    categoriaSeleccionadaLista = seleccion
                    actualizarVista()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        // Inicializar el SensorManager y el Sensor del acelerómetro
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    }

    override fun onResume() {
        super.onResume()
        // Registrar el listener del acelerómetro al reanudar la actividad
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        // Desregistrar el listener del acelerómetro al pausar la actividad
        sensorManager.unregisterListener(this)
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
                gastoDao.obtenerTotalGastos() // Manejar el caso cuando es null
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
        CoroutineScope(Dispatchers.IO).launch {
            val numeroGastos = gastoDao.obtenerMapaDeGastosPorCategoria()
            withContext(Dispatchers.Main) {
                val customAdapter = AdaptadorCategorias(this@MainActivity, categoriasList.toTypedArray(), numeroGastos, this@MainActivity)
                recyclerView!!.layoutManager = LinearLayoutManager(this@MainActivity)
                recyclerView!!.adapter = customAdapter
            }
        }
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
        cargarGastosCategoria(categoriasList)
        categoriasList.add(0,"Todas")
        categoriasList.remove(categoriaSeleccionadaLista)
        categoriasList.add(0, categoriaSeleccionadaLista)
        configurarSpinner(categoriasList)

        if(agregarGastoDialogo.estaAbierto()){
            agregarGastoDialogo.cerrarDialogo()
            agregarGastoDialogo.mostrar(obtenerCategoriasDesdeSharedPreferences(),false)
        }
    }

    override fun onCategoriaEliminada() {
        categoriaSeleccionadaLista = "Todas"
        val categoriasList = obtenerCategoriasDesdeSharedPreferences()
        cargarGastosCategoria(categoriasList)
        categoriasList.add(0,"Todas")
        configurarSpinner(categoriasList)
    }

    fun cargarGastosCategoria(categoriasList: MutableList<String>){
        if(categoriaSeleccionadaLista.equals("Todas")){
            cargarGastos(categoriasList)
        }else{
            val categorias = ArrayList<String>()
            categorias.add(categoriaSeleccionadaLista)
            cargarGastos(categorias)
        }
    }

    override fun onGastoAgregado() {
        actualizarTotalGastos(categoriaSeleccionadaLista)
        cargarGastosCategoria(obtenerCategoriasDesdeSharedPreferences())
        cerrarDialogoAgregarGasto()
    }

    fun mostrarDialogoAgregarGasto(view: View?) {
        if(!agregarGastoDialogo.estaAbierto()){
            agregarGastoDialogo.mostrar(obtenerCategoriasDesdeSharedPreferences(),false)
        }
    }

    fun cerrarDialogoAgregarGasto() {
        agregarGastoDialogo.cerrarDialogo()
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Manejar cambios de precisión si es necesario
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val xAxis = event.values[0]
            val yAxis = event.values[1]
            val zAxis = event.values[2]

            // Detectar inclinación hacia atrás
            if (yAxis < -8.0f) { // Ajusta este umbral según sea necesario
                mostrarDialogoAgregarGasto(view = null)
            }
        }
    }
}


