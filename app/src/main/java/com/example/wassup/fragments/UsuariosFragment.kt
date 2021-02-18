package com.example.wassup.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wassup.R
import com.example.wassup.adapters.UsuarioAdapter
import com.example.wassup.modelos.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class UsuariosFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var usuarioAdapter: UsuarioAdapter? = null
    private var listaUsuarios: MutableList<Usuario?>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_usuarios, container, false)
        recyclerView = view.findViewById(R.id.usuariosRV)
        recyclerView.setHasFixedSize(true)
        recyclerView.setLayoutManager(LinearLayoutManager(context))
        listaUsuarios = ArrayList()
        obtenerUsuarios()
        return view
    }

    private fun obtenerUsuarios() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Usuarios")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaUsuarios!!.clear()
                for (dataSnapshot in snapshot.children) {
                    val usuario = dataSnapshot.getValue(Usuario::class.java)
                    Log.e("------------ USUARIO ", usuario!!.id + "ESTADO" + usuario.estado + "IMAGEN" + usuario.imagenURL + "NOMBRE" + usuario.nombreUsuario)
                    assert(usuario != null)

                    //Con esta condición evitamos que el usuario del dispositivo esté en la lista de contactos y añadimos al resto de usuarios
                    if (usuario.id != firebaseUser!!.uid) {
                        listaUsuarios!!.add(usuario)
                    }

                    //Mostramos los usuarios en el RV
                    usuarioAdapter = UsuarioAdapter(context!!, listaUsuarios, false)
                    recyclerView!!.adapter = usuarioAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}