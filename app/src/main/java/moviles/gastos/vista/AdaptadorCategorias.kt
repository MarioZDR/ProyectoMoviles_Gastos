package moviles.gastos.vista

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import moviles.gastos.ActividadResumen
import moviles.gastos.R

class AdaptadorCategorias(private val context: Context, private val dataset: Array<String>) :
    RecyclerView.Adapter<AdaptadorCategorias.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tituloGasto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_gasto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = dataset[position]
        holder.textView.text = categoria

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
