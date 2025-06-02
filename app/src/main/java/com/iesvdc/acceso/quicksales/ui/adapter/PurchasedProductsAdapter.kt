package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemProductCompradoBinding

/**
 * Adaptador de RecyclerView para mostrar la lista de productos comprados por el usuario.
 *
 * @property items Lista de [ProductResponse] que contiene los productos adquiridos.
 */
class PurchasedProductsAdapter(
    private val items: List<ProductResponse>
) : RecyclerView.Adapter<PurchasedProductsAdapter.VH>() {

    /**
     * ViewHolder que contiene la vista de cada producto comprado en la lista.
     *
     * @property b Binding de la vista [ItemProductCompradoBinding], que provee acceso a los elementos de la plantilla.
     */
    inner class VH(val b: ItemProductCompradoBinding) : RecyclerView.ViewHolder(b.root) {
        /**
         * Vincula un objeto [ProductResponse] a la vista.
         *
         * - Muestra el nombre del producto en [b.textProductoName].
         * - Muestra el precio formateado en [b.textProductoPrice] con el símbolo de euro.
         * - Si el producto tiene una imagen en Base64, la decodifica y la carga en [b.imageProducto] usando Glide.
         *
         * @param p Instancia de [ProductResponse] a mostrar.
         */
        fun bind(p: ProductResponse) {
            b.textProductoName.text  = p.nombre
            b.textProductoPrice.text = "€ %.2f".format(p.precio)
            p.imagenBase64?.let { b64 ->
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                Glide.with(b.imageProducto).load(bytes).into(b.imageProducto)
            }
        }
    }

    /**
     * Infla la vista de cada elemento del RecyclerView y crea un [VH].
     *
     * @param parent Grupo de vistas que contendrá el nuevo ViewHolder.
     * @param viewType Tipo de vista (no utilizado en este adaptador).
     * @return Instancia de [VH] con la vista inflada para un producto comprado.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductCompradoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    /**
     * Retorna la cantidad de elementos actuales en la lista.
     *
     * @return Número total de productos comprados a mostrar.
     */
    override fun getItemCount() = items.size

    /**
     * Vincula el ViewHolder con los datos del producto en la posición especificada.
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param pos Posición del elemento en la lista [items].
     */
    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(items[pos])
}
