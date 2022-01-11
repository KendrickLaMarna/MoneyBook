package it.michelemarnati.moneybook

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.text.TextUtils
import android.util.Patterns
import android.content.ContentValues.TAG
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList


class RegisterWindow : AppCompatActivity(){
    private var editTextName: EditText? = null
    private var editTextSurname: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextConfirmMail: EditText? = null
    private var editTextPsw: EditText? = null
    private var editTextConfirmPsw: EditText? = null
    private var alphanumeric: String? = null
    private var registerWindowButton: Button? = null
    private var LAUNCH_CONFIRM_EMAIL_CODE: Int = 0
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_window)
        LAUNCH_CONFIRM_EMAIL_CODE = 1
        editTextName = findViewById<View>(R.id.editTextName) as EditText
        editTextSurname = findViewById<View>(R.id.editTextSurname) as EditText
        editTextEmail = findViewById<View>(R.id.editTextSignUpMail) as EditText
        editTextConfirmMail = findViewById<View>(R.id.confirmMail) as EditText
        editTextPsw = findViewById<View>(R.id.editTextSignUpPassword) as EditText
        editTextConfirmPsw = findViewById<View>(R.id.confirmPassword) as EditText
        registerWindowButton = findViewById<View>(R.id.signupButton) as Button

        //get the Firebase Database
        database = FirebaseDatabase.getInstance("https://moneybook-86d63-default-rtdb.europe-west1.firebasedatabase.app").reference

        //Initialize the Firebase auth
        auth = Firebase.auth

        registerWindowButton!!.setOnClickListener {
            checkData()
//            if(checkData()){
//                sendEmail()
//                Handler().postDelayed({
//                    val confirmCodeWindowIntent = Intent(applicationContext, ConfirmEmailCode::class.java)
//                    try{
//                        confirmCodeWindowIntent.putExtra("emailCode", alphanumeric)
//                        startActivityForResult(confirmCodeWindowIntent, LAUNCH_CONFIRM_EMAIL_CODE)
//                    }catch(e: Exception){
//                        e.printStackTrace()
//
//                    }
//                }, 2000)
//
//
//            }


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_CONFIRM_EMAIL_CODE) {
            if (resultCode == RESULT_OK) {
                //User has written the correct code
                auth.createUserWithEmailAndPassword(editTextEmail!!.text.toString(), editTextPsw!!.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = User(
                                editTextName!!.text.toString(),
                                editTextSurname!!.text.toString(),
                                editTextEmail!!.text.toString()
                            )

                            //Insert user data in Firebase database
                            database.child("users").child(auth.currentUser!!.uid).setValue(user)
                                .addOnSuccessListener(this, {
                                    @Override fun onSuccess(aVoid: Void){
                                        // SUCCESS
                                        // Log the details
                                        Log.d("FirebaseData","user data uploaded successfully");
                                    }

                                }).addOnFailureListener(this, OnFailureListener(){
                                    @Override fun onFailure(@NonNull e: Exception){
                                        // FAILURE
                                        // Log the details
                                        Log.d("FirebaseData","user data upload failed");
                                    }
                                });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                finish()
            }
        }
    }


    private fun sendEmail() {
        //Getting content for email
        val email = editTextEmail!!.text.toString().trim { it <= ' ' }
        val subject = "MoneyBook authentication"
        alphanumeric = UUID.randomUUID().toString().substring(0, 8);
        val message = "Hi "+ editTextName!!.text.toString() + "! Please insert this code: " + alphanumeric!!

        //Creating SendMail object
        val sm = SendMail(this, email, subject, message)

        //Executing sendmail to send email
        sm.execute()
    }

    private fun checkData(){
        var checkOk = 0
        var email = editTextEmail!!.text.toString()
        if(isValidEmail(email)) {

            //check if user email already exists
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(OnCompleteListener<SignInMethodQueryResult> { task ->
                    val isNewUser = task.result.signInMethods!!.isEmpty()

                    if (isNewUser
                        && isValidEmail(editTextEmail!!.text.toString())
                        && editTextEmail!!.text.toString()
                            .equals(editTextConfirmMail!!.text.toString())
                        && editTextPsw!!.text.toString()
                            .equals(editTextConfirmPsw!!.text.toString())
                        && !(editTextPsw!!.text.toString().equals(""))
                        && !(editTextName!!.text.toString().equals(""))
                        && !(editTextSurname!!.text.toString().equals(""))
                    ) {
                        sendEmail()
                        Handler().postDelayed({
                            val confirmCodeWindowIntent =
                                Intent(applicationContext, ConfirmEmailCode::class.java)
                            try {
                                val emailCodeArray = ArrayList<String>()
                                emailCodeArray.add(editTextEmail!!.text.toString())
                                emailCodeArray.add(editTextName!!.text.toString())
                                emailCodeArray.add(alphanumeric!!)
                                confirmCodeWindowIntent.putExtra("emailCode", emailCodeArray)
                                startActivityForResult(
                                    confirmCodeWindowIntent,
                                    LAUNCH_CONFIRM_EMAIL_CODE
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()

                            }
                        }, 2000)
                    } else {
                        if (!isNewUser) {
                            editTextEmail!!.setError(getString(R.string.error_register_existing_mail))
                            editTextConfirmMail!!.setError(getString(R.string.error_register_existing_mail))
                            Toast.makeText(this, getString(R.string.error_register_existing_mail), Toast.LENGTH_SHORT).show()
                        } else if (!(editTextEmail!!.text.toString()
                                .equals(editTextConfirmMail!!.text.toString()))
                        ) {
                            editTextEmail!!.setError(getString(R.string.error_register_copy_mail))
                            editTextConfirmMail!!.setError(getString(R.string.error_register_copy_mail))
                            Toast.makeText(this, getString(R.string.error_register_copy_mail), Toast.LENGTH_SHORT).show()
                        } else if (!isValidEmail(editTextEmail!!.text.toString())) {
                            editTextEmail!!.setError(getString(R.string.error_invalid_mail))
                            editTextConfirmMail!!.setError(getString(R.string.error_invalid_mail))
                            Toast.makeText(this, getString(R.string.error_invalid_mail), Toast.LENGTH_SHORT).show()
                        }
                        if (!(editTextPsw!!.text.toString()
                                .equals(editTextConfirmPsw!!.text.toString()))
                        ) {
                            editTextPsw!!.setError(getString(R.string.error_register_copy_psw))
                            editTextConfirmPsw!!.setError(getString(R.string.error_register_copy_psw))
                            Toast.makeText(this, getString(R.string.error_register_copy_psw), Toast.LENGTH_SHORT).show()
                        } else if (editTextPsw!!.text.toString().equals("")) {
                            editTextPsw!!.setError(getString(R.string.error_register_empty_psw))
                            editTextConfirmPsw!!.setError(getString(R.string.error_register_empty_psw))
                            Toast.makeText(this, getString(R.string.error_register_empty_psw), Toast.LENGTH_SHORT).show()
                        } else if (editTextPsw!!.text.toString().length < 5) {
                            editTextPsw!!.setError(getString(R.string.error_register_short_psw))
                            editTextConfirmPsw!!.setError(getString(R.string.error_register_short_psw))
                            Toast.makeText(this, getString(R.string.error_register_short_psw), Toast.LENGTH_SHORT).show()
                        }
                        if (editTextName!!.text.toString().equals("")) {
                            editTextName!!.setError(getString(R.string.error_register_empty_name))
                            Toast.makeText(this, getString(R.string.error_register_empty_name), Toast.LENGTH_SHORT).show()
                        }

                        if (editTextSurname!!.text.toString().equals("")) {
                            editTextSurname!!.setError(getString(R.string.error_register_empty_surname))
                            Toast.makeText(this, getString(R.string.error_register_empty_surname), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting sign in methods for user", exception)
                }
        }
        else{
            if (!(editTextEmail!!.text.toString()
                    .equals(editTextConfirmMail!!.text.toString()))
            ) {
                editTextEmail!!.setError(getString(R.string.error_register_copy_mail))
                editTextConfirmMail!!.setError(getString(R.string.error_register_copy_mail))
                Toast.makeText(this, getString(R.string.error_register_copy_mail), Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(editTextEmail!!.text.toString())) {
                editTextEmail!!.setError(getString(R.string.error_invalid_mail))
                editTextConfirmMail!!.setError(getString(R.string.error_invalid_mail))
                Toast.makeText(this, getString(R.string.error_invalid_mail), Toast.LENGTH_SHORT).show()
            }
            if (!(editTextPsw!!.text.toString()
                    .equals(editTextConfirmPsw!!.text.toString()))
            ) {
                editTextPsw!!.setError(getString(R.string.error_register_copy_psw))
                editTextConfirmPsw!!.setError(getString(R.string.error_register_copy_psw))
                Toast.makeText(this, getString(R.string.error_register_copy_psw), Toast.LENGTH_SHORT).show()
            } else if (editTextPsw!!.text.toString().equals("")) {
                editTextPsw!!.setError(getString(R.string.error_register_empty_psw))
                editTextConfirmPsw!!.setError(getString(R.string.error_register_empty_psw))
                Toast.makeText(this, getString(R.string.error_register_empty_psw), Toast.LENGTH_SHORT).show()
            } else if (editTextPsw!!.text.toString().length < 5) {
                editTextPsw!!.setError(getString(R.string.error_register_short_psw))
                editTextConfirmPsw!!.setError(getString(R.string.error_register_short_psw))
                Toast.makeText(this, getString(R.string.error_register_short_psw), Toast.LENGTH_SHORT).show()
            }
            if (editTextName!!.text.toString().equals("")) {
                editTextName!!.setError(getString(R.string.error_register_empty_name))
                Toast.makeText(this, getString(R.string.error_register_empty_name), Toast.LENGTH_SHORT).show()
            }

            if (editTextSurname!!.text.toString().equals("")) {
                editTextSurname!!.setError(getString(R.string.error_register_empty_surname))
                Toast.makeText(this, getString(R.string.error_register_empty_surname), Toast.LENGTH_SHORT).show()
            }


        }

    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

}

