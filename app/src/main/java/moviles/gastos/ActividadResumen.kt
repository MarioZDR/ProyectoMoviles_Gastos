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
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.GastoDao
import moviles.gastos.vista.AdaptadorGastos
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.MutableStateFlow
import moviles.gastos.datos.Gasto
import com.google.android.material.floatingactionbutton.FloatingActionButton
import moviles.gastos.vista.AgregarGastoDialogo
import moviles.gastos.vista.GastoAgregadoListener
import moviles.gastos.vista.GastoEliminadoListener

class ActividadResumen : AppCompatActivity(), GastoAgregadoListener, GastoEliminadoListener,
    SensorEventListener {

    private lateinit var gastoDao: GastoDao
    private lateinit var categoria: String
    private var recyclerView: RecyclerView? = null
    private lateinit var btnRegreso: FloatingActionButton
    private lateinit var  btnAgregaGasto: FloatingActionButton
    private lateinit var  tituloCategoria: TextView
    private lateinit var adaptador: AdaptadorGastos
    private var sharedPreferences: SharedPreferences? = null
    private var listaGastosFlow: MutableStateFlow<List<Gasto>> = MutableStateFlow(emptyList())
    private var agregarGastoDialogo: AgregarGastoDialogo = AgregarGastoDialogo(this, this)
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_resumen)

        sharedPreferences = getSharedPreferences("categorias", MODE_PRIVATE)
        recyclerView = findViewById(R.id.recyclerView)
        gastoDao = BaseDatosGastos.getInstance(this).gastoDao
        categoria = intent.getStringExtra("categoriaSeleccionada") ?: "Todas"
        tituloCategoria = findViewById(R.id.tvGastosHormiga)
        tituloCategoria.text = categoria

            btnRegreso = findViewById<FloatingActionButton>(R.id.btnRegresar)
            btnRegreso.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            btnAgregaGasto = findViewById<FloatingActionButton>(R.id.btnAgregarGasto)
            btnAgregaGasto.setOnClickListener{
                mostrarDialogoAgregarGasto()
            }

        adaptador = AdaptadorGastos(this, listaGastosFlow, this, this)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adaptador

        obtenerListaGastos(categoria)

        actualizarTotalGastos()

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
                mostrarDialogoAgregarGasto()
            }
        }
    }

    private fun obtenerListaGastos(categoriaSeleccionada: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val listaGastos = gastoDao.obtenerGastosPorCategoria(categoriaSeleccionada)
            gastoDao.obtenerGastosPorCategoria(categoriaSeleccionada).collect { listaGastos ->
                listaGastosFlow.value = listaGastos
            }
        }
    }

    override fun onGastoAgregado() {
        actualizarTotalGastos()
    }

    override fun onGastoEliminado() {
        actualizarTotalGastos()
    }

    fun mostrarDialogoAgregarGasto() {
        if(!agregarGastoDialogo.estaAbierto()){
            val categoriaActual = ArrayList<String>()
            categoriaActual.add(categoria)
            agregarGastoDialogo.mostrar(categoriaActual, true)
        }
    }


    fun actualizarTotalGastos() {
        CoroutineScope(Dispatchers.IO).launch {
            val total: Float = gastoDao.obtenerTotalGastosPorCategoria(categoria) ?: 0f // Manejar el caso cuando es null
            runOnUiThread {
                val textView = findViewById<TextView>(R.id.tvTotalGastos)
                textView.text = "Total de gastos: $$total"
            }
        }
    }
}