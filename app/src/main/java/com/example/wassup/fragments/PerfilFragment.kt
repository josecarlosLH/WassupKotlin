package com.example.wassup.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wassup.R
import com.example.wassup.modelos.Usuario
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.*

class PerfilFragment : Fragment() {
    //Componentes
    var textView: TextView? = null
    var imageView: ImageView? = null

    //Firebase
    var reference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    //Imagen perfil
    var storageReference: StorageReference? = null
    private var imageUri: Uri? = null
    private var uploadTask: StorageTask<*>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)
        imageView = view.findViewById(R.id.imagenPerfilIV)
        textView = view.findViewById(R.id.nombreUsuario2TV)

        //Ubicar imagen de perfil en FireBase
        storageReference = FirebaseStorage.getInstance().getReference("Uploads")
        imageView.setOnClickListener(View.OnClickListener { seleccionarImagen() })
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(firebaseUser!!.uid)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Mostramos el nombre de usuario en su TV
                val usuario = snapshot.getValue(Usuario::class.java)
                textView.setText(usuario!!.nombreUsuario)
                Log.e("------------ USUARIO ", usuario.id + "ESTADO" + usuario.estado + "IMAGEN" + usuario.imagenURL + "NOMBRE" + usuario.nombreUsuario)

                //Mostramos la foto de perfil en el IV
                /*
                if (usuario.getImagenURL().equals("default")) {
                    imageView.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getContext()).load(usuario.getImagenURL()).into(imageView);
                }*/
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return view
    }

    private fun seleccionarImagen() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(i, PEDIR_IMAGEN)
    }

    private fun getExtensionArchivo(uri: Uri): String? {
        val contentResolver = context!!.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun subirImagen() {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(getString(R.string.subiendo))
        progressDialog.show()
        if (imageUri != null) {
            val fileReference = storageReference!!.child(System.currentTimeMillis().toString() + "." + getExtensionArchivo(imageUri!!))
            uploadTask = fileReference.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot?, Task<Uri>> { task ->
                if (!task.isSuccessful) throw task.exception!!
                fileReference.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val descargarUri = task.result
                    val mUri = descargarUri.toString()
                    reference = FirebaseDatabase.getInstance().getReference("Usuarios").child(firebaseUser!!.uid)
                    val hashMap = HashMap<String, Any>()
                    hashMap["imagenURL"] = mUri
                    reference!!.updateChildren(hashMap)
                    progressDialog.dismiss()
                } else {
                    Toast.makeText(context, R.string.falloimagen, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e -> Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show() }
        } else {
            Toast.makeText(context, "No hay imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PEDIR_IMAGEN && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            if (uploadTask != null && uploadTask!!.isInProgress) {
                Toast.makeText(context, getString(R.string.subidaprogresp), Toast.LENGTH_SHORT).show()
            } else {
                subirImagen()
            }
        }
    }

    companion object {
        private const val PEDIR_IMAGEN = 1
    }
}