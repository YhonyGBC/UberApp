package com.example.uberapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.uberapp.databinding.ActivityMainBinding
import com.example.uberapp.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnRegister.setOnClickListener{ goToRegister() }
        binding.btnLogin.setOnClickListener{ login() }

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun login() {
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()
/*
        Toast.makeText(this, "Email: $email", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "Password: $password", Toast.LENGTH_SHORT).show()
*/
        if (isValidForm(email, password)) {
            /* Toast.makeText(this, "Formulario válido", Toast.LENGTH_SHORT).show() */
            authProvider.login(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    goToMap()
                }
                else {
                    Toast.makeText(this@MainActivity, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE", "ERROR: ${it.exception.toString()}")
                }
            }
        }

    }

    private fun goToMap() {
        val i = Intent(this, MapActivity::class.java)
        // Borra historial de activities
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun isValidForm(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo electrónico", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()

        if (authProvider.existSession()) {
            goToMap()
        }
    }
}