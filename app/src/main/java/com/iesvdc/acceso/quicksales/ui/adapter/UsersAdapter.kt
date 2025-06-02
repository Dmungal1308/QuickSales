package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import com.iesvdc.acceso.quicksales.databinding.ItemUserBinding

/**
 * Adaptador de RecyclerView para mostrar una lista de usuarios con su información básica.
 *
 * @property items Lista de [UserDetailResponse] que contiene los detalles de cada usuario a mostrar.
 * @property onClick Lambda que se invoca al pulsar sobre un elemento de la lista; recibe el [UserDetailResponse] seleccionado.
 */
class UsersAdapter(
    private val items: List<UserDetailResponse>,
    private val onClick: (UserDetailResponse) -> Unit
): RecyclerView.Adapter<UsersAdapter.VH>() {

    /**
     * ViewHolder que contiene la vista de cada usuario en la lista.
     *
     * @property b Binding de la vista [ItemUserBinding], que provee acceso a los elementos de la plantilla.
     */
    inner class VH(val b: ItemUserBinding): RecyclerView.ViewHolder(b.root) {
        /**
         * Vincula un objeto [UserDetailResponse] a la vista.
         *
         * - Muestra el nombre de usuario en [b.tvUserName].
         * - Muestra el correo en [b.tvUserEmail].
         * - Si el usuario tiene una imagen en Base64, la decodifica y la carga en [b.imgUser] usando Glide, aplicando un recorte circular.
         * - Configura el listener de clic en la raíz de la vista para invocar [onClick] pasando el usuario.
         *
         * @param u Instancia de [UserDetailResponse] a mostrar.
         */
        fun bind(u: UserDetailResponse) {
            b.tvUserName.text = u.nombreUsuario
            b.tvUserEmail.text = u.correo
            u.imagenBase64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                Glide.with(b.imgUser).load(bytes).circleCrop().into(b.imgUser)
            }
            b.root.setOnClickListener { onClick(u) }
        }
    }

    /**
     * Infla la vista de cada elemento del RecyclerView y crea un [VH].
     *
     * @param p Grupo de vistas que contendrá el nuevo ViewHolder.
     * @param v Tipo de vista (no utilizado en este adaptador).
     * @return Instancia de [VH] con la vista inflada para un usuario.
     */
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(
        ItemUserBinding.inflate(LayoutInflater.from(p.context), p, false)
    )

    /**
     * Retorna la cantidad de elementos actuales en la lista.
     *
     * @return Número total de usuarios a mostrar.
     */
    override fun getItemCount() = items.size

    /**
     * Vincula el ViewHolder con los datos del usuario en la posición especificada.
     *
     * @param h ViewHolder que debe ser actualizado.
     * @param i Posición del elemento en la lista [items].
     */
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
}
