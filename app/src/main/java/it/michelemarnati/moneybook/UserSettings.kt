package it.michelemarnati.moneybook

import android.app.AlertDialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import android.os.Build
import androidx.fragment.app.FragmentTransaction


class UserSettings: Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var pb: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.user_settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Initialize variables
        init()
        fillUserInfos()
        pb.visibility = View.GONE
    }

    private fun init(){
        //Load circle progress bar
        pb = view?.findViewById(R.id.progressBar) as ProgressBar
        pb.visibility = View.VISIBLE

        //get the Firebase Database
        database = FirebaseDatabase.getInstance("https://moneybook-86d63-default-rtdb.europe-west1.firebasedatabase.app").reference

        //Initialize the Firebase auth
        auth = Firebase.auth

        val view = view

        val tvChangePw = view?.findViewById(R.id.editTextChangePassword) as TextView
        tvChangePw.setOnClickListener {
            tvChangePw.setTextColor(getResources().getColorStateList(R.color.blue_eyes))
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setMessage("Vuoi davvero cambiare la password?")
            dialogBuilder.setPositiveButton("SI",
                DialogInterface.OnClickListener { dialog, whichButton -> changePassword()})
            dialogBuilder.setNegativeButton("NO",
                DialogInterface.OnClickListener { dialog, whichButton -> })
            val b = dialogBuilder.create()
            b.show()
        }

        val saveButton = view?.findViewById(R.id.saveUserSettings) as Button
        saveButton.setOnClickListener{
            pb.visibility = View.VISIBLE
            val name = view?.findViewById(R.id.editTextName) as EditText
            val surname = view?.findViewById(R.id.editTextSurname) as EditText
            if(checkData()){
                updateUserInfos(name.text.toString(), surname.text.toString())
            }
            else{
                pb.visibility = View.GONE
            }

        }

        val deleteButton = view?.findViewById(R.id.deleteUserButton) as Button
        deleteButton.setOnClickListener{
            pb.visibility = View.VISIBLE
            deleteUser()

        }








    }

    private fun fillUserInfos(){
        val etName = view?.findViewById(R.id.editTextName) as EditText
        val etSurname = view?.findViewById(R.id.editTextSurname) as EditText
        val etMail = view?.findViewById(R.id.editTextMail) as TextView

        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<String> =
                    object : GenericTypeIndicator<String>() {}
                etName.text = Editable.Factory.getInstance().newEditable(dataSnapshot.child("name").getValue(t) as String)
                etSurname.text = Editable.Factory.getInstance().newEditable(dataSnapshot.child("surname").getValue(t) as String)
                etMail.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_light_disabled))
                etMail.text = Editable.Factory.getInstance().newEditable(dataSnapshot.child("email").getValue(t) as String)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })

    }

    private fun checkData(): Boolean{
        var result = true
        val etName = view?.findViewById(R.id.editTextName) as EditText
        val etSurname = view?.findViewById(R.id.editTextSurname) as EditText

        if(etName.text.toString().equals("") && etSurname.text.toString().equals("")){
            etName.setError(getString(R.string.error_register_empty_name))
            etSurname.setError(getString(R.string.error_register_empty_surname))
            Toast.makeText(this.context, getString(R.string.error_empty_name_surname), Toast.LENGTH_SHORT).show()
            result = false
        }
        else if(etName.text.toString().equals("")){
            etName.setError(getString(R.string.error_register_empty_name))
            Toast.makeText(this.context, getString(R.string.error_register_empty_name), Toast.LENGTH_SHORT).show()
            result = false
        }
        else if(etSurname.text.toString().equals("")){
            etSurname.setError(getString(R.string.error_register_empty_surname))
            Toast.makeText(this.context, getString(R.string.error_register_empty_surname), Toast.LENGTH_SHORT).show()
            result = false
        }

        return result

    }
    private fun updateUserInfos(name: String, surname: String){
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<String> =
                    object : GenericTypeIndicator<String>() {}

                database.child("users").child(auth.currentUser!!.uid).child("name").setValue(name)
                database.child("users").child(auth.currentUser!!.uid).child("surname").setValue(surname)
                pb.visibility = View.GONE
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder.setMessage("Operazione eseguita con successo!")
                dialogBuilder.setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, whichButton -> })
                val b = dialogBuilder.create()
                b.show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })

    }



    private fun changePassword(){
        pb.visibility = View.VISIBLE
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<String> =
                    object : GenericTypeIndicator<String>() {}
                Firebase.auth.sendPasswordResetEmail(dataSnapshot.child("email").getValue(t) as String)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                            pb.visibility = View.GONE
                            val dialogBuilder = AlertDialog.Builder(context)
                            dialogBuilder.setMessage("Ti è stata inviata una mail di reset della password. " +
                                    "Verrai reindirizzato al menù principale.")
                            dialogBuilder.setPositiveButton("OK",
                                DialogInterface.OnClickListener { dialog, whichButton ->
                                    dialog.dismiss()
                                    val mainMenuWindowIntent = Intent(requireActivity().applicationContext, MainActivity::class.java)
                                    startActivity(mainMenuWindowIntent)
                                    requireActivity().finish()})
                            val b = dialogBuilder.create()
                            b.show()
                        }
                        else{
                            pb.visibility = View.GONE
                        }
                    }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })
    }



    private fun deleteUser(){
        database.child("users").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<String> =
                    object : GenericTypeIndicator<String>() {}

                database.child("users").child(auth.currentUser!!.uid).removeValue()
                val user = Firebase.auth.currentUser!!
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            pb.visibility = View.GONE
                            Log.d(TAG, "User account deleted.")
                            val dialogBuilder = AlertDialog.Builder(context)
                            dialogBuilder.setMessage("Account eliminato con successo!")
                            dialogBuilder.setPositiveButton("OK",
                                DialogInterface.OnClickListener { dialog, whichButton ->
                                    dialog.dismiss()
                                    val mainMenuWindowIntent = Intent(requireActivity().applicationContext, MainActivity::class.java)
                                    startActivity(mainMenuWindowIntent)
                                    requireActivity().finish() })
                            val b = dialogBuilder.create()
                            b.show()
                        }
                        else{
                            pb.visibility = View.GONE
                        }
                    }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })
    }





}