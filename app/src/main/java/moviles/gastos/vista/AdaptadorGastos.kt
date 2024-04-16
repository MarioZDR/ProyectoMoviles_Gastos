package moviles.gastos.vista

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moviles.gastos.R
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.Gasto
import moviles.gastos.datos.GastoDao
import moviles.gastos.utilerias.FormateadorFechas

class AdaptadorGastos(
    private val lifecycleOwner: LifecycleOwner,
    val listaGastosFlow: MutableStateFlow<List<Gasto>>,
    private val context: Context
) : RecyclerView.Adapter<AdaptadorGastos.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descripcionTextView: TextView = itemView.findViewById(R.id.descripcionGasto)
        val totalTextView: TextView = itemView.findViewById(R.id.totalGasto)
        val fechaTextView: TextView = itemView.findViewById(R.id.fechaGasto)
        val eliminarButton: Button = itemView.findViewById(R.id.eliminarGasto)
        val gastoDao = BaseDatosGastos.getInstance(context).gastoDao
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
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Confirmar eliminación")
            builder.setMessage("¿Estás seguro de que deseas eliminar este gasto?")
            builder.setPositiveButton("Eliminar") { dialog, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    holder.gastoDao.eliminarGasto(gasto)
                }
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
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