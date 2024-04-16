package moviles.gastos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.GastoDao
import moviles.gastos.vista.AdaptadorGastos
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.MutableStateFlow
import moviles.gastos.datos.Gasto
import moviles.gastos.vista.AdaptadorCategorias

class ActividadResumen : AppCompatActivity() {

    private lateinit var gastoDao: GastoDao
    private lateinit var categoria: String
    private var recyclerView: RecyclerView? = null
    private lateinit var adaptador: AdaptadorGastos
    private var listaGastosFlow: MutableStateFlow<List<Gasto>> = MutableStateFlow(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actividad_resumen)

        recyclerView = findViewById(R.id.recyclerView)
        gastoDao = BaseDatosGastos.getInstance(this).gastoDao
        categoria = intent.getStringExtra("categoriaSeleccionada") ?: "Todas"

        adaptador = AdaptadorGastos(this, listaGastosFlow)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adaptador

        obtenerListaGastos(categoria)
    }

    private fun obtenerListaGastos(categoriaSeleccionada: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val listaGastos = gastoDao.obtenerGastosPorCategoria(categoriaSeleccionada)
            gastoDao.obtenerGastosPorCategoria(categoriaSeleccionada).collect { listaGastos ->
                listaGastosFlow.value = listaGastos
            }
        }
    }

    fun openMainActivity(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}