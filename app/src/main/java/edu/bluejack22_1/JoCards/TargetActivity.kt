package edu.bluejack22_1.JoCards

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.bluejack22_1.JoCards.databinding.ActivityTargetBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.ceil
import kotlin.system.exitProcess

class TargetActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTargetBinding
    private var db = Firebase.firestore
    private lateinit var userEmail : String
    private lateinit var dailyCardProgress : LinearProgressIndicator
    private lateinit var monthlyCardProgress : LinearProgressIndicator
    private lateinit var progressDialog : ProgressDialog


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTargetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = getIntent().extras
        userEmail = bundle?.getString("email").toString()

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        db.collection("Cards").whereEqualTo("creator", userEmail).addSnapshotListener {
            snap,e ->
            var totalCards = Integer.parseInt(snap?.size().toString())
            if(totalCards <= 1) {
                binding.totalCardIndicator.setText("$totalCards card!!")
            } else {
                binding.totalCardIndicator.setText("$totalCards cards!!")
            }
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
            db.collection("DailyCardTarget").document(userEmail).addSnapshotListener {
                    snap,e -> targetDailyCards = Integer.parseInt(snap?.data?.get("quantity").toString())
                    binding.editDailyTarget.setText(targetDailyCards.toString())
            }
            db.collection("MonthlyCardTarget").document(userEmail).addSnapshotListener {
                    snap,e -> targetMonthlyCards = Integer.parseInt(snap?.data?.get("quantity").toString())
                    binding.editMonthlyTarget.setText(targetMonthlyCards.toString())
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
        }

        binding.updateTargetButton.setOnClickListener {
            var dailyCard = binding.editDailyTarget.text.toString()
            var monthlyCard = binding.editMonthlyTarget.text.toString()

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("updating the target...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if(dailyCard.isNotEmpty() && monthlyCard.isNotEmpty()) {
                if(dailyCard.toInt() != 0 && monthlyCard.toInt() != 0) {
                    if(dailyCard.toInt() > monthlyCard.toInt()) {
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        popUpModal("${getString(R.string.daily_target_must_be_smaller_than_monthly_target)}!")
                    } else {
                        db.collection("DailyCardTarget").document(userEmail).update(
                            mapOf(
                                "quantity" to dailyCard
                            )
                        ).addOnCompleteListener {
                            if(it.isSuccessful) {
                                db.collection("MonthlyCardTarget").document(userEmail).update(
                                    mapOf(
                                        "quantity" to monthlyCard
                                    )
                                ).addOnCompleteListener {
                                    if(it.isSuccessful) {
                                        if(progressDialog.isShowing) progressDialog.dismiss()
                                        popUpModalUpdate("${getString(R.string.success_update_daily_and_monthly_target)}!!")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    popUpModal("the input must be greater than 0!")
                }
             } else {
                if(progressDialog.isShowing) progressDialog.dismiss()
                popUpModal("all field must be filled!")
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

    fun popUpModalUpdate(message: String){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.modal_notification)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val modal_message = dialog.findViewById<TextView>(R.id.modal_message)
        modal_message.setText(message)
        val close_button = dialog.findViewById<ImageView>(R.id.close_button)
        close_button?.setOnClickListener{
            dialog.dismiss()
            this.recreate()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}