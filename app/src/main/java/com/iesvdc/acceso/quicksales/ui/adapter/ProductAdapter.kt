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

class ProductAdapter(
    private val onBuy: (ProductResponse) -> Unit,
    private val onToggleFavorite: (ProductResponse) -> Unit,
    private val onItemClick: (ProductResponse) -> Unit
) : ListAdapter<ProductResponse, ProductAdapter.ViewHolder>(DIFF) {

    private var favoriteIds: Set<Int> = emptySet()

    fun setFavorites(ids: Set<Int>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: ProductResponse) {
            // datos
            binding.textProductoName.text        = product.nombre
            binding.textProductoPrice.text       = "€ %.2f".format(product.precio)

            product.imagenBase64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                Glide.with(binding.imageProducto)
                    .asBitmap()
                    .load(bytes)
                    .into(binding.imageProducto)
            }

            // icono favorito
            val isFav = favoriteIds.contains(product.id)
            binding.favButton.setImageResource(
                if (isFav) R.mipmap.ic_corazon_lleno_foreground
                else R.mipmap.ic_corazon_vacio_foreground
            )

            // listeners
            binding.buyButton.setOnClickListener      { onBuy(product) }
            binding.favButton.setOnClickListener      { onToggleFavorite(product) }
            binding.root.setOnClickListener           { onItemClick(product) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductResponse>() {
            override fun areItemsTheSame(a: ProductResponse, b: ProductResponse) = a.id == b.id
            override fun areContentsTheSame(a: ProductResponse, b: ProductResponse) = a == b
        }
    }
}
