package com.example.wassup.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wassup.R
import com.example.wassup.modelos.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MensajesAdapter(private val context: Context, private val listaChats: List<Chat>, private val imgURL: String) : RecyclerView.Adapter<MensajesAdapter.ViewHolder>() {
    //Firebase
    var firebaseUser: FirebaseUser? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Dependiendo del tipo de mensaje, usaremos el layout de enviado o recibido para mostrar (inflar) el mensaje
        return if (viewType == MSJ_ENVIADO) {
            val view = LayoutInflater.from(context).inflate(R.layout.chat_item_enviado, parent, false)
            ViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.chat_item_recibido, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = listaChats[position]
        holder.mensajeTV.text = chat.mensaje

        //Si el usuario no tiene una imagen determinado, cargamos una por defecto
        if (imgURL == "default") {
            holder.perfilIV.setImageResource(R.mipmap.ic_launcher)
            //Si el usuario tiene una imagen guardada, usamos la librería de Glide (previamente vista con Luis)
            //para cargar nuestra imagen cargar nuestra imagen para cargar nuestra imagen en un marco circular.
        } else {
            Glide.with(context)
                    .load(imgURL)
                    .into(holder.perfilIV)
        }
        if (position == listaChats.size - 1) {
            if (chat.isEsLeido) {
                holder.leidoTV.setText(R.string.leido)
            } else {
                holder.leidoTV.setText(R.string.enviado)
            }
        } else {
            holder.leidoTV.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return listaChats.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mensajeTV: TextView
        var perfilIV: ImageView
        var leidoTV: TextView

        init {
            mensajeTV = itemView.findViewById(R.id.mensajeTV)
            perfilIV = itemView.findViewById(R.id.imagenPerfilChatIV)
            leidoTV = itemView.findViewById(R.id.leidoTV)
        }
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        //Con esto comprobamos si el usuario que ha enviado el mensaje es el mismo usuario que está conectado en nuestro dispositivo.
        //De esta forma, sabremos que un mensaje está siendo ENVIADO
        return if (listaChats[position].emisor == firebaseUser!!.uid) {
            MSJ_ENVIADO
        } else {
            //Si el usuario del mensaje no está conectado en nuestro dispositivo, podemos saber que el mensaje es RECIBIDO
            MSJ_RECIBIDO
        }
    }

    companion object {
        // CREAMOS VARIABLES FINALES PARA SABER SI EL MENSAJE CON EL QUE TRABAJAMOS ES ENVIADO O RECIBIDO
        const val MSJ_RECIBIDO = 0
        const val MSJ_ENVIADO = 1
    }
}