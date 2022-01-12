package it.michelemarnati.moneybook

import android.app.Activity
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
import android.view.Gravity
import android.view.View.SCROLLBARS_OUTSIDE_OVERLAY
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.util.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import androidx.core.view.setPadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.whiteelephant.monthpicker.MonthPickerDialog
import java.lang.Exception
import java.text.SimpleDateFormat
import android.widget.LinearLayout
import android.widget.AdapterView
import androidx.core.text.isDigitsOnly


class Statistics: Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var user_transactions: ArrayList<UserTransaction>
    private lateinit var data_statistiche:EditText
    private lateinit var spinner: Spinner
    private lateinit var statistics_selector: Spinner
    private lateinit var searchButton: Button
    private lateinit var table_transactions: TableLayout
    private lateinit var pb: ProgressBar

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

        //Initialize circle progress bar
        pb = view?.findViewById(R.id.progressBar) as ProgressBar




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

            //Hide keyboard
            activity?.let { it1 -> hideSoftKeyboard(it1) }

            //Load circle progress bar
            pb.visibility = View.VISIBLE
            if(checkData()){
                refresh()

                getUserTransactionsFromDB(data_statistiche.text.toString(), spinner.selectedItem.toString())
            }
            else{
                pb.visibility = View.GONE
            }
        }

        //Initialize spinner which contains statistics selector
        statistics_selector = view?.findViewById<View>(R.id.statistics_selector_spinner) as Spinner
        this.context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.statistics_selection_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                statistics_selector.adapter = adapter
            }
        }

        statistics_selector.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                data_statistiche.setOnClickListener { null }
                data_statistiche.text = null
                refresh()
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Statistiche mensili") {
                    data_statistiche.setOnClickListener {
                        data_statistiche.setError(null)
                        var builder: MonthPickerDialog.Builder  = MonthPickerDialog.Builder(context,
                            { i: Int, i1: Int ->
                                data_statistiche.text = Editable.Factory.getInstance().newEditable(if((i + 1) < 10){"0" + (i + 1).toString() + "/" + i1} else (i + 1).toString() + "/" + i1)
                            }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH));

                        builder.setActivatedMonth(myCalendar.get(Calendar.MONTH))
                            .setMinYear(1990)
                            .setActivatedYear(myCalendar.get(Calendar.YEAR))
                            .setMaxYear(2050)
                            .setTitle("Select trading month")
                            .build().show();
                    }


                }
                else if (selectedItem == "Statistiche annuali") {
                    data_statistiche.setOnClickListener {
                        data_statistiche.setError(null)
                        var builder: MonthPickerDialog.Builder  = MonthPickerDialog.Builder(context,
                            { i: Int, i1: Int ->
                                data_statistiche.text = Editable.Factory.getInstance().newEditable(i1.toString())
                            }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH));

                        builder.setActivatedMonth(myCalendar.get(Calendar.MONTH))
                            .setMinYear(myCalendar.get(Calendar.YEAR) - 30)
                            .setActivatedYear(myCalendar.get(Calendar.YEAR))
                            .setMaxYear(myCalendar.get(Calendar.YEAR) + 30)
                            .setTitle("Select year")
                            .showYearOnly()
                            .build().show();
                    }

                }

            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })





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
        var tvEntrate = view?.findViewById(R.id.entrate) as TextView
        var tvUscite = view?.findViewById(R.id.uscite) as TextView
        var tvTotale = view?.findViewById(R.id.totale) as TextView

        database.child("users").child(auth.currentUser!!.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val t: GenericTypeIndicator<List<UserTransaction>> =
                    object : GenericTypeIndicator<List<UserTransaction>>() {}

                if(dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction> != null){
                    user_transactions = dataSnapshot.child("transactions").getValue(t) as ArrayList<UserTransaction>

                    //check if stats are monthly or for years
                    if(!dataStats.isDigitsOnly()){
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
                        }
                        tvTotale.text = tvTotale.text.toString() + " " + bilancio.toString() + " €"
                        tvEntrate.text = tvEntrate.text.toString() + " +" + entrate.toString() + " €"
                        tvUscite.text = tvUscite.text.toString() + " -" + uscite.toString() + " €"
                        populateTableTransactions(user_monthTransactions)
                    }
                    //statistics for year
                    else{
                        if(type.equals("Tutte")){
                            for(transaction in user_transactions) run{
                                val dateSplit = transaction.date.split("/")
                                val ds = dateSplit[2]
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
                                val ds = dateSplit[2]

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
                        }
                        tvTotale.text = tvTotale.text.toString() + " " + bilancio.toString() + " €"
                        tvEntrate.text = tvEntrate.text.toString() + " +" + entrate.toString() + " €"
                        tvUscite.text = tvUscite.text.toString() + " -" + uscite.toString() + " €"
                        populateTableTransactions(user_monthTransactions)

                    }

                }
                pb.visibility = View.GONE
            }


            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(ContentValues.TAG, "onCancelled", databaseError.toException())
            }
        })

    }

    fun populateTableTransactions(user_transactions: ArrayList<UserTransaction>){
        //Adding an initial line
        var rowSep = TableRow(this.context)
        var line = View(this.context)
        line.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT, 1)
        line.setBackgroundColor(getResources().getColor(R.color.black))
        var lp = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 10, 0, 10)
        rowSep.layoutParams = lp
        rowSep.addView(line)
        table_transactions.addView(rowSep)
        for(transaction in user_transactions) run {
            var date = TextView(this.context)
            date.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                200)
            date.gravity = Gravity.CENTER
            date.text = transaction.date
            date.setPadding(10)
            var description = TextView(this.context)
            description.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            description.gravity = Gravity.CENTER
            description.text = transaction.description
            description.setPadding(10)
            var import = TextView(this.context)
            import.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT)
            import.gravity = Gravity.CENTER
            import.text = when(transaction.type){
                "Stipendio" -> {
                    "+" + transaction.import.toString() + "€"
                }
                "Entrate varie" -> {
                    "+" + transaction.import.toString() + "€"
                }
                else -> {
                    "-" + transaction.import.toString() + "€"
                }
            }
            import.setPadding(10)



            //adding columns into row
            var trow = TableRow(this.context)
            //trow.setPadding(10,10,10,10)
            trow.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                0, 1F)
            trow.addView(date)
            trow.addView(description)
            trow.addView(import)
            table_transactions.addView(trow, TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT))
            table_transactions.setColumnStretchable(1, true);
            table_transactions.setColumnStretchable(2, true);

            //Adding a line to separate rows
            var rowSep = TableRow(this.context)
            var line = View(this.context)
            line.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 1)
            line.setBackgroundColor(getResources().getColor(R.color.black))
            var lp = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, 10, 0, 10)
            rowSep.layoutParams = lp
            rowSep.addView(line)
            table_transactions.addView(rowSep)
        }
    }


    private fun refresh(){
        var tvEntrate = view?.findViewById(R.id.entrate) as TextView
        var tvUscite = view?.findViewById(R.id.uscite) as TextView
        var tvTotale = view?.findViewById(R.id.totale) as TextView
        tvTotale.text = "Totale:"
        tvEntrate.text = "Entrate:"
        tvUscite.text = "Uscite:"
        table_transactions.removeAllViews()
    }
    //Function that hides Keyboard when called
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



}