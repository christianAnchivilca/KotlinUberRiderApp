package com.example.kotlinriderapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlinriderapp.Common.Common
import com.example.kotlinriderapp.Model.RiderModel
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

class SplashScreenActivity : AppCompatActivity() {
    //variables staticas
    companion object{
        private val REQUEST_CODE_LOGIN = 1212
    }

    lateinit var providers:List<AuthUI.IdpConfig>
    lateinit var firebaseAuth:FirebaseAuth
    lateinit var listener:FirebaseAuth.AuthStateListener

    private lateinit var database:FirebaseDatabase
    private lateinit var riderInfoRef:DatabaseReference

    override fun onStart() {
        super.onStart()
        delaySplashScreen()
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    private fun delaySplashScreen() {
        Completable.timer(3,TimeUnit.SECONDS,AndroidSchedulers.mainThread())
            .subscribe({
                firebaseAuth.addAuthStateListener(listener)
            })

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        init()
    }

    fun init(){
        //get database reference
        database= FirebaseDatabase.getInstance()
        riderInfoRef = database.getReference(Common.RIDER_INFO_REFERENCE)
        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        listener = FirebaseAuth.AuthStateListener {
            myFirebaseAuth->
            val user = myFirebaseAuth.currentUser
            if (user != null){
                checkUserFromFirebase()
            }else{
                showLoginLayout()
            }

        }
    }

    private fun showLoginLayout() {

    }

    private fun checkUserFromFirebase() {

        riderInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                   Toast.makeText(this@SplashScreenActivity,""+p0.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val model = p0.getValue(RiderModel::class.java)
                        irAlActivityHome(model!!)

                    }else{
                        showRegisterLayout()
                    }

                }

            })

    }

    private fun irAlActivityHome(model:RiderModel) {
        Common.currentRider = model

    }

    private fun showRegisterLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.layout_sign_in)
            .setPhoneButtonId(R.id.btn_phone_sign_in)
            .setGoogleButtonId(R.id.btn_google_sign_in)
            .build()

        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAuthMethodPickerLayout(authMethodPickerLayout)
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false).build(), REQUEST_CODE_LOGIN)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOGIN){
            val respuesta = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser
            }
            else
                Toast.makeText(this,""+respuesta!!.error!!.message,Toast.LENGTH_LONG).show()
        }
    }
}