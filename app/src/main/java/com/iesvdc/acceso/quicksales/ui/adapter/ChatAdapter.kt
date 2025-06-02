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

/**
 * Adaptador de RecyclerView para mostrar una lista de mensajes de chat.
 *
 * @property items Lista de [ChatMessageResponse] que contiene los mensajes a mostrar.
 * @property userIdActual Identificador del usuario actual, utilizado para diferenciar mensajes propios y ajenos.
 */
class ChatAdapter(
    private var items: List<ChatMessageResponse>,
    private val userIdActual: Int
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    /**
     * ViewHolder que contiene la vista de cada mensaje en el chat.
     *
     * @property b Binding de la vista [ItemMensajeBinding], que provee acceso a los elementos de la plantilla de mensaje.
     */
    inner class VH(val b: ItemMensajeBinding) : RecyclerView.ViewHolder(b.root) {
        /**
         * Vincula un objeto [ChatMessageResponse] a los elementos de la vista.
         *
         * Se encarga de:
         * - Mostrar el texto del mensaje en [b.tvTexto].
         * - Extraer y formatear la hora desde [m.fechaEnvio] para mostrarla en [b.tvFecha].
         * - Ajustar la posición y el estilo de la burbuja de chat según si el remitente es el usuario actual
         *   o un tercero:
         *     - Si `m.idRemitente == userIdActual`, alinea a la derecha, aplica el fondo `bg_burbuja_mia`
         *       y establece el color del texto en blanco.
         *     - En caso contrario, alinea a la izquierda, aplica el fondo `bg_burbuja_otros`
         *       y establece el color del texto en blanco.
         *
         * @param m Mensaje de chat a mostrar, de tipo [ChatMessageResponse].
         */
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

    /**
     * Infla la vista de cada elemento del RecyclerView y devuelve un [VH].
     *
     * @param parent Grupo de vistas que contendrá el nuevo ViewHolder.
     * @param viewType Tipo de vista (no usado en este adaptador).
     * @return Instancia de [VH] con la vista inflada para un mensaje.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMensajeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    /**
     * Vincula el ViewHolder con los datos del mensaje en la posición especificada.
     *
     * @param holder ViewHolder que debe ser actualizado.
     * @param position Posición del elemento en la lista [items].
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    /**
     * Retorna la cantidad de mensajes disponibles en [items].
     *
     * @return Número total de mensajes de chat a mostrar.
     */
    override fun getItemCount(): Int = items.size

    /**
     * Actualiza la lista de mensajes y notifica al adaptador que los datos han cambiado.
     *
     * @param newItems Nueva lista de [ChatMessageResponse] que reemplazará a [items].
     */
    fun updateList(newItems: List<ChatMessageResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
