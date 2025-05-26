package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ItemProductCompradoBinding

class PurchasedProductsAdapter(
    private val items: List<ProductResponse>
) : RecyclerView.Adapter<PurchasedProductsAdapter.VH>() {

    inner class VH(val b: ItemProductCompradoBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: ProductResponse) {
            b.textProductoName.text  = p.nombre
            b.textProductoPrice.text = "€ %.2f".format(p.precio)
            p.imagenBase64?.let { b64 ->
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                Glide.with(b.imageProducto).load(bytes).into(b.imageProducto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductCompradoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(items[pos])
}
