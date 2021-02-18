package com.example.wassup.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wassup.MensajesActivity
import com.example.wassup.R
import com.example.wassup.modelos.Usuario

class UsuarioAdapter(private val context: Context, private val listaUsuarios: List<Usuario>, private val estaConectado: Boolean) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.usuario_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.usuario.text = usuario.nombreUsuario

        //Si el usuario no tiene una imagen determinado, cargamos una por defecto
        if (usuario.imagenURL == "default") {
            holder.usuarioIV.setImageResource(R.mipmap.ic_launcher)
            //Si el usuario tiene una imagen guardada, usamos la librería de Glide (previamente vista con Luis) para cargar nuestra imagen
            //en un marco circular.
        } else {
            Glide.with(context)
                    .load(usuario.imagenURL)
                    .into(holder.usuarioIV)
        }

        //Estado del usuario
        if (estaConectado) {
            if (usuario.estado == "online") {
                holder.onlineIV.visibility = View.VISIBLE
                holder.offlineIV.visibility = View.GONE
            } else {
                holder.onlineIV.visibility = View.GONE
                holder.offlineIV.visibility = View.VISIBLE
            }
            // Si no está conectado, ocultamos las IV de estado de conexión
        } else {
            holder.onlineIV.visibility = View.GONE
            holder.offlineIV.visibility = View.GONE
        }

        //Listener para cuando seleccionemos un usuario del RecyclerView
        holder.itemView.setOnClickListener {
            val i = Intent(context, MensajesActivity::class.java)
            i.putExtra("userid", usuario.id)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usuario: TextView
        var usuarioIV: ImageView
        var onlineIV: ImageView
        var offlineIV: ImageView

        init {
            usuario = itemView.findViewById(R.id.usuarioTV)
            usuarioIV = itemView.findViewById(R.id.usuarioIV)
            onlineIV = itemView.findViewById(R.id.onlineIV)
            offlineIV = itemView.findViewById(R.id.offlineIV)
        }
    }
}