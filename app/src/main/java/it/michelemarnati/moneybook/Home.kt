package it.michelemarnati.moneybook

import android.app.Activity
import android.content.ContentValues
import androidx.fragment.app.Fragment


import android.os.Bundle
import android.util.Log

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

import android.app.DatePickerDialog
import java.text.SimpleDateFormat


class Home : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var date_transaction: EditText
    private lateinit var spinner: Spinner
    private lateinit var table_transactions: TableLayout
    private lateinit var user_transactions: ArrayList<UserTransaction>
    private lateinit var addTransactionButton: Button
    private var myCalendar: Calendar = Calendar.getInstance()
    private lateinit var date: DatePickerDialog.OnDateSetListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //Initialize variables
        init()

        //Display the calendar's popup when EditText of date is clicked
        date_transaction!!.setOnClickListener{
            this.context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    date,
                    myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                ).show()
            }
        }
        //User add a transaction by clicking the button "AGGIUNGI"
        addTransactionButton!!.setOnClickListener {

            //Hide keyboard
            activity?.let { it1 -> hideSoftKeyboard(it1) }

            if(checkData()){
                //Add transaction
                addTransaction()
            }

        }
    }

    fun init(){
        val view = view
        //get the Firebase Database
        database = FirebaseDatabase.getInstance("https://moneybook-86d63-default-rtdb.europe-west1.firebasedatabase.app").reference

        //Initialize the Firebase auth
        auth = Firebase.auth

        //Spinner contains all types of transactions
        spinner = view?.findViewById<View>(R.id.transaction_type_spinner) as Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.transaction_type_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
        }

        date_transaction = view?.findViewById<View>(R.id.date_transaction) as EditText
        table_transactions = view?.findViewById<View>(R.id.tableResults) as TableLayout
        user_transactions = ArrayList<UserTransaction>()
        addTransactionButton = view?.findViewById<View>(R.id.addTransactionsBtn) as Button


        //Init calendar's popup
        date = DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateLabel()
            }
        //Get Users' transactions from Firebase and populates transactions' table for the first time
        getUserTransactionsFromDB()

        //set total balance
        calculateTotalBalance()
    }

    fun addTransaction(){
        //Get infos of user transaction
        val date = view?.findViewById<View>(R.id.date_transaction) as EditText
        val description = view?.findViewById<View>(R.id.transaction) as EditText
        val import = view?.findViewById<View>(R.id.import_transaction) as EditText
        val type = view?.findViewById<View>(R.id.transaction_type_spinner) as Spinner

        //Serialize infos
        val single_transaction = ArrayList<UserTransaction>()

        single_transaction.add(UserTransaction(date.text.toString(), description.text.toString(), import.text.toString().toFloat(), type.selectedItem.toString()))
        //Insert user transaction in table
        populateTableTransactions(single_transaction)

        //Update data in Firebase database
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<List<UserTransaction>> =
                    object : GenericTypeIndicator<List<UserTransaction>>() {}
                if(dataSnapshot.child("transactions").getValue(t) != null){
                    user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>
                    user_transactions.add(single_transaction.get(0))
                    database.child("users").child(auth.currentUser!!.uid).child("transactions").setValue(user_transactions)
                    calculateTotalBalance()
                }
                else{
                    user_transactions.add(single_transaction.get(0))
                    database.child("users").child(auth.currentUser!!.uid).child("transactions").setValue(user_transactions)
                    calculateTotalBalance()
                }



            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })
    }

    //Get Users' transactions from Firebase and populates transactions' table for the first time
    fun getUserTransactionsFromDB(){

        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<List<UserTransaction>> =
                    object : GenericTypeIndicator<List<UserTransaction>>() {}
                if(dataSnapshot.child("transactions").getValue(t) != null){
                    user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>
                    //Insert user transactions in table
                    populateTableTransactions(user_transactions)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })

    }


    fun populateTableTransactions(user_transactions: ArrayList<UserTransaction>){
        for(transaction in user_transactions) run {
            val date = TextView(this.context)
            date.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            date.gravity = 1
            date.text = transaction.date
            date.setPadding(10)
            val description = TextView(this.context)
            description.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            description.gravity = 1
            description.text = transaction.description
            description.setPadding(10)
            val import = TextView(this.context)
            import.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            import.gravity = 1
            import.text = transaction.import.toString()
            import.setPadding(10)
            val type = TextView(this.context)
            type.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            type.gravity = 1
            type.text = transaction.type
            type.setPadding(10)

            //adding columns into row
            val trow = TableRow(this.context)
            trow.setPadding(10,10,10,10)
            trow.addView(date)
            trow.addView(description)
            trow.addView(import)
            trow.addView(type)
            table_transactions.addView(trow)
        }
    }

    fun calculateTotalBalance(){
        var entrate = 0f
        var uscite = 0f
        var bilancio = 0f
        val tvBilancio = view?.findViewById<View>(R.id.bilancio_totale) as TextView
        val tvEntrateUscite = view?.findViewById<View>(R.id.entrateUscite) as TextView

        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<List<UserTransaction>> =
                    object : GenericTypeIndicator<List<UserTransaction>>() {}
                if(dataSnapshot.child("transactions").getValue(t) != null){
                    user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>
                    for(transaction in user_transactions) run{
                        when(transaction.type){
                            "Stipendio" -> {
                                entrate += transaction.import
                                bilancio += transaction.import
                            }
                            "Entrate varie" -> {
                                entrate += transaction.import
                                bilancio += transaction.import
                            }
                            else -> {
                                uscite += transaction.import
                                bilancio -= transaction.import
                            }
                        }
                    }
                    tvBilancio.text = "€" + bilancio.toString()
                    tvEntrateUscite.text = "+ " + entrate.toString() + " | " + "- " + uscite.toString()
                }
                //If user hasn't transactions associated, then set only balance and cash spent/received (0)
                else{
                    tvBilancio.text = "€" + bilancio.toString()
                    tvEntrateUscite.text = "+ " + entrate.toString() + " | " + "- " + uscite.toString()
                }



            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })
    }

    //function that hides Keyboard when called
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            AppCompatActivity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }

    private fun checkData(): Boolean{
        val date = view?.findViewById<View>(R.id.date_transaction) as EditText
        val description = view?.findViewById<View>(R.id.transaction) as EditText
        val import = view?.findViewById<View>(R.id.import_transaction) as EditText
        val type = view?.findViewById<View>(R.id.transaction_type_spinner) as Spinner
        var result = true

        if(date.text.toString().equals("")){
            Toast.makeText(this.context, getString(R.string.error_empty_date), Toast.LENGTH_SHORT).show()
            date.setError(getString(R.string.error_empty_date))
            result = false
        }
        if(description.text.toString().equals("")){
            Toast.makeText(this.context, getString(R.string.error_empty_description), Toast.LENGTH_SHORT).show()
            description.setError(getString(R.string.error_empty_description))
            result = false
        }

        if(!isNumeric(import.text.toString()) || import.text.toString().equals("")){
            Toast.makeText(this.context, getString(R.string.error_invalid_import), Toast.LENGTH_SHORT).show()
            import.setError(getString(R.string.error_invalid_import))
            result = false
        }


        return result
    }

    private fun isNumeric(str: String) = str
        .replace(".", "")
        .all { it in '0'..'9' }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.ITALY)
        date_transaction.setText(dateFormat.format(myCalendar.getTime()))
    }




}