package com.camerapet.debug.data.repository

import android.util.Log
import androidx.lifecycle.liveData
import com.camerapet.debug.data.common.setStringEventListener
import com.camerapet.debug.data.common.setStringListener
import com.camerapet.debug.data.wrappers.LoginResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class UserRepository @Inject constructor() {

    val firebaseAuth = FirebaseAuth.getInstance()

    fun getUserToken(): String? = firebaseAuth.currentUser?.uid

    fun checkUserAuth(): Boolean {
        Log.d("asdasd", (firebaseAuth.currentUser != null).toString())
        return firebaseAuth.currentUser != null
    }

    fun login(mail: String, pass: String, callback: (LoginResult) -> Unit) =
        firebaseAuth.signInWithEmailAndPassword(mail, pass)
            .addOnCanceledListener {
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnSuccessListener {
                //callback(LoginResult.SUCCESS)
            }
            .addOnCompleteListener {
                if (it.isSuccessful) callback(LoginResult.SUCCESS)
                else callback(LoginResult.ERROR)
            }

    fun create(mail: String, pass: String, callback: (LoginResult) -> Unit) =
        firebaseAuth.createUserWithEmailAndPassword(mail, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) callback(LoginResult.SUCCESS)
                else callback(LoginResult.ERROR)
            }

    fun createInDb(mail: String, listener: (LoginResult) -> Unit) {
        val data = hashMapOf(
            "user" to mail,
            "userId" to getUserToken(),
            "tag" to UUID.randomUUID().toString()
        )
        Firebase.firestore.collection("users").document(getUserToken()!!).set(data)
            .addOnCompleteListener {
                if (it.isSuccessful) listener(LoginResult.SUCCESS)
                else listener(LoginResult.ERROR)
            }
    }

    fun updateUserState(state: String) =
        Firebase.firestore.collection("users").document(getUserToken()!!)
            .update("state", state)

    //TODO convert to flow
    suspend fun getState(): String? =
        Firebase.firestore.collection("users").document(getUserToken()!!).setStringListener("state")

    //TODO convert to flow
    suspend fun getUserTag(): String? =
        Firebase.firestore.collection("users").document(getUserToken()!!).setStringListener("tag")

    fun getUserTag(tagListener: (String?) -> Unit) {
        Firebase.firestore.collection("users").document(getUserToken()!!)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    if (value.getString("tag") != null) {
                        tagListener(value.getString("tag"))
                    }
                }
            }
    }

    suspend fun getStateTag(tag: String, scope: CoroutineScope): Flow<String?> =
        Firebase.firestore.collection("users").document(getUserToken()!!)
            .setStringEventListener(tag, scope)

    suspend fun updateUserTag() {
        Firebase.firestore.collection("users").document(getUserToken()!!)
            .update("tag", UUID.randomUUID().toString())
    }

}