package it.michelemarnati.moneybook

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity

import android.content.Intent
import android.os.Handler
import androidx.appcompat.app.AlertDialog


class ConfirmEmailCode: AppCompatActivity() {
    private var editTextCode: EditText? = null
    private var confirmButton: Button? = null
    private var originalCode: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_email_code)

        editTextCode = findViewById<View>(R.id.email_code) as EditText
        confirmButton = findViewById<View>(R.id.confirmCode) as Button
        originalCode = intent.getStringExtra("emailCode")
        confirmButton!!.setOnClickListener {
            val resultIntent = Intent()
            if(editTextCode!!.text.toString().equals(originalCode)){
                resultIntent.putExtra("result", true)
                setResult(Activity.RESULT_OK, resultIntent)
                AlertDialog.Builder(this@ConfirmEmailCode)
                    .setTitle("Codice corretto")
                    .setMessage("Hai inserito il codice correttamente!")
                    .show();
            }
            else {
                resultIntent.putExtra("result", false)
                setResult(Activity.RESULT_CANCELED, resultIntent)
                AlertDialog.Builder(this@ConfirmEmailCode)
                    .setTitle("Codice errato")
                    .setMessage("Hai inserito il codice sbagliato.")
                    .show();
            }
            Handler().postDelayed({
                finish()
            }, 3000)


        }
    }
}