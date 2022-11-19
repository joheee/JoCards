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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityCreateCardBinding
import edu.bluejack22_1.JoCards.databinding.ActivityHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class CreateCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCardBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener{
            finish()
        }

        val db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()
        val isFirebaseAuth = firebaseAuth.currentUser
        val isGoogleAuth = GoogleSignIn.getLastSignedInAccount(this)

        binding.createCardButton.setOnClickListener{
            val topic = binding.topicInput.text.toString()
            val question = binding.questionInput.text.toString()
            val answer = binding.answerInput.text.toString()

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("loading...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if(topic.isNotEmpty() && question.isNotEmpty() && answer.isNotEmpty()){
                db.collection("Cards").whereEqualTo("question", question).get()
                    .addOnSuccessListener {
                        documents ->
                        if(documents.isEmpty()){
                            val card = hashMapOf(
                                "creator" to if(isFirebaseAuth?.email.toString().isNotEmpty()) isFirebaseAuth?.email.toString() else isGoogleAuth?.email.toString(),
                                "topic" to topic,
                                "question" to question,
                                "answer" to answer,
                                "created" to SimpleDateFormat("dd/M/yyyy").format(Date()).toString()
                            )
                            db.collection("Cards").add(card).addOnCompleteListener {
                                if(it.isSuccessful) {
                                    if(progressDialog.isShowing) progressDialog.dismiss()
                                    popUpModalCreateCard("new card has successfully created!!")
                                } else {
                                    if(progressDialog.isShowing) progressDialog.dismiss()
                                    Log.v("jojojo", it.exception.toString())
                                }
                            }
                        } else {
                            if(progressDialog.isShowing) progressDialog.dismiss()
                            popUpModal("question already being used!!")
                        }
                    }


            } else {
                if(progressDialog.isShowing) progressDialog.dismiss()
                popUpModal("all field must be filled!!")
            }
        }
    }

    fun popUpModal(message: String){
        val dialog = Dialog(this)
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

    fun popUpModalCreateCard(message: String){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_notification)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val modal_message = dialog.findViewById<TextView>(R.id.modal_message)
        modal_message.setText(message)
        val close_button = dialog.findViewById<ImageView>(R.id.close_button)
        close_button?.setOnClickListener{
            dialog.dismiss()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}