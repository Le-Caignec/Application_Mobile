package com.mobile.sharedwallet.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.sharedwallet.constants.FirebaseConstants
import com.mobile.sharedwallet.models.User
import com.mobile.sharedwallet.utils.Shared
import com.mobile.sharedwallet.utils.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GoogleSignInActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    companion object {
        private const val GOOGLE_SIGN_IN : Int = 100001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("621263219579-td0v5p3filf17br6vjtcf8hcg7qrfme3.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        signIn()
    }

    private fun signIn(){
        startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.result!!
                if(task.isSuccessful) {
                    MainScope().launch {
                        loginWithGoogle(account.idToken!!, account)
                    }
                }
            } catch (e: Exception) {
                // Google Sign In failed, update UI appropriately
                // Log.w(TAG, "Google sign in failed", e)
                print(e.message)
            }
        }
    }

    private suspend fun loginWithGoogle(token : String, google : GoogleSignInAccount) {
        try {
            val credential = GoogleAuthProvider.getCredential(token, null)

            val auth : AuthResult = FirebaseAuth
                .getInstance()
                .signInWithCredential(credential)
                .await()

            val user : User = Utils.createUserFromFirebaseUser(auth.user, true)
            user.firstName = google.givenName
            user.lastName = google.familyName

            FirebaseFirestore
                .getInstance()
                .collection(FirebaseConstants.CollectionNames.Users)
                .add(user.toFirebase())
                .await()

            Shared.user = user
            finish()
        }
        catch (e : Exception) {}
    }
}