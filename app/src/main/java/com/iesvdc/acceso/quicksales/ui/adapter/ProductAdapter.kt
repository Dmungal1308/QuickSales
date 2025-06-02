package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemProductBinding

/**
 * Adaptador de RecyclerView para mostrar la lista de productos disponibles.
 *
 * Soporta acciones de compra, alternar favorito y clic en el ítem completo.
 *
 * @property onBuy Lambda que se invoca al pulsar el botón "Buy" de un producto.
 * @property onToggleFavorite Lambda que se invoca al pulsar el botón de favorito de un producto.
 * @property onItemClick Lambda que se invoca al pulsar la fila completa de un producto.
 */
class ProductAdapter(
    private val onBuy: (ProductResponse) -> Unit,
    private val onToggleFavorite: (ProductResponse) -> Unit,
    private val onItemClick: (ProductResponse) -> Unit
) : ListAdapter<ProductResponse, ProductAdapter.ViewHolder>(DIFF) {

    /**
     * Conjunto de IDs de productos marcados como favoritos.
     * Se utiliza para determinar qué icono de favorito mostrar.
     */
    private var favoriteIds: Set<Int> = emptySet()

    /**
     * Actualiza el conjunto de IDs de productos favoritos y refresca la vista.
     *
     * @param ids Conjunto de identificadores de productos que están marcados como favoritos.
     */
    fun setFavorites(ids: Set<Int>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

    /**
     * Infla la vista de cada elemento del RecyclerView y crea un [ViewHolder].
     *
     * @param parent Grupo de vistas que contendrá el nuevo ViewHolder.
     * @param viewType Tipo de vista (no utilizado en este adaptador).
     * @return Instancia de [ViewHolder] con la vista inflada para un producto.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    /**
     * Vincula el ViewHolder con los datos del producto en la posición especificada.
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
     * @property binding Binding de la vista [ItemProductBinding], que provee acceso a los elementos de la plantilla.
     */
    inner class ViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Vincula un objeto [ProductResponse] a la vista.
         *
         * - Muestra el nombre en [binding.textProductoName].
         * - Muestra el precio formateado en [binding.textProductoPrice].
         * - Si existe imagen en Base64, la decodifica y la carga en [binding.imageProducto] usando Glide.
         * - Muestra el icono de favorito según si el ID del producto está en [favoriteIds].
         * - Configura los listeners:
         *     - [binding.buyButton] invoca [onBuy] con el producto.
         *     - [binding.favButton] invoca [onToggleFavorite] con el producto.
         *     - [binding.root] invoca [onItemClick] con el producto.
         *
         * @param product Instancia de [ProductResponse] a mostrar.
         */
        fun bind(product: ProductResponse) {
            binding.textProductoName.text = product.nombre
            binding.textProductoPrice.text = "€ %.2f".format(product.precio)

            product.imagenBase64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                Glide.with(binding.imageProducto)
                    .asBitmap()
                    .load(bytes)
                    .into(binding.imageProducto)
            }

            val isFav = favoriteIds.contains(product.id)
            binding.favButton.setImageResource(
                if (isFav) R.mipmap.ic_corazon_lleno_foreground
                else R.mipmap.ic_corazon_vacio_foreground
            )

            binding.buyButton.setOnClickListener { onBuy(product) }
            binding.favButton.setOnClickListener { onToggleFavorite(product) }
            binding.root.setOnClickListener { onItemClick(product) }
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
