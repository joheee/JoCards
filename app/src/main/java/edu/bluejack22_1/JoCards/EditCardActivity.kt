package edu.bluejack22_1.JoCards

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityEditCardBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCardBinding
    private val db = Firebase.firestore
    private lateinit var progressDialog: ProgressDialog
    private lateinit var cardId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = getIntent().extras
        Log.v("jojojo", bundle?.getString("question").toString())

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        db.collection("Cards")
            .whereEqualTo("question", bundle?.getString("question").toString())
            .get().addOnSuccessListener { documents ->
                documents.forEach { document ->
                    binding.cardTopic.setText(document.data.get("topic").toString())
                    binding.editCardTopic.setText(document.data.get("topic").toString())
                    binding.editCardQuestion.setText(document.data.get("question").toString())
                    binding.editCardAnswer.setText(document.data.get("answer").toString())
                    cardId = document.id
            }
        }

        binding.updateCardButton.setOnClickListener {
            var topic = binding.editCardTopic.text.toString()
            var question = binding.editCardQuestion.text.toString()
            var answer = binding.editCardAnswer.text.toString()

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("update card data...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            db.collection("Cards")
                .whereEqualTo("question", question).get()
                .addOnSuccessListener {
                        documents ->
                if(documents.isEmpty()){
                    db.collection("Cards").document(cardId).update(
                        mapOf(
                            "topic" to topic,
                            "question" to question,
                            "answer" to answer
                        )
                    ).addOnCompleteListener {
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        if(it.isSuccessful) {
                            binding.cardTopic.setText(topic)
                            popUpModal("success update card!!")
                        } else {
                            Log.v("jojojo", "${cardId} error update ${it.exception}")
                        }
                    }
                } else {
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    popUpModal("question already being used!!")
                }
            }
        }

        binding.deleteCardButton.setOnClickListener {
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("delete card data...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            db.collection("Cards").document(cardId).delete().addOnCompleteListener {
                if(progressDialog.isShowing) progressDialog.dismiss()
                if(it.isSuccessful) {
                    popUpModalDeleteCard("success delete card!!")
                } else {
                    popUpModalDeleteCard("error when delete the card!!")
                }
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

    fun popUpModalDeleteCard(message: String){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_notification)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val modal_message = dialog.findViewById<TextView>(R.id.modal_message)
        modal_message.setText(message)
        val close_button = dialog.findViewById<ImageView>(R.id.close_button)
        close_button?.setOnClickListener{
            dialog.dismiss()
            finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

