package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.databinding.ItemChatSessionBinding
import com.iesvdc.acceso.quicksales.ui.modelview.ChatRecopiladosViewModel

class ChatSessionsAdapter(
    private var items: List<ChatRecopiladosViewModel.Item>,
    private val onClick: (ChatRecopiladosViewModel.Item) -> Unit
) : RecyclerView.Adapter<ChatSessionsAdapter.VH>() {

    inner class VH(val b: ItemChatSessionBinding) : RecyclerView.ViewHolder(b.root) {
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

    fun updateList(newItems: List<ChatRecopiladosViewModel.Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemChatSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(items[pos])
}
