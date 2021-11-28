package it.michelemarnati.moneybook

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    //Declaring EditText
    private var editTextEmail: EditText? = null
    private var editTextPsw: EditText? = null
    private var alphanumeric: String? = null

    //Send button
    private var loginButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing the views
        editTextEmail = findViewById<View>(R.id.editTextMail) as EditText
        editTextPsw = findViewById<View>(R.id.editTextPassword) as EditText
        loginButton = findViewById<View>(R.id.loginBtn) as Button
        //Adding click listener
        loginButton!!.setOnClickListener(this)
    }


    private fun sendEmail() {
        //Getting content for email
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val subject = "MoneyBook app authentication"
        alphanumeric = UUID.randomUUID().toString().substring(0, 8);
        val message = alphanumeric!!

        //Creating SendMail object
        val sm = SendMail(this, email, subject, message)

        //Executing sendmail to send email
        sm.execute()
    }

    override fun onClick(v: View?) {
        sendEmail()
    }
}