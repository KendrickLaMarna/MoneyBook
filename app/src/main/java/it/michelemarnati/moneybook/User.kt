package it.michelemarnati.moneybook

class User {
    lateinit var name: String
    lateinit var surname: String
    lateinit var email: String

    constructor(){
        //default
    }
    constructor(
        name: String,
        surname: String,
        email: String
    ) {
        this.name = name
        this.surname = surname
        this.email = email
    }


}