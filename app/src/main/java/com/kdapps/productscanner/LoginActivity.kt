package com.kdapps.productscanner

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kdapps.productscanner.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(loginListener)
        binding.btnSignUp.setOnClickListener(signUpListener)

        window.navigationBarColor = getColor(R.color.navigationBarColorSettings)
    }

    private val loginListener = View.OnClickListener{
        if(!validateLogin()) return@OnClickListener

        val emailLogin = binding.etLoginEmail.editText?.text.toString()
        val passwordLogin = binding.etLoginPassword.editText?.text.toString()

        Firebase.auth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                finish()
            } else {
                Toast.makeText(this,getString(R.string.sign_in_fail),Toast.LENGTH_LONG).show()
            }
        }
    }

    private val signUpListener = View.OnClickListener{
        if(!validateSignUp()) return@OnClickListener

        val emailSignUp     = binding.etSignUpEmail.editText?.text.toString()
        val passwordSignUp  = binding.etSignUpPassword.editText?.text.toString()

        Firebase.auth.createUserWithEmailAndPassword(emailSignUp, passwordSignUp).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this,getString(R.string.sign_up_success),Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this,getString(R.string.sign_up_failed),Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateLogin() : Boolean{

        var result = true

        val emailLogin = binding.etLoginEmail.editText?.text.toString()
        val passwordLogin = binding.etLoginPassword.editText?.text.toString()

        if(!emailLogin.isValidEmail()){
            binding.etLoginEmail.error = getString(R.string.bad_email_format)
            result = false
        }

        if(!passwordLogin.isValidPassword()){
            binding.etLoginPassword.error = getString(R.string.bad_password_format)
            result = false
        }

        return result

    }

    private fun validateSignUp() : Boolean{

        var result = true

        val emailSignUp             = binding.etSignUpEmail.editText?.text.toString()
        val passwordSignUp          = binding.etSignUpPassword.editText?.text.toString()
        val passwordSignUpConfirm   = binding.etSignUpPasswordConfirm.editText?.text.toString()

        if(!emailSignUp.isValidEmail()){
            binding.etSignUpEmail.error = getString(R.string.bad_email_format)
            result = false
        }

        if(!passwordSignUp.isValidPassword()){
            binding.etSignUpPassword.error = getString(R.string.bad_password_format)
            result = false
        }

        if(passwordSignUp != passwordSignUpConfirm){
            binding.etSignUpPasswordConfirm.error = getString(R.string.password_match_error)
            result = false
        }

        return result

    }

    fun String.isValidEmail() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun String.isValidPassword() = !TextUtils.isEmpty(this) && this.length >= 8

}