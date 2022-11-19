package edu.bluejack22_1.JoCards

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.redirectToRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(this)
        if(isLoginGoogle != null){
            Log.v("jojojo","uda login google " + isLoginGoogle.email.toString())
            var intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("email", isLoginGoogle.email.toString())
            startActivity(intent)
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val isLoginAuth = firebaseAuth.currentUser
        if(isLoginAuth != null){
            Log.v("jojojo","uda login firebase auth " + isLoginAuth.email.toString())
            var intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.signInButton.setOnClickListener{

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("verified your credential...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

//            val email = findViewById<EditText>(R.id.email_input)

            if(email.isEmpty() || password.isEmpty()){
                if(progressDialog.isShowing) progressDialog.dismiss()
                popUpModal("all field must be filled!")
            }else {
                Log.v("jojojo", email + " " + password)
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    if(it.isSuccessful) {
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        var intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        popUpModal("invalid email or password!!")
                    }
                }
            }
        }

        // init before the google button is clicked for overheading internet issues
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInButton.setOnClickListener{
            signInGoogle()
        }
    }

    fun popUpModal(message: String){
        val dialog = Dialog(this@LoginActivity)
        dialog.setContentView(R.layout.modal_notification)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val modal_message = dialog.findViewById<TextView>(R.id.modal_message)
        modal_message.setText(message)
        val close_button = dialog.findViewById<ImageView>(R.id.close_button)
        close_button?.setOnClickListener{
            dialog.dismiss()
        }
    }

    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("verified your credential...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        } else {
            if(progressDialog.isShowing) progressDialog.dismiss()
            popUpModal("login canceled!!")
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if(account != null) {
                updateUI(account)
            }
        } else {
            if(progressDialog.isShowing) progressDialog.dismiss()
            popUpModal(task.exception.toString())
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful) {
                val db = Firebase.firestore
                db.collection("UserDetail").document(account.email.toString()).get().addOnCompleteListener { res ->
                    if(res.isSuccessful) {
                        val document = res.result
                        if(document.data == null) {
                            val user = hashMapOf(
                                "email" to account.email,
                                "username" to account.displayName,
                                "fullName" to account.displayName,
                                "picture" to "default_picture.jpg"
                            )
                            db.collection("UserDetail").document(account.email.toString()).set(user).addOnSuccessListener { documentReference ->
                                Log.v("jojojo", "New user is sign in with google, create UserDetail collection!!")
                            }.addOnCompleteListener { userDetail ->
                                if(userDetail.isSuccessful) {

                                    val dailyCardTarget = hashMapOf(
                                        "email" to account.email,
                                        "quantity" to 20
                                    )
                                    db.collection("DailyCardTarget").document(account.email.toString()).set(dailyCardTarget).addOnCompleteListener {
                                        if(it.isSuccessful){
                                            val monthlyCardTarget = hashMapOf(
                                                "email" to account.email,
                                                "quantity" to 500
                                            )
                                            db.collection("MonthlyCardTarget").document(account.email.toString()).set(monthlyCardTarget).addOnCompleteListener {
                                                if(it.isSuccessful){
                                                    if(progressDialog.isShowing) progressDialog.dismiss()
                                                    var intent = Intent(this, HomeActivity::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if(progressDialog.isShowing) progressDialog.dismiss()
                            var intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        Log.v("jojojo", "failed to get user detail collection")
                    }
                }
            } else {
                if(progressDialog.isShowing) progressDialog.dismiss()
                popUpModal(it.exception.toString())
            }
        }
    }

    override fun onBackPressed() {
        finish()
        finishAffinity()
    }
}