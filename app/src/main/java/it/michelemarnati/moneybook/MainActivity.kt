package it.michelemarnati.moneybook

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;





class MainActivity : AppCompatActivity(), View.OnClickListener {
    //Declaring EditText
    private var editTextEmail: EditText? = null
    private var editTextSubject: EditText? = null
    private var editTextMessage: EditText? = null

    //Send button
    private var buttonSend: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing the views
        editTextEmail = findViewById<View>(R.id.recipientEt) as EditText
        editTextSubject = findViewById<View>(R.id.subjectEt) as EditText
        editTextMessage = findViewById<View>(R.id.messageEt) as EditText
        buttonSend = findViewById<View>(R.id.sendEmailBtn) as Button

        //Adding click listener
        buttonSend!!.setOnClickListener(this)
    }


    private fun sendEmail() {
        //Getting content for email
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val subject = editTextSubject!!.text.toString().trim { it <= ' ' }
        val message = editTextMessage!!.text.toString().trim { it <= ' ' }

        //Creating SendMail object
        val sm = SendMail(this, email, subject, message)

        //Executing sendmail to send email
        sm.execute()
    }

    override fun onClick(v: View?) {
        sendEmail()
    }
}