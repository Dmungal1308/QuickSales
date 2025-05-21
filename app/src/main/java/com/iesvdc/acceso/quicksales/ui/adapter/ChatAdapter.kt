package com.iesvdc.acceso.quicksales.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.databinding.ItemMensajeBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private var items: List<ChatMessageResponse>,
    private val userIdActual: Int
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class VH(val b: ItemMensajeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: ChatMessageResponse) {
            // 1) Texto y hora
            b.tvTexto.text = m.texto
            val horaMin = try {
                parser.parse(m.fechaEnvio)?.let { timeFormatter.format(it) } ?: m.fechaEnvio
            } catch (e: Exception) {
                m.fechaEnvio
            }
            b.tvFecha.text = horaMin

            // 2) Ajustar burbuja y colores
            val params = b.container.layoutParams as FrameLayout.LayoutParams
            if (m.idRemitente == userIdActual) {
                // Propio → a la derecha
                params.gravity = Gravity.END
                b.container.setBackgroundResource(R.drawable.bg_burbuja_mia)
                b.tvTexto.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
                b.tvFecha.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
            } else {
                // Otro → a la izquierda
                params.gravity = Gravity.START
                b.container.setBackgroundResource(R.drawable.bg_burbuja_otros)
                b.tvTexto.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
                b.tvFecha.setTextColor(ContextCompat.getColor(b.root.context, R.color.white))
            }
            b.container.layoutParams = params
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMensajeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size

    fun updateList(newItems: List<ChatMessageResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
