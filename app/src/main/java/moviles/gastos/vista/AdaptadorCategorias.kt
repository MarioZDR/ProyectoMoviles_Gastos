package moviles.gastos.vista

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moviles.gastos.ActividadResumen
import moviles.gastos.R
import moviles.gastos.datos.BaseDatosGastos
import moviles.gastos.datos.GastoDao
import java.util.HashSet

class AdaptadorCategorias(private val context: Context, private val dataset: Array<String>,
    private val numeroGastos: Map<String, Int>,private val listener: CategoriaEliminadaListener ) :
    RecyclerView.Adapter<AdaptadorCategorias.ViewHolder>() {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("categorias", Context.MODE_PRIVATE)
    class ViewHolder(view: View, private val context: Context) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tituloGasto)
        val gastoDao: GastoDao = BaseDatosGastos.getInstance(context).gastoDao
        val eliminarButton: Button = itemView.findViewById(R.id.borrarCategoria)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_gasto, parent, false)
        return ViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = dataset[position]
        val texto = "$categoria - ${numeroGastos[categoria] ?: 0} gasto(s)"
        val editor = sharedPreferences.edit()

        holder.textView.text = texto
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActividadResumen::class.java)
            intent.putExtra("categoriaSeleccionada", categoria)
            context.startActivity(intent)
        }

        holder.eliminarButton.setOnClickListener{
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Confirmar eliminación")
            builder.setMessage("¿Estás seguro de que deseas eliminar esta categoría?")
            builder.setPositiveButton("Eliminar") { dialog, _ ->
            val numGastos = numeroGastos[categoria] ?: 0

                if (numGastos == 0){
                    val categorias: MutableSet<String> = HashSet(sharedPreferences!!.getStringSet("categorias", HashSet()))
                    categorias.remove(categoria)
                    sharedPreferences!!.edit().putStringSet("categorias", categorias).apply()
                    Toast.makeText(context, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                    listener.onCategoriaEliminada()
                    dialog.dismiss()
                }else{
                    Toast.makeText(context, "La categoría no tiene que tener gastos", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }


}
