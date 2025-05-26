package com.iesvdc.acceso.quicksales.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemMyProductBinding

class MyProductsAdapter(
    private val onEdit: (ProductResponse) -> Unit,
    private val onDelete: (ProductResponse) -> Unit
) : ListAdapter<ProductResponse, MyProductsAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemMyProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val b: ItemMyProductBinding) :
        RecyclerView.ViewHolder(b.root) {
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
        private val DIFF = object : DiffUtil.ItemCallback<ProductResponse>() {
            override fun areItemsTheSame(a: ProductResponse, b: ProductResponse) = a.id == b.id
            override fun areContentsTheSame(a: ProductResponse, b: ProductResponse) = a == b
        }
    }
}
