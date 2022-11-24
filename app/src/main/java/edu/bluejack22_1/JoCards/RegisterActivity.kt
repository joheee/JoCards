package edu.bluejack22_1.JoCards

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.redirectToLogin.setOnClickListener{
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        fun popUpModal(message: String){
            val dialog = Dialog(this@RegisterActivity)
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
        fun popUpModalSuccessRegister(message: String){
            val dialog = Dialog(this@RegisterActivity)
            dialog.setContentView(R.layout.modal_notification)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val modal_message = dialog.findViewById<TextView>(R.id.modal_message)
            modal_message.setText(message)
            val close_button = dialog.findViewById<ImageView>(R.id.close_button)
            close_button?.setOnClickListener{
                dialog.dismiss()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        FirebaseApp.initializeApp(this@RegisterActivity)
        firebaseAuth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        binding.signUpButton.setOnClickListener{
            val email = binding.registerEmailInput.text.toString()
            var username = binding.registerUsernameInput.text.toString()
            var password = binding.registerPasswordInput.text.toString()
            var confirm_password = binding.registerConfirmPasswordInput.text.toString()

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if(email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirm_password.isNotEmpty()) {
                if(password == confirm_password) {
                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                        if(it.isSuccessful) {
                            val user = hashMapOf(
                                "email" to email,
                                "username" to username,
                                "fullName" to username,
                                "picture" to "default_picture.jpg"
                            )
                            db.collection("UserDetail").document(email).set(user).addOnSuccessListener { documentReference ->
                                Log.v("jojojo", "DocumentSnapshot added with ID: ${documentReference.toString()}")
                            }.addOnCompleteListener {
                                if(it.isSuccessful) {
                                    val dailyCardTarget = hashMapOf(
                                        "email" to email,
                                        "quantity" to 20
                                    )
                                    db.collection("DailyCardTarget").document(email).set(dailyCardTarget).addOnCompleteListener {
                                        if(it.isSuccessful){
                                            val monthlyCardTarget = hashMapOf(
                                                "email" to email,
                                                "quantity" to 500
                                            )
                                            db.collection("MonthlyCardTarget").document(email).set(monthlyCardTarget).addOnCompleteListener {
                                                if(it.isSuccessful){
                                                    if(progressDialog.isShowing) progressDialog.dismiss()
                                                    popUpModalSuccessRegister("${getString(R.string.success_register)} ${username}" )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if(password.length < 6) {
                                if(progressDialog.isShowing) progressDialog.dismiss()
                                Log.v("jojojo","length passnya < 6")
                                popUpModal("${getString(R.string.password_must_be_6_characters_minimum)}!")
                            } else {
                                if(progressDialog.isShowing) progressDialog.dismiss()
                                Log.v("jojojo", "emailnya kepake")
                                popUpModal("${getString(R.string.email_already_in_use)}!")
                            }
                        }
                    }
                } else {
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    Log.v("jojojo","pass and con pass not same")
                    popUpModal("${getString(R.string.password_are_different_from_confirm_password)}!")
                }
            } else {
                if(progressDialog.isShowing) progressDialog.dismiss()
                Log.v("jojojo","empty field!!")
                popUpModal("${getString(R.string.all_field_must_be_filled)}!!")
            }
        }

    }
}