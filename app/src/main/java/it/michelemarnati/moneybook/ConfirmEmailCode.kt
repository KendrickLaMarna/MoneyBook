package it.michelemarnati.moneybook

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.content.DialogInterface

import android.content.Intent
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import java.util.*


class ConfirmEmailCode: AppCompatActivity() {
    private var editTextCode: EditText? = null
    private var confirmButton: Button? = null
    private var originalCode: String? = null
    private lateinit var email:String
    private lateinit var name:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_email_code)

        editTextCode = findViewById<View>(R.id.email_code) as EditText
        confirmButton = findViewById<View>(R.id.confirmCode) as Button
        email = intent.getStringArrayListExtra("emailCode")!!.get(0)
        name = intent.getStringArrayListExtra("emailCode")!!.get(1)
        originalCode = intent.getStringArrayListExtra("emailCode")!!.get(2)
        confirmButton!!.setOnClickListener {
            val resultIntent = Intent()
            if(editTextCode!!.text.toString().equals(originalCode)){
                resultIntent.putExtra("result", true)
                setResult(Activity.RESULT_OK, resultIntent)
                AlertDialog.Builder(this@ConfirmEmailCode)
                    .setTitle("Codice corretto")
                    .setMessage("Hai inserito il codice correttamente!")
                    .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, whichButton ->
                        dialog.dismiss()
                        finish()})
                    .show();
            }
            else {
                resultIntent.putExtra("result", false)
                setResult(Activity.RESULT_CANCELED, resultIntent)
                AlertDialog.Builder(this@ConfirmEmailCode)
                    .setTitle("Codice errato")
                    .setMessage("Hai inserito il codice sbagliato.")
                    .setPositiveButton("OK",
                        DialogInterface.OnClickListener { dialog, whichButton ->
                            dialog.dismiss()
                            finish()})
                    .setPositiveButton("REINVIA",
                        DialogInterface.OnClickListener { dialog, whichButton ->
                            dialog.dismiss()
                            sendEmail(email, name)
                            })
                    .show();
            }



        }
    }
    private fun sendEmail(email:String, name:String) {
        //Getting content for email
        val email = email.trim { it <= ' ' }
        val subject = "MoneyBook authentication"
        originalCode = UUID.randomUUID().toString().substring(0, 8);
        val message = "Hi "+ name + "! Please insert this code: " + originalCode!!

        //Creating SendMail object
        val sm = SendMail(this, email, subject, message)

        //Executing sendmail to send email
        sm.execute()
    }
}