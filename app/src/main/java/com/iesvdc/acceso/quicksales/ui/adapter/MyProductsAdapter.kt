package com.iesvdc.acceso.quicksales.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemMyProductBinding

/**
 * Adaptador de RecyclerView para mostrar la lista de productos propios del usuario.
 *
 * Utiliza [ListAdapter] junto con [DiffUtil] para gestionar cambios de forma eficiente.
 * Permite acciones de edición y eliminación de cada producto.
 *
 * @property onEdit Lambda que se invoca al pulsar sobre un elemento para editar el producto.
 * @property onDelete Lambda que se invoca al pulsar el botón de eliminar de un elemento.
 */
class MyProductsAdapter(
    private val onEdit: (ProductResponse) -> Unit,
    private val onDelete: (ProductResponse) -> Unit
) : ListAdapter<ProductResponse, MyProductsAdapter.ViewHolder>(DIFF) {

    /**
     * Infla la vista de cada elemento del RecyclerView y crea un [ViewHolder].
     *
     * @param parent Grupo de vistas que contendrá el nuevo ViewHolder.
     * @param viewType Tipo de vista (no utilizado en este adaptador).
     * @return Instancia de [ViewHolder] con la vista inflada para un producto.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemMyProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    /**
     * Vincula el [ViewHolder] con los datos del producto en la posición especificada.
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del elemento en la lista gestionada por [ListAdapter].
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que contiene la vista de cada producto en la lista.
     *
     * @property b Binding de la vista [ItemMyProductBinding], que provee acceso a los elementos de la plantilla.
     */
    inner class ViewHolder(private val b: ItemMyProductBinding) :
        RecyclerView.ViewHolder(b.root) {
        /**
         * Vincula un objeto [ProductResponse] a la vista.
         *
         * - Muestra el nombre del producto en [b.textProductoName].
         * - Muestra la descripción en [b.textProductoDescription].
         * - Formatea y muestra el precio en [b.textProductoPrice] con el símbolo de euro.
         * - Si el producto tiene una imagen en Base64, la decodifica, la carga con Glide
         *   y la muestra en [b.imageProducto].
         * - Configura el listener de clic en la raíz de la vista para invocar [onEdit]
         *   pasando el producto.
         * - Configura el listener de clic en [b.buttonDelete] para invocar [onDelete]
         *   pasando el producto.
         *
         * @param p Instancia de [ProductResponse] a mostrar.
         */
        fun bind(p: ProductResponse) {
            b.textProductoName.text = p.nombre
            b.textProductoDescription.text = p.descripcion
            b.textProductoPrice.text = "€ %.2f".format(p.precio)
            p.imagenBase64?.let {
                val bytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                Glide.with(b.imageProducto).asBitmap().load(bytes).into(b.imageProducto)
            }
            b.root.setOnClickListener { onEdit(p) }
            b.buttonDelete.setOnClickListener { onDelete(p) }
        }
    }

    companion object {
        /**
         * Implementación de [DiffUtil.ItemCallback] para comparar elementos de tipo [ProductResponse].
         *
         * - [areItemsTheSame]: compara si dos objetos representan el mismo producto mediante su ID.
         * - [areContentsTheSame]: compara si el contenido de dos productos es idéntico.
         */
        private val DIFF = object : DiffUtil.ItemCallback<ProductResponse>() {
            override fun areItemsTheSame(a: ProductResponse, b: ProductResponse) = a.id == b.id
            override fun areContentsTheSame(a: ProductResponse, b: ProductResponse) = a == b
        }
    }
}
