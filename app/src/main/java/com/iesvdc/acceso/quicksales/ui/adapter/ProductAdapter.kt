// File: com/iesvdc/acceso/quicksales/ui/adapter/ProductAdapter.kt
package com.iesvdc.acceso.quicksales.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemProductBinding

class ProductAdapter(
    private val onBuy: (ProductResponse) -> Unit,
    private val onToggleFavorite: (ProductResponse) -> Unit
) : ListAdapter<ProductResponse, ProductAdapter.ViewHolder>(DIFF) {

    private var favoriteIds: Set<Int> = emptySet()

    fun setFavorites(ids: Set<Int>) {
        favoriteIds = ids
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemProductBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(p: ProductResponse) {
            b.textProductoName.text = p.nombre
            b.textProductoDescription.text = p.descripcion
            b.textProductoPrice.text = "€ %.2f".format(p.precio)
            p.imagenBase64?.let {
                val bytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                Glide.with(b.imageProducto).asBitmap().load(bytes).into(b.imageProducto)
            }

            b.buyButton.setOnClickListener { onBuy(p) }

            // aquí el toggle del icono
            val isFav = favoriteIds.contains(p.id)
            b.favButton.setImageResource(
                if (isFav) R.mipmap.ic_corazon_lleno_foreground
                else       R.mipmap.ic_corazon_vacio_foreground
            )
            b.favButton.setOnClickListener { onToggleFavorite(p) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductResponse>() {
            override fun areItemsTheSame(a: ProductResponse, b: ProductResponse) = a.id == b.id
            override fun areContentsTheSame(a: ProductResponse, b: ProductResponse) = a == b
        }
    }
}
