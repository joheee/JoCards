package edu.bluejack22_1.JoCards

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityMemorizationDetailBinding
import java.time.LocalDateTime

class MemorizationDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMemorizationDetailBinding
    private lateinit var userEmail : String
    private lateinit var front_anim : AnimatorSet
    private lateinit var back_anim : AnimatorSet
    private var isFront = true
    private var isFlip = false
    private var db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemorizationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = getIntent().extras
        userEmail = bundle?.getString("email").toString()

        var currentQuestion = Memorization.memorizationArr.get(0)
        Memorization.memorizationArr.remove(currentQuestion)

        Firebase.firestore.collection("Cards").whereEqualTo("question", currentQuestion).addSnapshotListener {
                snap, e ->
            if( e != null) {
                Log.v("jojojo", "errorr $e")
                return@addSnapshotListener
            }
            for (dc in snap!!.documentChanges) {
                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    val answer = dc.document.data.get("answer")
                    val question = dc.document.data.get("question")
                    val topic = dc.document.data.get("topic")
                    binding.cardAnswer.setText("$answer")
                    binding.cardQuestion.setText("$question")
                    binding.cardTopic.setText("$topic")
                }
            }
        }

        binding.indexIndicator.setText(Memorization.idx.toString())
        binding.nextCardButton.setOnClickListener {
            Memorization.idx += 1
            if(!isFlip) {
                var userCardLog = hashMapOf(
                    "email" to userEmail,
                    "question" to currentQuestion,
                    "timestamp" to LocalDateTime.now().toString()
                )
                db.collection("UserCardLog").add(userCardLog).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Memorization.curr += 1
                        recreate()
                    }
                }
            } else {
                recreate()
            }
        }

        binding.finishCardButton.setOnClickListener {
            if(!isFlip) {
                var userCardLog = hashMapOf(
                    "email" to userEmail,
                    "question" to currentQuestion,
                    "timestamp" to LocalDateTime.now().toString()
                )
                db.collection("UserCardLog").add(userCardLog).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Memorization.curr += 1
                        popUpModal("success finish ${Memorization.curr} ${if(Memorization.curr > 1) "cards" else "card"} today!!")

                    }
                }
            } else {
                popUpModal("success finish ${Memorization.curr} ${if(Memorization.curr > 1) "cards" else "card"} today!!")
            }
        }

        if(Memorization.memorizationArr.size > 0) {
            binding.nextCardButton.visibility = View.VISIBLE
            binding.finishCardButton.visibility = View.GONE
        } else {
            binding.nextCardButton.visibility = View.GONE
            binding.finishCardButton.visibility = View.VISIBLE
        }

        val scale = applicationContext.resources.displayMetrics.density
        binding.frontCard.cameraDistance = 4000 * scale
        binding.backCard.cameraDistance = 4000 * scale

        front_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.front_animator) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.back_animator) as AnimatorSet

        binding.frontCard.setOnClickListener {
            isFlip = true
            if(isFront) {
                front_anim.setTarget(binding.frontCard)
                back_anim.setTarget(binding.backCard)
                front_anim.start()
                back_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                binding.nextCardButton.isEnabled = false
                binding.finishCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                    binding.nextCardButton.isEnabled = true
                    binding.finishCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            } else {
                front_anim.setTarget(binding.backCard)
                back_anim.setTarget(binding.frontCard)
                back_anim.start()
                front_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                binding.nextCardButton.isEnabled = false
                binding.finishCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                    binding.nextCardButton.isEnabled = true
                    binding.finishCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            }

            binding.frontCard.isEnabled = false
            binding.nextCardButton.isEnabled = false
            binding.finishCardButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.frontCard.isEnabled = true
                binding.nextCardButton.isEnabled = true
                binding.finishCardButton.isEnabled = true
            }, CardConfig().cardFlipTiming)
        }

        binding.flipCardButton.setOnClickListener {
            isFlip = true
            if(isFront) {
                front_anim.setTarget(binding.frontCard)
                back_anim.setTarget(binding.backCard)
                front_anim.start()
                back_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                binding.nextCardButton.isEnabled = false
                binding.finishCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                    binding.nextCardButton.isEnabled = true
                    binding.finishCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            } else {
                front_anim.setTarget(binding.backCard)
                back_anim.setTarget(binding.frontCard)
                back_anim.start()
                front_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                binding.nextCardButton.isEnabled = false
                binding.finishCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                    binding.nextCardButton.isEnabled = true
                    binding.finishCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            }
            binding.frontCard.isEnabled = false
            binding.nextCardButton.isEnabled = false
            binding.finishCardButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.frontCard.isEnabled = true
                binding.nextCardButton.isEnabled = true
                binding.finishCardButton.isEnabled = true
            }, CardConfig().cardFlipTiming)
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
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "can't exit the app!", Toast.LENGTH_SHORT).show()
    }
}