package it.michelemarnati.moneybook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.util.*

class RegisterWindow : AppCompatActivity(){
    private var editTextName: EditText? = null
    private var editTextSurname: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextConfirmMail: EditText? = null
    private var editTextPsw: EditText? = null
    private var editTextConfirmPsw: EditText? = null
    private var alphanumeric: String? = null
    private var registerWindowButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_window)

        editTextName = findViewById<View>(R.id.editTextName) as EditText
        editTextSurname = findViewById<View>(R.id.editTextSurname) as EditText
        editTextEmail = findViewById<View>(R.id.editTextSignUpMail) as EditText
        editTextConfirmMail = findViewById<View>(R.id.confirmMail) as EditText
        editTextPsw = findViewById<View>(R.id.editTextSignUpPassword) as EditText
        editTextConfirmPsw = findViewById<View>(R.id.confirmPassword) as EditText
        registerWindowButton = findViewById<View>(R.id.signupButton) as Button

        registerWindowButton!!.setOnClickListener {
            if(checkData()){
                sendEmail()
            }
            else{

            }

        }

    }

    private fun sendEmail() {
        //Getting content for email
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val subject = "MoneyBook authentication"
        alphanumeric = UUID.randomUUID().toString().substring(0, 8);
        val message = alphanumeric!!

        //Creating SendMail object
        val sm = SendMail(this, email, subject, message)

        //Executing sendmail to send email
        sm.execute()
    }

    private fun checkData(): Boolean{
        if(editTextEmail!!.equals(editTextConfirmMail)
            && !editTextEmail!!.equals("")
            && editTextPsw!!.equals(editTextConfirmPsw)
            && !editTextPsw!!.equals("")
            && !editTextName!!.equals("")
            && !editTextSurname!!.equals("")){
            return true
        }
        else{
            return false
        }

    }

}