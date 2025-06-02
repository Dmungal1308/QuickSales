package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.databinding.ItemChatSessionBinding
import com.iesvdc.acceso.quicksales.ui.modelview.ChatRecopiladosViewModel

/**
 * Adaptador de RecyclerView para mostrar sesiones de chat agrupadas por producto.
 *
 * @property items Lista de elementos de tipo [ChatRecopiladosViewModel.Item] que contienen
 *                  información del producto y la sesión.
 * @property onClick Lambda que se invoca cuando el usuario pulsa sobre un elemento de la lista;
 *                   recibe el [ChatRecopiladosViewModel.Item] seleccionado.
 */
class ChatSessionsAdapter(
    private var items: List<ChatRecopiladosViewModel.Item>,
    private val onClick: (ChatRecopiladosViewModel.Item) -> Unit
) : RecyclerView.Adapter<ChatSessionsAdapter.VH>() {

    /**
     * ViewHolder que contiene la vista de cada sesión de chat en la lista.
     *
     * @property b Binding de la vista [ItemChatSessionBinding], que provee acceso a los elementos
     *              de la plantilla de cada fila.
     */
    inner class VH(val b: ItemChatSessionBinding) : RecyclerView.ViewHolder(b.root) {
        /**
         * Vincula un objeto [ChatRecopiladosViewModel.Item] a la fila de la interfaz.
         *
         * - Muestra el nombre del producto en [b.tvName].
         * - Formatea y muestra el precio en [b.tvPrice] con el símbolo de euro.
         * - Si el producto tiene una imagen en Base64, la decodifica, la carga con Glide
         *   y aplica un recorte circular en [b.image].
         * - Configura el listener de clic en la raíz de la vista para invocar [onClick]
         *   pasando el [item].
         *
         * @param item Elemento de tipo [ChatRecopiladosViewModel.Item] a mostrar.
         */
        fun bind(item: ChatRecopiladosViewModel.Item) {
            b.tvName.text  = item.product.nombre
            b.tvPrice.text = "€ %.2f".format(item.product.precio)
            item.product.imagenBase64?.let { b64 ->
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                Glide.with(b.image).load(bytes).circleCrop().into(b.image)
            }
            b.root.setOnClickListener { onClick(item) }
        }
    }

    /**
     * Actualiza la lista de elementos y notifica al adaptador que los datos han cambiado.
     *
     * @param newItems Nueva lista de [ChatRecopiladosViewModel.Item] que reemplazará a [items].
     */
    fun updateList(newItems: List<ChatRecopiladosViewModel.Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Infla la vista de cada elemento del RecyclerView y devuelve un [VH].
     *
     * @param parent Grupo de vistas que contendrá el nuevo ViewHolder.
     * @param viewType Tipo de vista (no utilizado en este adaptador).
     * @return Instancia de [VH] con la vista inflada para una sesión de chat.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemChatSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /**
     * Retorna el número de elementos actuales en la lista.
     *
     * @return Cantidad de [items] que se mostrarán en el RecyclerView.
     */
    override fun getItemCount() = items.size

    /**
     * Vincula el ViewHolder con los datos del elemento en la posición especificada.
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param pos    Posición del elemento en la lista [items].
     */
    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(items[pos])
}
