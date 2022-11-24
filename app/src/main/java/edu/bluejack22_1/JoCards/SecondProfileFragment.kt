package edu.bluejack22_1.JoCards

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.JoCards.databinding.FragmentFirstProfileBinding
import edu.bluejack22_1.JoCards.databinding.FragmentSecondProfileBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SecondProfileFragment : Fragment() {

    private var _binding : FragmentSecondProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressDialog : ProgressDialog
    private lateinit var imageUri : Uri
    private var db = Firebase.firestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userProfile : Bitmap
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var dailyCardProgress : LinearProgressIndicator
    private lateinit var monthlyCardProgress : LinearProgressIndicator
    private lateinit var userEmail : String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentSecondProfileBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        val isLoginGoogle = container?.let { GoogleSignIn.getLastSignedInAccount(it.context) }
        if(isLoginGoogle != null){
            userEmail = isLoginGoogle.email.toString()
        }

        val isLoginAuth = firebaseAuth.currentUser
        if(isLoginAuth != null){
            userEmail = isLoginAuth.email.toString()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        if (container != null) {
            googleSignInClient = GoogleSignIn.getClient(container.context, gso)
        }

        binding.signOutButton.setOnClickListener{
            if(isLoginGoogle != null){
                googleSignInClient.signOut().addOnCompleteListener {
                    if(it.isSuccessful) {
                        startActivity(Intent(activity, LoginActivity::class.java))
                        activity?.finish()
                    } else {
                        Log.v("jojojo", it.exception.toString())
                    }
                }
            }

            if(isLoginAuth != null){
                firebaseAuth.signOut()
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
            }
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
            Log.v("jojojo", "recalculate $dailyCards $monthlyCards $targetDailyCards $targetMonthlyCards")
            val daily = ceil((dailyCards.toDouble() /targetDailyCards) * 100).toInt()
            val monthly = ceil((monthlyCards.toDouble() /targetMonthlyCards) * 100).toInt()

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
        }

        binding.redirectToMyTarget.setOnClickListener {
            val intent = Intent(activity, TargetActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            activity?.finish()
        }

        return binding.root
    }
}