package com.example.loginapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
//import android.text.SpannableString
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.loginapp.viewmodel.LoginViewModel
import okhttp3.*

class LoginActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var signInButton: Button
    private lateinit var signInStatus: TextView
    private lateinit var forgotPW: TextView

    // Create to use the login View Model
    private val loginViewModel: LoginViewModel by viewModels()

    // Initialize OkHttpClient which handles HTTP responses and requests
    // controls sending and receiving data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        username = findViewById(R.id.loginEmail)
        password = findViewById(R.id.loginPassword)
        signInButton = findViewById(R.id.btnSignIn)
        signInStatus = findViewById(R.id.signIn_status)

        val newRegisterColor = resources.getColor(R.color.blue,theme)

        // Set up clickable registration text
        val register = findViewById<TextView>(R.id.registerAccount)
        val registerText = getString(R.string.registerAccount)
        val spannableString = SpannableString(registerText)

        // Set up forgot password Textview
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPassword)
        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

        val signInBtn = findViewById<Button>(R.id.btnSignIn)
        signInBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
        signInBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))


        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Redirect to the RegisterActivity when "Register now" is clicked
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = newRegisterColor// Set the color for the text
                ds.isUnderlineText = false // If you want to underline the text
            }
        }

        // Find and set the "Register now" span
        val loginStartIndex = registerText.indexOf("Register")
        spannableString.setSpan(
            clickableSpan,
            loginStartIndex,
            loginStartIndex + "Register".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //Set a foreground color for the register option in login page.
        spannableString.setSpan(
            ForegroundColorSpan(newRegisterColor),
            loginStartIndex,
            loginStartIndex + "Register".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        // Set the spannable string to the TextView
        register.text = spannableString
        register.movementMethod = LinkMovementMethod.getInstance()


        // Observe the Login Status
        loginViewModel.loginStatus.observe(this, Observer { status ->
            signInStatus.text = status
            if (status == "Login Successful!") {
                // Go to dashboard on successful login
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity
            }
        })

        // Set up login button click listener
        signInButton.setOnClickListener {
            val user = username.text.toString()
            val pass = password.text.toString()

            // Simple login validation
            if (user.isNotEmpty() && pass.isNotEmpty()) {
//                if (user == "admin" && pass == "password") { // Dummy credentials
                    // Start MainActivity on successful login
//                    val intent = Intent(this, DashboardActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
                loginViewModel.login2(user,pass)
//                loginViewModel.login(user,pass)

            } else {
                signInStatus.text = "Please enter both fields!"
                Toast.makeText(this, "Please enter both fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun showForgotPasswordDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_forgot_password)


        val emailEditText = dialog.findViewById<EditText>(R.id.etEmail)
        val btnRestartPassword = dialog.findViewById<Button>(R.id.btnRestartPassword)
        val tvBackToLogin = dialog.findViewById<TextView>(R.id.tvBackToLogin)

        btnRestartPassword.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {

                // call the viewModel to handle the password reset
                loginViewModel.resetPassword(email, dialog)

                Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
        tvBackToLogin.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}



