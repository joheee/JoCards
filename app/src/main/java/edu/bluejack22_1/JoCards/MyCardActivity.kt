package edu.bluejack22_1.JoCards

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityMyCardBinding
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.ArrayList
import kotlin.math.ceil

class MyCardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMyCardBinding
    private lateinit var userEmail : String
    private lateinit var dailyCardProgress : LinearProgressIndicator
    private lateinit var monthlyCardProgress : LinearProgressIndicator
    private var db = Firebase.firestore
    private lateinit var cardList : ArrayList<Card>
    private lateinit var cardAdapter: CardAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(this)
        val isLoginAuth = FirebaseAuth.getInstance().currentUser

        if(isLoginGoogle != null){
            userEmail = isLoginGoogle.email.toString()
        }
        if(isLoginAuth != null){
            userEmail = isLoginAuth.email.toString()
        }

        dailyCardProgress = binding.dailyCardProgressBar
        monthlyCardProgress = binding.monthlyCardProgressBar

        dailyCardProgress.setIndeterminate(false)
        monthlyCardProgress.setIndeterminate(false)

        var dailyCards = 0
        var monthlyCards = 0

        var targetDailyCards = 0
        var targetMonthlyCards = 0

        fun calculateCharts () {
            Log.v("jojojo", "recalculate $dailyCards $monthlyCards $targetDailyCards $targetMonthlyCards")
            var daily = ceil((dailyCards.toDouble() /targetDailyCards) * 100).toInt()
            var monthly = ceil((monthlyCards.toDouble() /targetMonthlyCards) * 100).toInt()

            dailyCardProgress.setProgressCompat(daily, true)
            monthlyCardProgress.setProgressCompat(monthly, true)

            binding.dailyCardInformation.setText("${dailyCards}/${targetDailyCards} ${getString(R.string.daily_card_information)}")
            binding.monthlyCardInformation.setText("${monthlyCards}/${targetMonthlyCards} ${getString(R.string.cards_this_month)}")
            binding.dailyCardPercentage.setText("${daily}%")
            binding.monthlyCardPercentage.setText("${monthly}%")
        }

        calculateCharts()

        if(userEmail != null) {
            db.collection("Cards").whereEqualTo("creator", userEmail).addSnapshotListener {
                    snap,e ->
                var totalCards = Integer.parseInt(snap?.size().toString())
                if(totalCards <= 1) {
                    binding.totalCardIndicator.setText("$totalCards card!!")
                } else {
                    binding.totalCardIndicator.setText("$totalCards cards!!")
                }
            }
            db.collection("DailyCardTarget").document(userEmail).addSnapshotListener {
                    snap,e -> targetDailyCards = Integer.parseInt(snap?.data?.get("quantity").toString())
            }
            db.collection("MonthlyCardTarget").document(userEmail).addSnapshotListener {
                    snap,e -> targetMonthlyCards = Integer.parseInt(snap?.data?.get("quantity").toString())
            }
            db.collection("UserCardLog").whereEqualTo("email",userEmail).addSnapshotListener {
                    snap,e ->
                dailyCards = Integer.parseInt(snap?.size().toString())
                monthlyCards = Integer.parseInt(snap?.size().toString())

                snap?.forEach {
                        doc ->
                    var currentTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    val fromDate = LocalDateTime.parse(doc.data.get("timestamp").toString(), formatter)
                    Log.v("jojojo", "days: ${ChronoUnit.DAYS.between(fromDate, currentTime)}")
                    Log.v("jojojo", "months: ${ChronoUnit.MONTHS.between(fromDate, currentTime)}")
                    if(ChronoUnit.DAYS.between(fromDate,currentTime) > 1) {
                        dailyCards -= 1
                    }
                    if(ChronoUnit.MONTHS.between(fromDate, currentTime) > 1) {
                        monthlyCards -= 1
                    }
                }
                calculateCharts()
            }


            val cardRecycleView = binding.homeCardRecycleview
            cardRecycleView.layoutManager = LinearLayoutManager(this)
            cardList = ArrayList()
            db.collection("Cards").whereEqualTo("creator", userEmail).addSnapshotListener { snap, e ->
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
                        cardList.add(Card("$answer","$created","$creator","$question","$topic"))
                    }
                }
                cardList.sortBy { it.created }
                cardList.reverse()
                if(cardList.size == 0) binding.latestCardText.setText("${getString(R.string.My_cards_is_empty)}...")
                else binding.latestCardText.setText("${getString(R.string.My_latest_cards)}")
                cardAdapter = CardAdapter(cardList)
                cardRecycleView.adapter = cardAdapter
                cardAdapter.onItemClick = {
                    val intent = Intent(this, CardDetailActivity::class.java)
                    intent.putExtra("question",it.question)
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.redirectToMyTarget1.setOnClickListener {
            val intent = Intent(this, TargetActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            finish()
        }

        binding.redirectToMyTarget2.setOnClickListener {
            val intent = Intent(this, TargetActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            finish()
        }
    }
}