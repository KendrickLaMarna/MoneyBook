package it.michelemarnati.moneybook

import android.app.Activity
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
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
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
            hideSoftKeyboard(this@MainActivity)

            //Load circle progress bar
            val pb = findViewById(R.id.progressBar) as ProgressBar
            pb.visibility = VISIBLE

            if(checkData()){
                auth.signInWithEmailAndPassword(editTextEmail!!.text.toString(), editTextPsw!!.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success")
                            val mainMenuWindowIntent = Intent(applicationContext, MainMenu::class.java)
                            startActivity(mainMenuWindowIntent)
                            pb.visibility = GONE
                            finish()
                        } else {
                            // Sign in failed
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            editTextEmail!!.setError(getString(R.string.invalid_mail_or_password))
                            editTextPsw!!.setError(getString(R.string.invalid_mail_or_password))
                            pb.visibility = GONE
                            Toast.makeText(this, getString(R.string.invalid_mail_or_password), Toast.LENGTH_SHORT).show()

                        }
                    }
            }
            else{
                pb.visibility = GONE
            }

        }

    }


    override fun onResume() {
        super.onResume()
        editTextEmail!!.setError(null)
        editTextEmail!!.text = Editable.Factory.getInstance().newEditable("")
        editTextPsw!!.setError(null)
        editTextPsw!!.text = Editable.Factory.getInstance().newEditable("")
    }

    fun checkData(): Boolean{
        var result = true

        if(!isValidEmail(editTextEmail!!.text.toString()) && editTextPsw!!.text.toString().equals("")){
            editTextEmail!!.setError(getString(R.string.error_invalid_mail))
            editTextPsw!!.setError(getString(R.string.error_register_empty_psw))
            Toast.makeText(this@MainActivity, getString(R.string.invalid_mail_or_password), Toast.LENGTH_SHORT).show()
            result = false
        }

        else if(!isValidEmail(editTextEmail!!.text.toString())){
            editTextEmail!!.setError(getString(R.string.error_invalid_mail))
            Toast.makeText(this@MainActivity, getString(R.string.error_invalid_mail), Toast.LENGTH_SHORT).show()
            result = false
        }
        else if (editTextPsw!!.text.toString().equals("")){
            editTextPsw!!.setError(getString(R.string.error_register_empty_psw))
            Toast.makeText(this@MainActivity, getString(R.string.error_register_empty_psw), Toast.LENGTH_SHORT).show()
            result = false
        }

        return result
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    //function that hides Keyboard when called
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }






}