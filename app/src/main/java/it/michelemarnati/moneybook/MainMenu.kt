package it.michelemarnati.moneybook

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.GenericTypeIndicator








class MainMenu: AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var spinner: Spinner
    private lateinit var table_transactions: TableLayout
    private lateinit var user_transactions: ArrayList<UserTransaction>
    private lateinit var addTransactionButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        //Initialize variables
        init()

        //Get user transactions from Firebase database
        getUserTransactionsFromDB()

        //User add a transaction by clicking the button "AGGIUNGI"
        addTransactionButton!!.setOnClickListener {
            addTransaction()
        }


    }

    fun init(){
        //get the Firebase Database
        database = FirebaseDatabase.getInstance("https://moneybook-86d63-default-rtdb.europe-west1.firebasedatabase.app").reference

        //Initialize the Firebase auth
        auth = Firebase.auth

        //Spinner contains all types of transactions
        spinner = findViewById(R.id.transaction_type_spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.transaction_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }


        table_transactions = findViewById(R.id.tableResults)
        user_transactions = ArrayList<UserTransaction>()
        addTransactionButton = findViewById(R.id.addTransactionsBtn)
    }

    fun addTransaction(){
        //Get infos of user transaction
        val date = findViewById(R.id.date_transaction) as EditText
        val description = findViewById(R.id.transaction) as EditText
        val import = findViewById(R.id.import_transaction) as EditText
        val type = findViewById(R.id.transaction_type_spinner) as Spinner

        //Serialize infos
        val single_transaction = ArrayList<UserTransaction>()
        single_transaction.add(UserTransaction(date.text.toString(), description.text.toString(), import.text.toString().toFloat(), type.selectedItem.toString()))
        //Insert user transaction in table
        populateTableTransactions(single_transaction)

//        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val t: GenericTypeIndicator<List<UserTransaction>> =
//                    object : GenericTypeIndicator<List<UserTransaction>>() {}
//                user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>
//
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException())
//            }
//        })
    }


    fun getUserTransactionsFromDB(){

        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<List<UserTransaction>> =
                    object : GenericTypeIndicator<List<UserTransaction>>() {}
                user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>

                //Insert user transactions in table
                populateTableTransactions(user_transactions)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException())
            }
        })

    }


    fun populateTableTransactions(user_transactions: ArrayList<UserTransaction>){
        for(transaction in user_transactions) run {
            val date = TextView(this@MainMenu)
            date.layoutParams = TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT)
            date.gravity = 1
            date.text = transaction.date
            val description = TextView(this@MainMenu)
            description.layoutParams = TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT)
            description.gravity = 1
            description.text = transaction.description
            val import = TextView(this@MainMenu)
            import.layoutParams = TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT)
            import.gravity = 1
            import.text = transaction.import.toString()
            val type = TextView(this@MainMenu)
            type.layoutParams = TableRow.LayoutParams(
                android.widget.TableRow.LayoutParams.WRAP_CONTENT,
                android.widget.TableRow.LayoutParams.WRAP_CONTENT)
            type.gravity = 1
            type.text = transaction.type

            //adding columns into row
            val trow = TableRow(this@MainMenu)
            trow.setPadding(10,10,10,10)
            trow.addView(date)
            trow.addView(description)
            trow.addView(type)
            trow.addView(import)
            table_transactions.addView(trow)
        }
    }
}