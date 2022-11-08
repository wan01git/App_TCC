package com.example.app_tcc

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class   FormLogin : AppCompatActivity() {
    private lateinit var texto: TextView;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_login)
        supportActionBar?.hide()

        texto = findViewById(R.id.text_cadastro);
        texto.setOnClickListener {
            var intent = Intent(this, formCadastrar::class.java)
            startActivity(intent)
        }
    }

}