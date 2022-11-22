package com.example.app_tcc

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class formCadastrar : AppCompatActivity() {
    private lateinit var editNome: TextView
    private lateinit var editEmail: TextView
    private lateinit var editSenha: TextView
    private lateinit var botao: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var msg: String

    lateinit var snackbar: Snackbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_cadastro)

        auth = Firebase.auth
        supportActionBar?.hide()
        editNome=findViewById(R.id.edit_nome)
        editEmail=findViewById(R.id.edit_email)
        editSenha=findViewById(R.id.edit_senha)
        botao= findViewById(R.id.bt_cadastrar)

        botao.setOnClickListener {
            var nome  = editNome.text.toString();
            var email  = editEmail.text.toString();
            var senha  = editSenha.text.toString();
            var edit_nome = editNome.text.toString();
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()){
                snackbar = Snackbar.make(it,"Preencha todos campos",Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.WHITE)
                snackbar.setTextColor(Color.BLACK)
                snackbar.show()
            }
            else{
                auth.createUserWithEmailAndPassword(email,senha).addOnCompleteListener(
                    OnCompleteListener {
                        task ->
                        if(task.isSuccessful){
                            var user = FirebaseAuth.getInstance().currentUser!!.uid
                            val database = Firebase.database
                            val referencia = database.getReference(user)
                            referencia.child("nome").setValue(nome)
                            snackbar = Snackbar.make(it,"Cadastro realizado com sucesso!",Snackbar.LENGTH_LONG);
                            snackbar.setBackgroundTint(Color.WHITE)
                            snackbar.setTextColor(Color.BLACK)
                            snackbar.show()
                            var intent = Intent(this, MapsActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            var intent = Intent(this, MapsActivity::class.java)
                            startActivity(intent)
                            /*try {
                                throw task.exception!!
                            }
                            catch (error:FirebaseAuthUserCollisionException){
                                msg="Está conta já possui cadastro!"
                            }
                            catch (error:FirebaseAuthWeakPasswordException){
                                msg="Senha deve conter no mínimo 6 caracteres!"
                            }
                            catch (error:FirebaseAuthInvalidCredentialsException){
                                msg="E-mail inválido!"
                            }
                            catch (e:Exception){
                                msg="Erro ao cadastrar usuário!"
                            }
                            snackbar = Snackbar.make(it,msg,Snackbar.LENGTH_LONG);
                            snackbar.setBackgroundTint(Color.WHITE)
                            snackbar.setTextColor(Color.BLACK)
                            snackbar.show()*/
                        }
                    })
            }
        }

    }
}