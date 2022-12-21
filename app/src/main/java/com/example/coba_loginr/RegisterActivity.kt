package com.example.coba_loginr

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.coba_loginr.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    var maxid : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.RegisterEmail.text.toString().trim()
            val password = binding.RegisterPassword.text.toString().trim()

            if (email.isEmpty()){
                binding.RegisterEmail.error = "Email Kosong silakan diisi"
                binding.RegisterEmail.requestFocus()
                return@setOnClickListener
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.RegisterEmail.error = "Email TIdak Valid"
                binding.RegisterEmail.requestFocus()
                return@setOnClickListener
            }

            else if (password.isEmpty() || password.length > 10) {
                binding.RegisterPassword.error = "Password Kosong atau password lebih dari 10 silakan diisi"
                binding.RegisterPassword.requestFocus()
                return@setOnClickListener
            }
            else {
                database = FirebaseDatabase.getInstance().getReference("Users")
                database.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            maxid = (snapshot.childrenCount)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(TAG, "Failed to read value.", error.toException())
                    }

                })
                val User = Pengguna(email, password)
                database.child((this.maxid +1).toString()).setValue(User).addOnCompleteListener {
                    registerUser(email, password)
                }

            }

        }
        binding.pindahLogin.setOnClickListener {
            Intent(this@RegisterActivity, LoginActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) { it ->
                if (it.isSuccessful) {
                    binding.RegisterEmail.text.clear()
                    binding.RegisterPassword.text.clear()
                    Toast.makeText(this, "Success Registration", Toast.LENGTH_SHORT).show()
                    Intent(this@RegisterActivity, LoginActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                }else {
                    Toast.makeText(this, "Register Gagal Silakan Dicoba ulang", Toast.LENGTH_SHORT).show()
                }
            }
    }
}