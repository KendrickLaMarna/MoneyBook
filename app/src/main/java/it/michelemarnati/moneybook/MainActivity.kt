package it.michelemarnati.moneybook

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.*
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private var editTextEmail: EditText? = null
    private var editTextPsw: EditText? = null
    private var loginButton: Button? = null
    private var openRegisterWindowButton: Button? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views of activity_main
        editTextEmail = findViewById<View>(R.id.editTextSignUpMail) as EditText
        editTextPsw = findViewById<View>(R.id.editTextSignUpPassword) as EditText
        loginButton = findViewById<View>(R.id.loginBtn) as Button
        openRegisterWindowButton = findViewById<View>(R.id.signupBtn) as Button

        // Initialize Firebase Auth
        auth = Firebase.auth

        openRegisterWindowButton!!.setOnClickListener() {
            val registerWindowIntent = Intent(applicationContext, RegisterWindow::class.java)
            startActivity(registerWindowIntent)
        }

        loginButton!!.setOnClickListener() {
            auth.signInWithEmailAndPassword(editTextEmail!!.text.toString(), editTextPsw!!.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success")
                        val mainMenuWindowIntent = Intent(applicationContext, MainMenu::class.java)
                        startActivity(mainMenuWindowIntent)
                        finish()
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                    }
                }
        }

    }






}