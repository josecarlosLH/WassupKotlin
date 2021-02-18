package com.example.wassup

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.wassup.fragments.ChatsFragment
import com.example.wassup.fragments.PerfilFragment
import com.example.wassup.fragments.UsuariosFragment
import com.example.wassup.modelos.Usuario
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {
    //Firebase
    var firebaseUser: FirebaseUser? = null
    var mRef: DatabaseReference? = null

    //Componentes
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null
    var viewPagerAdapter: ViewPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Obtenemos el usuario
        firebaseUser = FirebaseAuth.getInstance().currentUser
        mRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(firebaseUser!!.uid)
        mRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuario = snapshot.getValue(Usuario::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //Declaramos el tab layout  el viewpager junto a su adapter
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.view_pager)

        //NOTA: getSupportFragmentManager() se pone dentro del constructor porque lo que hace es devolver una objeto de tipo FragmentManager.
        //Gracias al FragmentManager, el ViewPager podrá cargar los distintos fragment disponibles en el View Pager.
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        //Añadimos los Fragment que hemos creado dentro del paquete fragments al ViewPager
        viewPagerAdapter!!.anadirFragment(ChatsFragment(), "Chats")
        viewPagerAdapter!!.anadirFragment(UsuariosFragment(), getString(R.string.usuarios))
        viewPagerAdapter!!.anadirFragment(PerfilFragment(), getString(R.string.perfil))

        //Le añadimos el adapter al ViewPager y cargamos el ViewPager en el TabLayout
        viewPager.setAdapter(viewPagerAdapter)
        tabLayout.setupWithViewPager(viewPager)
    }

    //Inflamos el menú
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //Aquí le añadimos la funcionalidad al botón cerrar sesión
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Creamos un switch en el que cada caso representa la funcionalidad de uno de los botones del menú
        when (item.itemId) {
            R.id.cerrarsesion -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, InicioActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                return true
            }
        }
        return false
    }

    //Clase del adapter del ViewPager
    inner class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        //En este ArrayList almacenaremos los fragments que va a contener el ViewPager
        private val fragments: ArrayList<Fragment>

        //En este ArrayList almacenaraemos los títulos de las pestañas correspondientes a cada fragment del ViewPager
        private val titulos: ArrayList<String>

        //Lo que devolvemos con este método es la posición dentro del ArrayList en la que está el fragment que queramos seleccionar.
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        //Devolvemos el número de fragments almacenados en el ArrayList
        override fun getCount(): Int {
            return fragments.size
        }

        //Método para añadir un fragment con su correspondiente título
        fun anadirFragment(fragment: Fragment, titulo: String) {
            fragments.add(fragment)
            titulos.add(titulo)
        }

        //Obtener número de páginas del ViewPager
        override fun getPageTitle(position: Int): CharSequence? {
            return titulos[position]
        }

        init {
            fragments = ArrayList()
            titulos = ArrayList()
        }
    }

    //Comprobamos si el usuario está conectado o desconectado
    private fun comprobarEstado(estado: String) {
        mRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["estado"] = estado
        mRef!!.updateChildren(hashMap)
    }

    //Si el usuario inicia la aplicación, está en línea
    override fun onResume() {
        super.onResume()
        comprobarEstado("online")
    }

    //Si el usuario pone a la aplicación en segundo plano o cierra la aplicación, se desconecta
    override fun onPause() {
        super.onPause()
        comprobarEstado("offline")
    }
}