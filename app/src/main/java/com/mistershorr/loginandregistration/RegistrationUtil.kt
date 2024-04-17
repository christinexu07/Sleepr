package com.mistershorr.loginandregistration

import android.util.Log

// object keyword makes it so all the functions are
// static functions
object RegistrationUtil {
    // use this in the test class for the is username taken test
    // make another similar list for some taken emails
    var existingUsers = mutableListOf<String>()
    var existingEmails = mutableListOf<String>()
    fun addUsers(username:String, email:String){
        existingUsers.add(username)
        existingEmails.add(email)
    }


    // isn't empty
    // isn't already taken
    // minimum number of characters is 3
    fun validateUsername(username: String) : Boolean {
        if(username!=null && username.length>2 && !existingUsers.contains(username)){
            return true
        }
        return false
    }

    // make sure meets security requirements (deprecated ones that are still used everywhere)


    fun validatePassword(password : String, confirmPassword: String) : Boolean {
        //if its null
        if(password==null || confirmPassword==null){
            return false
        }
        //if its too short
        else if(password.length < 8 || confirmPassword.length < 8) {
            return false
        }
        // if they don't match
        else if(!password.equals(confirmPassword)){
            return false
        }
        // if there's no digit
        else if(password.count { it.isDigit() } < 1){
            return false
        }
        else if(password.count{it.isUpperCase()}<1){
            return false
        }
        return true
    }

    // isn't empty
    fun validateName(name: String) : Boolean {
        if(name.isNotEmpty() ){
                return true
            }
            return false
    }

    // isn't empty
    // make sure the email isn't used
    // make sure it's in the proper email format user@domain.tld
    fun validateEmail(email: String) : Boolean {
        if(email.isNotEmpty()&&!existingEmails.contains(email)&&android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            return true
        }

        return false
    }
}