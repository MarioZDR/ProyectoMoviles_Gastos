package moviles.gastos.vista

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moviles.gastos.ActividadResumen
import moviles.gastos.R
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.GastoDao

class AdaptadorCategorias(private val context: Context, private val dataset: Array<String>,
    private val numeroGastos: Map<String, Int>) :
    RecyclerView.Adapter<AdaptadorCategorias.ViewHolder>() {

    class ViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tituloGasto)
        val gastoDao: GastoDao = BaseDatosGastos.getInstance(context).gastoDao
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_gasto, parent, false)
        return ViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = dataset[position]
        val texto = "$categoria - ${numeroGastos[categoria] ?: 0} gasto(s)"
        holder.textView.text = texto
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActividadResumen::class.java)
            intent.putExtra("categoriaSeleccionada", categoria)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}
