package com.example.wassup.fragments

import com.example.wassup.adapters.UsuarioAdapter
import com.example.wassup.modelos.Usuario
import com.example.wassup.modelos.ListaChats
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.wassup.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.ArrayList

class ChatsFragment : Fragment() {
    private var usuarioAdapter: UsuarioAdapter? = null
    private var listaUsuarios: MutableList<Usuario?>? = null
    private var listaChats: MutableList<ListaChats?>? = null
    var firebaseUser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var recyclerView: RecyclerView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        recyclerView = view.findViewById(R.id.chatRV)
        recyclerView.setHasFixedSize(true)
        recyclerView.setLayoutManager(LinearLayoutManager(context))
        firebaseUser = FirebaseAuth.getInstance().currentUser
        listaChats = ArrayList()

        //Aquí nos situamos en el nodo ListaChats y dentro de este, en las conversaciones que ha tenido el usuario
        reference = FirebaseDatabase.getInstance().getReference("ListaChats")
                .child(firebaseUser!!.uid)
        //Controlamos los cambios internos en el nodo ListaChats para que sea actualizado al instante
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaChats.clear()
                for (dataSnapshot in snapshot.children) {
                    val listaChatsc = dataSnapshot.getValue(ListaChats::class.java)
                    listaChats.add(listaChatsc)
                }
                listarChats()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return view
    }

    //Este método nos devuelve la lista de contctos con los que hemos chateado recientemente
    private fun listarChats() {
        listaUsuarios = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaUsuarios.clear()

                //Obtenemos todos los usuarios contactados y los almacenamos en el array
                for (dataSnapshot in snapshot.children) {
                    val usuario = dataSnapshot.getValue(Usuario::class.java)
                    for (chatlist in listaChats!!) {
                        if (usuario!!.id == chatlist!!.id) {
                            listaUsuarios.add(usuario)
                        }
                    }
                }
                //Mostramos la lista de usuarios en el RV
                usuarioAdapter = UsuarioAdapter(context!!, listaUsuarios, true)
                recyclerView!!.adapter = usuarioAdapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}