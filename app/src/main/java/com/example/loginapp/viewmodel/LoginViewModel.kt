package com.example.loginapp.viewmodel

import LoginRequest
import android.app.Dialog
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.loginapp.LoginErrResponse
import com.example.loginapp.LoginResponse
import com.example.loginapp.RegisterErrResponse
import com.example.loginapp.RegisterResponse
import com.example.loginapp.viewmodel.RegisterViewModel.RegistrationResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginViewModel : ViewModel() {

    private val _loginStatus = MutableLiveData<LoginResult>()
    val loginStatus: LiveData<LoginResult> get() = _loginStatus

    private val _resetPWStatus = MutableLiveData<String>()
    val resetPWStatus: LiveData<String> get() = _resetPWStatus

    private val client = OkHttpClient()

    sealed class LoginResult {
        data class Success(val response: LoginResponse) : LoginResult()
        data class Error(val message: String?) : LoginResult()
    }


    //Login Function
    fun login(username: String, password: String) {

        val backEndURL = "http://10.0.2.2:8080/api/v1/login"
        val requestData = LoginRequest(username, password)
        val userLoginData = Json.encodeToString(requestData)
        val requestBody = userLoginData.toRequestBody(("application/json; charset=utf-8").toMediaType())

        val request = Request.Builder()
            .url(backEndURL)
            .post(requestBody)
            .build()

//        Log.d("LoginActivity", "userLoginData: $userLoginData")
        // Does the async http request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                Log.e("LoginActivity", "Network error: ${e.message}")
                _loginStatus.postValue(LoginResult.Error(e.message))
            }

            override fun onResponse(call: Call, response: Response) {
//                Log.d("LoginActivity", "onRsponse")
                val responseBody = response.body?.string()
//                Log.d("LoginActivity", "responseBody1, $responseBody")
                if (response.isSuccessful && responseBody != null) {
//                    Log.d("LoginActivity", "responseBody: $responseBody")
                    val loginResponse = Json.decodeFromString<LoginResponse>(responseBody)
//                    Log.d("LoginActivity", "loginResponse: $loginResponse")
                    handleLoginResponse(loginResponse)
                } else {

                    if (responseBody != null) {
                        try {
                            val errorResponse = Json.decodeFromString<LoginErrResponse>(responseBody)
                            Log.e("LoginActivity", "loginErrResponse: $errorResponse")
                            _loginStatus.postValue(LoginResult.Error(errorResponse.message))
                        } catch (e: Exception) {
                            // Fallback in case the error response structure doesn't match
                            Log.e("LoginActivity", "loginErrResponse Exception")
                            _loginStatus.postValue(LoginResult.Error("Login failed: ${response.message}"))
                        }
                    } else {
                        Log.e("LoginActivity", "loginErrResponse not successful and null")
                        _loginStatus.postValue(LoginResult.Error("Login failed: ${response.message}"))
                    }
                }
            }
        })

    }

    private fun handleLoginResponse(responseData: LoginResponse) {

                Log.d("LoginActivity", "responseData  $responseData")
        Log.d("LoginActivity", "Response status: ${responseData.status}")
        Log.d("LoginActivity", "Response message: ${responseData.message}")
//        Log.d("RegisterActivity", "User ID: ${responseData.data.userId}")
        _loginStatus.postValue(LoginResult.Success(responseData))
    }

     fun resetPassword(email: String, dialog: Dialog) {

        if (email.isEmpty()) {
            _resetPWStatus.value = "Please enter your email!"
            return
        }

        val backEndURL = "http://your-backend-url/resetPassword"

        val passwordResetData = JSONObject().apply(){
            put("email", email)
        }
        // Convert JSON object to request body
        val requestBody = passwordResetData.toString().toRequestBody(("application/json; charset=utf-8").toMediaType())

        // Create HTTP Request
        val request = Request.Builder()
            .url(backEndURL)
            .post(requestBody)
            .build()

        // Do an async request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                _resetPWStatus.postValue("Network error: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    _resetPWStatus.postValue("Password reset link sent to $email")
                } else {
                    _resetPWStatus.postValue("Failed to send reset link!")
                }

            }
        })
    }
}
