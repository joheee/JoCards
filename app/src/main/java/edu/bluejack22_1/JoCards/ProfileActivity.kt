package edu.bluejack22_1.JoCards

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.JoCards.databinding.ActivityProfileBinding
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userEmail : String
    private lateinit var dailyCardProgress : LinearProgressIndicator
    private lateinit var monthlyCardProgress : LinearProgressIndicator
    private lateinit var googleSignInClient : GoogleSignInClient
    private var db = Firebase.firestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var progressDialog : ProgressDialog
    private lateinit var userProfile : Bitmap
    private lateinit var imageUri : Uri


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(this)
        val isLoginAuth = firebaseAuth.currentUser

        binding.signOutButton.setOnClickListener{
            if(isLoginGoogle != null){
                googleSignInClient.signOut().addOnCompleteListener {
                    if(it.isSuccessful) {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Log.v("jojojo", it.exception.toString())
                    }
                }
            }

            if(isLoginAuth != null){
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        val bundle = getIntent().extras
        userEmail = bundle?.getString("email").toString()

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
            var daily = ceil((dailyCards.toDouble() /targetDailyCards) * 100).toInt()
            var monthly = ceil((monthlyCards.toDouble() /targetMonthlyCards) * 100).toInt()

            dailyCardProgress.setProgressCompat(daily, true)
            monthlyCardProgress.setProgressCompat(monthly, true)

            binding.dailyCardInformation.setText("${dailyCards}/${targetDailyCards} cards this day")
            binding.monthlyCardInformation.setText("${monthlyCards}/${targetMonthlyCards} cards this month")
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
            db.collection("UserDetail").whereEqualTo("email", userEmail).get()
                .addOnSuccessListener {
                    documents -> documents.forEach {
                    document ->
                    val storageRef = firebaseStorage.reference.child("default/${document.data.get("picture").toString()}")
                    val localFile = File.createTempFile("profileImage","jpg")
                    storageRef.getFile(localFile).addOnSuccessListener {
                        userProfile = BitmapFactory.decodeFile(localFile.absolutePath)
                        binding.imageProfile.setImageBitmap(userProfile)
                    }
                    binding.editFullName.setText(document.data.get("fullName").toString())
                    binding.editUserName.setText(document.data.get("username").toString())
                }
            }
        }

        binding.updateUserButton.setOnClickListener {
            var fullname = binding.editFullName.text.toString()
            var username = binding.editUserName.text.toString()

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("updating user info...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if(fullname.isNotEmpty() && username.isNotEmpty()) {
                db.collection("UserDetail").document(userEmail).update(
                    mapOf(
                        "fullName" to fullname,
                        "username" to username
                    )
                ).addOnCompleteListener {
                    if(it.isSuccessful) {
                        if(progressDialog.isShowing) progressDialog.dismiss()
                        popUpModal("success update user!!")
                    }
                }
            } else {
                if(progressDialog.isShowing) progressDialog.dismiss()
                popUpModal("all field must be filled!!")
            }
        }

        binding.editUserPicture.setOnClickListener{
            selectImage()
        }

        binding.redirectToMyTarget.setOnClickListener {
            val intent = Intent(this, TargetActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            finish()
        }

    }

    fun selectImage(){
        var intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        launcher.launch(intent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("updating your profile...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            imageUri = result.data?.getData()!!;

            firebaseStorage.getReference("default/$userEmail").putFile(imageUri).addOnCompleteListener {
                Log.v("jojojo", "in here")
                if(it.isSuccessful) {
                    db.collection("UserDetail").document(userEmail).update(
                        mapOf(
                            "picture" to userEmail
                        )
                    ).addOnCompleteListener {
                        if(it.isSuccessful) {
                            if(progressDialog.isShowing) progressDialog.dismiss()
                            popUpModal("success update profile picture!!")
                            binding.imageProfile.setImageURI(imageUri);
                        }
                    }
                }
            }

        } else {
            if(progressDialog.isShowing) progressDialog.dismiss()
            popUpModal("failed to upload!!")
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