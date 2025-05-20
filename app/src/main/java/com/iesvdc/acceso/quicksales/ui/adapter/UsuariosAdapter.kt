package com.iesvdc.acceso.quicksales.ui.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import com.iesvdc.acceso.quicksales.databinding.ItemUserBinding

class UsuariosAdapter(
    private val items: List<UserDetailResponse>,
    private val onClick: (UserDetailResponse) -> Unit
): RecyclerView.Adapter<UsuariosAdapter.VH>() {
    inner class VH(val b: ItemUserBinding): RecyclerView.ViewHolder(b.root) {
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
    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(
        ItemUserBinding.inflate(LayoutInflater.from(p.context), p, false)
    )
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
}
