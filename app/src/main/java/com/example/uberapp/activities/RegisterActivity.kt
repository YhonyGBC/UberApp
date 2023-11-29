package com.example.uberapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.uberapp.databinding.ActivityRegisterBinding
import com.example.uberapp.models.Client
import com.example.uberapp.providers.AuthProvider
import com.example.uberapp.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnGoToLogin.setOnClickListener{ goToLogin() }
        binding.btnRegister.setOnClickListener{ register() }

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun register() {
        val name = binding.textFieldName.text.toString()
        val lastName = binding.textFieldLastName.text.toString()
        val phone = binding.textFieldPhone.text.toString()
        val email = binding.textFieldEmail.text.toString()
        val password = binding.textFieldPassword.text.toString()
        val confirmPassword = binding.textFieldConfirmPassword.text.toString()

        if (isValidForm(name, lastName, phone, email, password, confirmPassword)) {
            /* Toast.makeText(this, "Formulario válido", Toast.LENGTH_SHORT).show() */
            authProvider.register(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    /* Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_LONG).show() */
                    val client = Client(
                        id = authProvider.getId(),
                        name = name,
                        lastName = lastName,
                        phone = phone,
                        email = email
                    )
                    clientProvider.create(client).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            goToMap()
                        }
                        else {
                            Toast.makeText(this@RegisterActivity, "Hubo un error almacenando los datos del usuario ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                            Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                        }
                    }
                }
                else {
                    Toast.makeText(this@RegisterActivity,
                        "Registro fallido ${it.exception.toString()}", Toast.LENGTH_LONG).show()
                    Log.d("FIREBASE", "Error: ${it.exception.toString()}")
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
    private fun isValidForm(name: String, lastName: String,
                            phone: String, email: String,
                            password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Debes ingresar tu nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        if (lastName.isEmpty()) {
            Toast.makeText(this, "Debes ingresar tu apellido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Debes ingresar tu teléfono", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "Debes ingresar tu correo electrónico", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Debes ingresar la contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Debes ingresar la confirmación de la contraseña", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas deben coindicir", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener mínimo 6 carácteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun goToLogin() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}