package it.michelemarnati.moneybook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.ProgressDialog.show
import android.content.ContentValues
import android.content.DialogInterface
import android.os.Build
import android.text.Editable
import android.util.Log
import android.widget.*
import java.util.*
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColor
import androidx.core.view.setPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.whiteelephant.monthpicker.MonthPickerDialog
import java.lang.Exception
import java.text.SimpleDateFormat


class Statistics: Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user_transactions: ArrayList<UserTransaction>
    private lateinit var data_statistiche:EditText
    private lateinit var spinner: Spinner
    private lateinit var searchButton: Button
    private lateinit var table_transactions: TableLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.statistics_fragment, container, false)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Initialize variables
        init()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun init(){
        val view = view
        var myCalendar = Calendar.getInstance();

        data_statistiche = view?.findViewById<View>(R.id.data_statistiche) as EditText
        //Spinner contains all types of transactions
        spinner = view?.findViewById<View>(R.id.transaction_type_spinner) as Spinner
        searchButton = view?.findViewById<View>(R.id.searchBtn) as Button
        //get the Firebase Database
        database = FirebaseDatabase.getInstance("https://moneybook-86d63-default-rtdb.europe-west1.firebasedatabase.app").reference

        //Initialize the Firebase auth
        auth = Firebase.auth

        //Initialize table of transactions
        table_transactions = view?.findViewById<View>(R.id.tableResults) as TableLayout
        data_statistiche.setOnClickListener {
            var builder: MonthPickerDialog.Builder  = MonthPickerDialog.Builder(this.context,
                { i: Int, i1: Int ->
                    data_statistiche.text = Editable.Factory.getInstance().newEditable(((i + 1).toString() + "/" + i1))
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH));

            builder.setActivatedMonth(myCalendar.get(Calendar.MONTH))
                .setMinYear(1990)
                .setActivatedYear(myCalendar.get(Calendar.YEAR))
                .setMaxYear(2050)
                .setTitle("Select trading month")
                .build().show();
        }


        // Create an ArrayAdapter using the string array and a default spinner layout
        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.searchTransaction_type_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
        }

        searchButton!!.setOnClickListener {
            if(checkData()){
                table_transactions.removeAllViews()
                getUserTransactionsFromDB(data_statistiche.text.toString(), spinner.selectedItem.toString())
            }
        }

    }

    private fun checkData():Boolean{
        var result = true

        if(data_statistiche.text.toString().equals("")){
            Toast.makeText(this.context, getString(R.string.error_empty_date), Toast.LENGTH_SHORT).show()
            data_statistiche.setError(getString(R.string.error_empty_date))
            result = false
        }

        return result
    }

    private fun getUserTransactionsFromDB(dataStats:String, type:String){
        var entrate = 0f
        var uscite = 0f
        var bilancio = 0f
        var user_monthTransactions = ArrayList<UserTransaction>()
        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<List<UserTransaction>> =
                    object : GenericTypeIndicator<List<UserTransaction>>() {}
                user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>

                if(type.equals("Tutte")){
                    for(transaction in user_transactions) run{
                        val dateSplit = transaction.date.split("/")
                        val ds = dateSplit[1] + "/" + dateSplit[2]
                        if(ds.equals(dataStats)){
                            user_monthTransactions.add(transaction)
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
                    }
                }
                else{
                    for(transaction in user_transactions) run{
                        val dateSplit = transaction.date.split("/")
                        val ds = dateSplit[1] + "/" + dateSplit[2]

                        if(ds.equals(dataStats) && transaction.type.equals(type)){
                            user_monthTransactions.add(transaction)
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
                    }
//                tvBilancio.text = "€" + bilancio.toString()
//                tvEntrateUscite.text = "+ " + entrate.toString() + " | " + "- " + uscite.toString()
                }
                populateTableTransactions(user_monthTransactions)



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



}