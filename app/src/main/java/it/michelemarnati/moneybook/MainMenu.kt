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
import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainMenu: AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var spinner: Spinner
    private lateinit var table_transactions: TableLayout
    private lateinit var user_transactions: ArrayList<UserTransaction>
    private lateinit var addTransactionButton: Button
    private lateinit var bottomNavView: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)
        bottomNavView = findViewById(R.id.bottom_navigation) as BottomNavigationView
        bottomNavView.setSelectedItemId(R.id.action_recents);
        bottomNavView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_recents -> {
                }
                R.id.action_months -> {
                }
                R.id.action_user_settings -> {
                }
            }
            true
        }
        loadFragment(Home())




    }


    fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.commit()
    }
    //function that hides Keyboard when called
    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager: InputMethodManager = activity.getSystemService(
            INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }


}