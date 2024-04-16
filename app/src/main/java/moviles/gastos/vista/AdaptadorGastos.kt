package moviles.gastos.vista

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import moviles.gastos.R
import moviles.gastos.datos.Gasto
import moviles.gastos.utilerias.FormateadorFechas

class AdaptadorGastos(
    private val lifecycleOwner: LifecycleOwner,
    val listaGastosFlow: MutableStateFlow<List<Gasto>>
) : RecyclerView.Adapter<AdaptadorGastos.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionGasto)
        val totalTextView: TextView = itemView.findViewById(R.id.totalGasto)
        val fechaTextView: TextView = itemView.findViewById(R.id.fechaGasto)
        val eliminarButton: Button = itemView.findViewById(R.id.eliminarGasto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_gasto_detalle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gasto = listaGastosFlow.value[position]

        holder.descripcionTextView.text = gasto.descripcion
        holder.totalTextView.text = gasto.total.toString()
        holder.fechaTextView.text = FormateadorFechas.convertirMilisegundosAFecha(gasto.fecha)

        holder.eliminarButton.setOnClickListener {
            // Aquí puedes manejar la acción de eliminar el gasto
            // Puedes utilizar la posición del gasto o el ID del gasto para identificarlo
        }
    }

    override fun getItemCount(): Int {
        return listaGastosFlow.value.size
    }

    init {
        lifecycleOwner.lifecycleScope.launchWhenStarted {
            listaGastosFlow.collect { newGastos ->
                notifyDataSetChanged()
            }
        }
    }
}