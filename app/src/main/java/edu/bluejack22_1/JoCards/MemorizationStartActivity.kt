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
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.JoCards.databinding.ActivityMemorizationStartBinding
import java.io.File
import java.util.ArrayList

class MemorizationStartActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMemorizationStartBinding
    private lateinit var cardList : ArrayList<CardMemorization>
    private lateinit var cardMemorizationAdapter: CardMemorizationAdapter
    private var db = Firebase.firestore
    private lateinit var userEmail : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemorizationStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButtonMemorization.setOnClickListener {
            finish()
        }

        Memorization.curr = 0
        Memorization.idx = 1

        val cardRecycleView = binding.memorizationCardRecycleview
        cardRecycleView.layoutManager = LinearLayoutManager(this)

        cardList = ArrayList()
        db.collection("Cards").addSnapshotListener { snap, e ->
            if( e != null) {
                Log.v("jojojo", "errorr $e")
                return@addSnapshotListener
            }
            for (dc in snap!!.documentChanges) {
                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    val answer = dc.document.data.get("answer")
                    val created = dc.document.data.get("created")
                    val creator = dc.document.data.get("creator")
                    val question = dc.document.data.get("question")
                    val topic = dc.document.data.get("topic")
                    Log.v("jojojo", "cards: $answer $created $creator $question $topic")
                    cardList.add(CardMemorization("$answer","$created","$creator","$question","$topic"))
                }
            }
            cardList.sortBy { it.created }
            cardList.reverse()
            if(cardList.size == 0) binding.latestCardText.setText("Cards is empty...")
            else binding.latestCardText.setText("${getString(R.string.latest_cards)}")
            cardMemorizationAdapter = CardMemorizationAdapter(cardList)
            cardRecycleView.adapter = cardMemorizationAdapter

            Memorization.memorizationArr.clear()
            binding.cardSelectedIndicator.setText("${Memorization.memorizationArr.size}/5 " +
                    "${if(Memorization.memorizationArr.size > 1) "cards"  else "card"} selected")

            cardMemorizationAdapter.onItemClick = {
                if(Memorization.memorizationArr.filter { s -> s == it.question }.isEmpty()) {
                    if(Memorization.memorizationArr.size < 5) {
                        Memorization.memorizationArr.add(it.question)
                        binding.cardSelectedIndicator.setText("${Memorization.memorizationArr.size}/5 " +
                                "${if(Memorization.memorizationArr.size > 1) "cards"  else "card"} selected")
                    } else {
                        popUpModal("already pick 5 cards")
                    }
                } else {
                    Memorization.memorizationArr.remove(it.question)
                    binding.cardSelectedIndicator.setText("${Memorization.memorizationArr.size}/5 " +
                            "${if(Memorization.memorizationArr.size > 1) "cards"  else "card"} selected")
                }
                Log.v("jojojo", "list ${Memorization.memorizationArr}")
            }
        }

        var searchInput : SearchView = binding.searchCardInput
        searchInput.clearFocus()
        searchInput.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

        binding.memorizeButton.setOnClickListener {
            if(Memorization.memorizationArr.size < 5) {
                popUpModal("${getString(R.string.must_picked_5_cards)}!")
            } else {

                var progressDialog = ProgressDialog(this)
                progressDialog.setMessage("update card data...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(this)
                val isLoginAuth = FirebaseAuth.getInstance().currentUser

                if(isLoginGoogle != null){
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    userEmail = isLoginGoogle.email.toString()
                    val intent = Intent(this, MemorizationDetailActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                }
                if(isLoginAuth != null){
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    userEmail = isLoginAuth.email.toString()
                    val intent = Intent(this, MemorizationDetailActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun filterList(newText: String?) {
        var filteredList : ArrayList<CardMemorization> = ArrayList()
        for(card in cardList) {
            if (newText != null) {
                if(card.question.toLowerCase().contains(newText.toLowerCase())) {
                    filteredList.add(card)
                }
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(this, "no data found", Toast.LENGTH_SHORT).show()
        } else {
            cardMemorizationAdapter.setCardList(filteredList)
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
}