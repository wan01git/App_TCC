package com.example.app_tcc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class   FormLogin : AppCompatActivity() {
    private lateinit var texto: TextView;
    private lateinit var edit_email: EditText
    private lateinit var edit_senha: EditText
    private lateinit var bt_entrar: Button
    private lateinit var snackbar: Snackbar
    private lateinit var user: FirebaseUser



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_login)
        supportActionBar?.hide()


        texto = findViewById(R.id.text_cadastro);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_entrar = findViewById(R.id.bt_entrar);

        texto.setOnClickListener {
            var intent = Intent(this, formCadastrar::class.java)

            startActivity(intent)
        }
        bt_entrar.setOnClickListener(View.OnClickListener {
            var email  = edit_email.text.toString();
            var senha  = edit_senha.text.toString();
            if (email.isEmpty() || senha.isEmpty()){
                snackbar = Snackbar.make(it,"Preencha todos campos", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.WHITE)
                snackbar.setTextColor(Color.BLACK)
                snackbar.show()
            }
            else{
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,senha).addOnCompleteListener(
                    OnCompleteListener {
                        task ->
                        if (task.isSuccessful){
                            var intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }else{
                            try {
                                throw task.exception!!
                            }
                            catch (e:Exception){
                                snackbar = Snackbar.make(it,"Erro ao realizar login!",Snackbar.LENGTH_LONG);
                                snackbar.setBackgroundTint(Color.WHITE)
                                snackbar.setTextColor(Color.BLACK)
                                snackbar.show()
                            }
                        }
                    })
            }
        })
    }

    override fun onStart() {
        super.onStart()
        user = FirebaseAuth.getInstance().currentUser!!
        if (user != null){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        else{
            var intent = Intent(this, FormLogin::class.java)
            startActivity(intent)
        }
    }

}