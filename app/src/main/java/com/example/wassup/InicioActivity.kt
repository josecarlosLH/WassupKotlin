package com.example.wassup

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class InicioActivity : AppCompatActivity() {
    //Componentes
    var usuarioET: EditText? = null
    var contrasenaET: EditText? = null
    var conectarBT: Button? = null
    var registrarBT: Button? = null

    //Firebase
    var auth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        // Comprobamos si el usuario se conecta por primera vez
        if (firebaseUser != null) {
            //Si ya se ha conectado previamente, lo enviamos a la pantalla principal directamente
            val i = Intent(this@InicioActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        //Inicializamos componentes
        usuarioET = findViewById(R.id.usuarioET)
        contrasenaET = findViewById(R.id.contrasenaET)
        conectarBT = findViewById(R.id.conectrBT)
        registrarBT = findViewById(R.id.registrarahoraBT)

        //Firebase Auth
        auth = FirebaseAuth.getInstance()

        //Listener al botón registarse ahora
        registrarBT.setOnClickListener(View.OnClickListener {
            val i = Intent(this@InicioActivity, RegistroActivity::class.java)
            startActivity(i)
        })

        //Listener al botón conectarse
        conectarBT.setOnClickListener(View.OnClickListener {
            val email_texto = usuarioET.getText().toString()
            val contrasena_texto = contrasenaET.getText().toString()

            //Comprobamos si los campos están vacíos
            if (TextUtils.isEmpty(email_texto) || TextUtils.isEmpty(contrasena_texto)) {
                Toast.makeText(this@InicioActivity, R.string.rellenar, Toast.LENGTH_SHORT).show()
            } else {
                auth!!.signInWithEmailAndPassword(email_texto, contrasena_texto) //Tenemos que comprobar si el usuario existe en la base de datos
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Si los datos son correctos, pasamos a la pantalla principal
                                val i = Intent(this@InicioActivity, MainActivity::class.java)
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(i)
                                finish()
                            } else {
                                //Si no son correctos, le damos un toast indicando que hay error
                                Toast.makeText(this@InicioActivity, R.string.credencialesincorrectas, Toast.LENGTH_SHORT).show()
                            }
                        }
            }
        })
    }
}