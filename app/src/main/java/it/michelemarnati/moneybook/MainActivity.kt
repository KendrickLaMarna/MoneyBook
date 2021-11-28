package it.michelemarnati.moneybook

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.content.Intent
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    private var editTextEmail: EditText? = null
    private var editTextPsw: EditText? = null
    private var alphanumeric: String? = null
    private var loginButton: Button? = null
    private var openRegisterWindowButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        editTextEmail = findViewById<View>(R.id.editTextSignUpMail) as EditText
        editTextPsw = findViewById<View>(R.id.editTextSignUpPassword) as EditText
        loginButton = findViewById<View>(R.id.loginBtn) as Button
        openRegisterWindowButton = findViewById<View>(R.id.signupBtn) as Button

        openRegisterWindowButton!!.setOnClickListener() {

            //Toast.makeText(this, "ciao", Toast.LENGTH_SHORT).show()
            val registerWindowIntent = Intent(applicationContext, RegisterWindow::class.java)
            startActivity(registerWindowIntent)


        }

    }






}