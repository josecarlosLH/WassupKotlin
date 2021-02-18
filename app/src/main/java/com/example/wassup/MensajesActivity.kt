package com.example.wassup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wassup.adapters.MensajesAdapter
import com.example.wassup.modelos.Chat
import com.example.wassup.modelos.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class MensajesActivity : AppCompatActivity() {
    //Componentes
    var nombreUsuario: TextView? = null
    var imageView: ImageView? = null
    var mensajeET: EditText? = null
    var enviarBT: ImageButton? = null

    //Firebase
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var intent: Intent? = null

    //Chat
    var mensajesAdapter: MensajesAdapter? = null
    var listaChat: MutableList<Chat?>? = null
    var recyclerView: RecyclerView? = null
    var usuarioid: String? = null
    var mensajeLeidoEvent: ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes)

        //Componentes del layout
        imageView = findViewById(R.id.perfilIV)
        nombreUsuario = findViewById(R.id.nombreUsuarioTV)
        enviarBT = findViewById(R.id.enviarBT)
        mensajeET = findViewById(R.id.mensajeET)

        //RecyclerView
        recyclerView = findViewById(R.id.mensajesRV)
        recyclerView.setHasFixedSize(true)
        //Con LinearLayoutManager establecemos un layout para mostrar el contenido de forma organizada
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true //Con este método, los mensajes empezarán a mostrarse de abajo a arriba
        recyclerView.setLayoutManager(linearLayoutManager)

        //Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        //setSupportActionBar(toolbar);
        supportActionBar!!.setTitle("")
        //setDisplayHomeAsUpEnabled(true) añade el icono > al toolbar para poder clicarlo y regresar a la pantalla principal. Mismo funcionamiento que
        //al pulsar el botón atrás del dispositivo
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //Cuando pulsamos el botón de atrás del dispositivo, finalizará la activity de los mensajes para volver a la pantalla principal
        toolbar.setNavigationOnClickListener { finish() }

        //Recibimos la id del usuario que seleccionamos en el RecyclerView de usuarios
        intent = getIntent()
        usuarioid = intent.getStringExtra("userid")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Usuarios")

        //Cargamos el nombre, la imagen de perfil y los mensajes que hemos intercambiado con ese usuario
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario = snapshot.getValue(Usuario::class.java)
                nombreUsuario.setText(usuario!!.nombreUsuario)
                if (usuario.imagenURL == "default") {
                    imageView.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(this@MensajesActivity)
                            .load(usuario.imagenURL)
                            .into(imageView)
                }
                cargarMensajes(firebaseUser!!.uid, usuarioid, usuario.imagenURL)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        enviarBT.setOnClickListener(View.OnClickListener {
            val mensaje = mensajeET.getText().toString()
            //Comprobamos que el mensaje no esté vacío para poder enviarlo
            if (mensaje != "") {
                enviarMensaje(firebaseUser!!.uid, usuarioid, mensaje)
            }
            mensajeET.setText("")
        })
        estadoMensaje(usuarioid)
    }

    private fun estadoMensaje(usuarioid: String?) {
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        mensajeLeidoEvent = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.receptor == firebaseUser!!.uid && chat.emisor == usuarioid) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["visto"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun enviarMensaje(emisor: String, receptor: String?, mensaje: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any?>()
        hashMap["emisor"] = emisor
        hashMap["receptor"] = receptor
        hashMap["mensaje"] = mensaje
        hashMap["visto"] = false

        //Con esta línea, guardamos los datos del mensaje en el nodo Chats indicando el mensaje, el receptor y el emisor.
        reference.child("Chats").push().setValue(hashMap)

        //Obtenemos el historial ordenado de los usuarios con los que YA hemos contactado
        val chatRef = FirebaseDatabase.getInstance().getReference("ListaChats")
                .child(firebaseUser!!.uid)
                .child(usuarioid!!)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(usuarioid)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //Cargamos los mensajes de un chat concreto y obtenemos los datos del usuario con el que estamos chateando
    private fun cargarMensajes(miId: String, usuarioid: String?, imgURL: String?) {
        listaChat = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaChat.clear()
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    //Con este if comprobamos si los mensajes están siendo enviados por mí o por la otra persona
                    if (chat!!.receptor == miId && chat.emisor == usuarioid
                            || chat.receptor == usuarioid && chat.emisor == miId) {
                        listaChat.add(chat)
                    }
                    //Cargamos los mensajes en el RV
                    mensajesAdapter = MensajesAdapter(this@MensajesActivity, listaChat, imgURL!!)
                    recyclerView!!.adapter = mensajesAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun comprobarEstado(estado: String) {
        reference = FirebaseDatabase.getInstance().getReference("Usuarios").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado
        reference!!.updateChildren(hashMap)
    }

    //Si el usuario inicia la aplicación, está en línea
    override fun onResume() {
        super.onResume()
        comprobarEstado("online")
    }

    //Si el usuario pone a la aplicación en segundo plano o cierra la aplicación, se desconecta y paramos el listener para marcar los mensajes como vistos
    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(mensajeLeidoEvent!!)
        comprobarEstado("offline")
    }
}