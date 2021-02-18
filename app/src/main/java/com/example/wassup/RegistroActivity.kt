package com.example.wassup

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import android.os.Bundle
import com.example.wassup.R
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.view.View
import android.widget.Button
import com.example.wassup.MainActivity
import java.util.HashMap

class RegistroActivity : AppCompatActivity() {
    //Componentes del layout
    var usuarioET: EditText? = null
    var contrasenaET: EditText? = null
    var emailET: EditText? = null
    var registroBT: Button? = null

    //Firebase
    var auth: FirebaseAuth? = null
    var mRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //Inicializando componentes
        usuarioET = findViewById(R.id.usuarioEditText)
        contrasenaET = findViewById(R.id.passwordEditText)
        emailET = findViewById(R.id.emailEditText)
        registroBT = findViewById(R.id.registroButton)

        //Firebase Auth
        auth = FirebaseAuth.getInstance()

        //Añadimos el listener al botón de registro
        registroBT.setOnClickListener(View.OnClickListener {
            val usuario_texto = usuarioET.getText().toString()
            val email_texto = emailET.getText().toString()
            val contrasena_texto = contrasenaET.getText().toString()

            //Si los campos no son rellenados, hacemos un toast indicando el error. Si están correctos, creo el usuario
            if (TextUtils.isEmpty(usuario_texto) || TextUtils.isEmpty(email_texto) || TextUtils.isEmpty(contrasena_texto)) {
                Toast.makeText(this@RegistroActivity, R.string.rellenarcampos, Toast.LENGTH_SHORT).show()
            } else {
                registrarUsuario(usuario_texto, email_texto, contrasena_texto)
            }
        })
    }

    private fun registrarUsuario(usuario: String, email: String, password: String) {
        auth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth!!.currentUser
                        val userid = firebaseUser!!.uid
                        mRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userid)

                        //HashMap
                        val hashMap = HashMap<String, String>()
                        hashMap["id"] = userid
                        hashMap["usuario"] = usuario
                        hashMap["imagenURL"] = "default"
                        hashMap["estado"] = "offline"

                        //Abrir la activity main tras registrarnos con éxito
                        mRef!!.setValue(hashMap).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Vamos desde el activity de registro al main activity
                                val i = Intent(this@RegistroActivity, MainActivity::class.java)
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(i)
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@RegistroActivity, R.string.emailinvalido, Toast.LENGTH_SHORT).show()
                    }
                }
    }
}