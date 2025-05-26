package com.iesvdc.acceso.quicksales.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.databinding.ItemMensajeBinding

class ChatAdapter(
    private var items: List<ChatMessageResponse>,
    private val userIdActual: Int
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    inner class VH(val b: ItemMensajeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessageResponse) {
            b.tvTexto.text = m.texto

            val raw = m.fechaEnvio
            val horaMin = raw
                .substringAfter('T')
                .take(5)
            b.tvFecha.text = horaMin

            val params = b.container.layoutParams as FrameLayout.LayoutParams
            if (m.idRemitente == userIdActual) {
                params.gravity = Gravity.END
                b.container.setBackgroundResource(R.drawable.bg_burbuja_mia)
                b.tvTexto.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
                b.tvFecha.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
            } else {
                params.gravity = Gravity.START
                b.container.setBackgroundResource(R.drawable.bg_burbuja_otros)
                b.tvTexto.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
                b.tvFecha.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
            }
            b.container.layoutParams = params
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMensajeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<ChatMessageResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
