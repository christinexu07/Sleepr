package com.mistershorr.loginandregistration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.mistershorr.loginandregistration.databinding.ActivityRegistrationBinding


class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    companion object{
        var TAG="bloop"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // retrieve any information from the intent using the extras keys
        val username = intent.getStringExtra(LoginActivity.EXTRA_USERNAME) ?: ""
        val password = intent.getStringExtra(LoginActivity.EXTRA_PASSWORD) ?: ""

        // prefill the username & password fields
        // for EditTexts, you actually have to use the setText functions
        binding.editTextRegistrationUsername.setText(username)
        binding.editTextTextPassword.setText(password)

        // register an account and send back the username & password
        // to the login activity to prefill those fields
        binding.buttonRegistrationRegister.setOnClickListener {
            Log.d(TAG, "onCreate: register button worked")
            val password = binding.editTextTextPassword.text.toString()
            val confirm = binding.editTextRegistrationConfirmPassword.text.toString()
            val username = binding.editTextRegistrationUsername.text.toString()
            val email = binding.editTextRegistrationEmail.text.toString()
            val name=binding.editTextRegistrationName.text.toString()
            if(RegistrationUtil.validatePassword(password, confirm) &&
                RegistrationUtil.validateUsername(username)&&
                RegistrationUtil.validateEmail(email)&&
                RegistrationUtil.validateName(name))  {
                //register the user on backendless following the documentation
                //and in the onHandleResponse, that's where we make the resultIntent and go back
                RegistrationUtil.addUsers(username,email)
                val user = BackendlessUser()
                user.setProperty("email", email)
                user.setProperty("username", username)
                user.setProperty("name",name)
                user.password = password

                Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser?> {
                    override fun handleResponse(registeredUser: BackendlessUser?) {
                        // user has been registered and now can login
                        val resultIntent = Intent().apply {
                            putExtra(LoginActivity.EXTRA_USERNAME, binding.editTextRegistrationUsername.text.toString())
                            putExtra(LoginActivity.EXTRA_PASSWORD, password)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        // an error has occurred, the error code can be retrieved with fault.getCode()
                        Log.d(TAG, "handleFault: userregistrationfailed")
                        Toast.makeText(this@RegistrationActivity,"Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else{
                Toast.makeText(this, "bad registration info", Toast.LENGTH_SHORT).show()
            }
        }

    }
}