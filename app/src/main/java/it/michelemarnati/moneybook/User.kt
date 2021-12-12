package it.michelemarnati.moneybook

class User {
    lateinit var name: String
    lateinit var surname: String
    lateinit var email: String
    lateinit var password: String
    lateinit var transactions: ArrayList<UserTransaction>

    constructor(){
        //default
    }
    constructor(
        name: String,
        surname: String,
        email: String,
        password: String,
        transactions: ArrayList<UserTransaction>
    ) {
        this.name = name
        this.surname = surname
        this.email = email
        this.password = password
        this.transactions = transactions
    }


}