package com.example.app_tcc

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.app_tcc.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var text_nome: EditText
    private lateinit var text_email: EditText
    private lateinit var deslogar: Button
    private lateinit var db:FirebaseDatabase
    private lateinit var ref:DatabaseReference
    private lateinit var snackbar: Snackbar

    lateinit var nome: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val nav= findViewById<NavigationView>(R.id.nav_home)
        val header = nav?.getHeaderView(0)
        //deslogar = header?.findViewById<TextView>(R.id.bt_deslogar) as Button

        db = FirebaseDatabase.getInstance()
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        ref = db.reference.child(userID)
        var getdata= object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                nome= snapshot.child("nome").getValue().toString()
                snackbar = Snackbar.make(nav,nome, Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.WHITE)
                snackbar.setTextColor(Color.BLACK)
                snackbar.show()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }

        /*deslogar.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent = Intent(this, FormLogin::class.java)
            startActivity(intent)
            finish()
        })*/

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onStart() {
        super.onStart()

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}