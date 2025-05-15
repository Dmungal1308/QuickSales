package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemProductBinding

class ProductAdapter(
    private val onDelete: (ProductResponse)->Unit,
    private val onEdit: (ProductResponse)->Unit,
    private val onBuy:  (ProductResponse)->Unit
): RecyclerView.Adapter<ProductAdapter.VH>() {

    private val items = mutableListOf<ProductResponse>()
    fun submit(list: List<ProductResponse>) {
        items.clear(); items += list; notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemProductBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, pos: Int) {
        holder.bind(items[pos])
    }

    inner class VH(val b: ItemProductBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(m: ProductResponse) {
            b.textProductoName.text        = m.nombre
            b.textProductoDescription.text = m.descripcion
            b.textProductoPrice.text       = "€${m.precio}"
            if (!m.imagenBase64.isNullOrEmpty()) {
                val bytes = Base64.decode(m.imagenBase64, Base64.DEFAULT)
                Glide.with(b.root).asBitmap().load(bytes).into(b.imageProducto)
            } else {
                b.imageProducto.setImageResource(R.mipmap.ic_logo_principal)
            }
            b.buyButton?.setOnClickListener { onBuy(m) }
        }
    }
}