package edu.bluejack22_1.JoCards

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityCardDetailBinding

class CardDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCardDetailBinding
    private lateinit var front_anim : AnimatorSet
    private lateinit var back_anim : AnimatorSet
    var isFront = true
    private lateinit var userEmail : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(this)
        val isLoginAuth = FirebaseAuth.getInstance().currentUser


        binding.backButton.setOnClickListener {
            finish()
        }

        val bundle = getIntent().extras
        binding.editCardButton.setOnClickListener {
            val intent = Intent(this, EditCardActivity::class.java)
            intent.putExtra("question", bundle?.getString("question").toString())
            startActivity(intent)
            finish()
        }

        Firebase.firestore.collection("Cards").whereEqualTo("question", bundle?.getString("question").toString()).addSnapshotListener {
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
                    val creator = dc.document.data.get("creator")
                    if(isLoginGoogle != null){
                        userEmail = isLoginGoogle.email.toString()
                    }
                    if(isLoginAuth != null){
                        userEmail = isLoginAuth.email.toString()
                    }
                    if(userEmail != creator) {
                        binding.editCardButton.visibility = View.GONE
                    }
                    binding.cardAnswer.setText("$answer")
                    binding.cardQuestion.setText("$question")
                    binding.cardTopic.setText("$topic")
                }
            }
        }

        val scale = applicationContext.resources.displayMetrics.density
        binding.frontCard.cameraDistance = 4000 * scale
        binding.backCard.cameraDistance = 4000 * scale

        front_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.front_animator) as AnimatorSet
        back_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.back_animator) as AnimatorSet

        binding.frontCard.setOnClickListener {
            if(isFront) {
                front_anim.setTarget(binding.frontCard)
                back_anim.setTarget(binding.backCard)
                front_anim.start()
                back_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            } else {
                front_anim.setTarget(binding.backCard)
                back_anim.setTarget(binding.frontCard)
                back_anim.start()
                front_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            }

            binding.frontCard.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.frontCard.isEnabled = true
            }, CardConfig().cardFlipTiming)
        }

        binding.flipCardButton.setOnClickListener {
            if(isFront) {
                front_anim.setTarget(binding.frontCard)
                back_anim.setTarget(binding.backCard)
                front_anim.start()
                back_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            } else {
                front_anim.setTarget(binding.backCard)
                back_anim.setTarget(binding.frontCard)
                back_anim.start()
                front_anim.start()
                isFront = !isFront

                binding.flipCardButton.isEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.flipCardButton.isEnabled = true
                }, CardConfig().cardFlipTiming)
            }
            binding.frontCard.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.frontCard.isEnabled = true
            }, CardConfig().cardFlipTiming)
        }
    }
}