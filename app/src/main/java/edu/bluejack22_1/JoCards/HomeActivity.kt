package edu.bluejack22_1.JoCards

import android.app.ActionBar
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.JoCards.databinding.ActivityHomeBinding
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var dailyCardProgress : LinearProgressIndicator
    private lateinit var monthlyCardProgress : LinearProgressIndicator
    private lateinit var cardList : ArrayList<Card>
    private lateinit var cardAdapter: CardAdapter
    private lateinit var userEmail : String
    private lateinit var userName : String
    private lateinit var userProfile : Bitmap
    private var cardTotal by Delegates.notNull<Int>()
    val db = Firebase.firestore

    private lateinit var gestureDetector : GestureDetector
    var x2:Float = 0.0f
    var x1:Float = 0.0f
    var y2:Float = 0.0f
    var y1:Float = 0.0f

    companion object {
        const val MIN_DISTANCE = 150
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val isLoginGoogle = GoogleSignIn.getLastSignedInAccount(this)
        val isLoginAuth = firebaseAuth.currentUser

        firebaseStorage = FirebaseStorage.getInstance()
        if(isLoginGoogle != null){
            userEmail = isLoginGoogle.email.toString()
            db.collection("UserDetail").whereEqualTo("email", isLoginGoogle.email.toString()).get()
                .addOnSuccessListener {
                    documents -> documents.forEach {
                        document ->
                        userName = document.data.get("username").toString()
                        binding.displayUsername.setText("Hi, " + userName)
                        val storageRef = firebaseStorage.reference.child("default/${document.data.get("picture").toString()}")
                        val localFile = File.createTempFile("profileImage","jpg")
                        storageRef.getFile(localFile).addOnSuccessListener {
                            userProfile = BitmapFactory.decodeFile(localFile.absolutePath)
                            binding.imageProfile.setImageBitmap(userProfile)
                            binding.imageProfileBottom.setImageBitmap(userProfile)
                    }
                }
            }
        }
        if(isLoginAuth != null){
            userEmail = isLoginAuth.email.toString()
            db.collection("UserDetail").whereEqualTo("email", isLoginAuth.email.toString()).get()
                .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    userName = document.data.get("username").toString()
                    binding.displayUsername.setText("Hi, " + document.data.get("username").toString())
                    val storageRef = firebaseStorage.reference.child("default/${document.data.get("picture").toString()}")
                    val localFile = File.createTempFile("profileImage","jpg")
                    storageRef.getFile(localFile).addOnSuccessListener {
                        userProfile = BitmapFactory.decodeFile(localFile.absolutePath)
                        binding.imageProfile.setImageBitmap(userProfile)
                        binding.imageProfileBottom.setImageBitmap(userProfile)
                    }
                }
            }
        }

        gestureDetector = GestureDetector(this, this)

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
                    if(ChronoUnit.DAYS.between(fromDate,currentTime) > 1) {
                        dailyCards -= 1
                    }
                    if(ChronoUnit.MONTHS.between(fromDate, currentTime) > 1) {
                        monthlyCards -= 1
                    }
                }

                calculateCharts()
                var dailyNotificitaion = arrayOf<String>(
                    "new day new cards to learn!",
                    "daily grind for lifetime knowledge!"
                )
                var monthNotification = arrayOf<String>(
                    "fresh month to grind some cards!",
                    "new month new cards to conquer!"
                )
                var nahNotification = arrayOf<String>(
                    "learning start today!",
                    "cards can't read itself!"
                )

                var i = Random().nextInt(24) % 2
                if(dailyCards == 0 && monthlyCards != 0) {
                    var toast = Toast.makeText(this, dailyNotificitaion[i], Toast.LENGTH_LONG)
                    toast.show()
                } else if(dailyCards != 0 && monthlyCards == 0) {
                    var toast = Toast.makeText(this, monthNotification[i], Toast.LENGTH_LONG)
                    toast.show()
                } else if(dailyCards == 0 && monthlyCards == 0) {
                    var toast = Toast.makeText(this, nahNotification[i], Toast.LENGTH_LONG)
                    toast.show()
                }
            }
        }

        binding.redirectToCreateCard.setOnClickListener{
            startActivity(Intent(this, CreateCardActivity::class.java))
        }

        val cardRecycleView = binding.homeCardRecycleview
        cardRecycleView.layoutManager = LinearLayoutManager(this)

        cardList = ArrayList()
        db.collection("Cards").addSnapshotListener { snap, e ->
            if( e != null) {
                return@addSnapshotListener
            }
            cardTotal = Integer.parseInt(snap?.size().toString())
            for (dc in snap!!.documentChanges) {
                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    val answer = dc.document.data.get("answer")
                    val created = dc.document.data.get("created")
                    val creator = dc.document.data.get("creator")
                    val question = dc.document.data.get("question")
                    val topic = dc.document.data.get("topic")
                    cardList.add(Card("$answer","$created","$creator","$question","$topic"))
                }
            }
            cardList.sortBy { it.created }
            cardList.reverse()
            if(cardList.size == 0) binding.latestCardText.setText("${getString(R.string.cards_is_empty)}...")
            else binding.latestCardText.setText("${getString(R.string.latest_cards)}")
            cardAdapter = CardAdapter(cardList)
            cardRecycleView.adapter = cardAdapter
            cardAdapter.onItemClick = {
                val intent = Intent(this, CardDetailActivity::class.java)
                intent.putExtra("question",it.question)
                startActivity(intent)
            }
        }

        binding.redirectToSidebar.setOnClickListener {
            popUpModalSidebar()
        }

        binding.monthlyTargetButton.setOnClickListener {
            var intent = Intent(this, TargetActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            finish()
        }

        binding.redirectToUserProfileInTarget.setOnClickListener {
            var intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            finish()
        }

        binding.redirectToUserProfileInBottomBar.setOnClickListener {
            var intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
        }

        binding.redirectToMemorization.setOnClickListener {
            if(cardTotal < 5) {
                popUpModal("create ${5 - cardTotal} ${if(5 - cardTotal > 1) "cards" else "card"} to start memorize!!")
            } else {
                startActivity(Intent(this, MemorizationStartActivity::class.java))
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        Log.v("jojojo", "on touch")

        when(event?.action) {
            0-> {
                x1 = event.x
                y1 = event.y
            }
            1-> {
                x2 = event.x
                y2 = event.y

                val valueX:Float = x2-x1
                val valueY:Float = y2-y1

                if(abs(valueX) > MIN_DISTANCE) {
                    if(x2 > x1) {
                        Log.v("jojojo", "right swipe")
                    } else {
                        Log.v("jojojo", "left swipe")
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun onBackPressed() {
        this.finish()
        finishAffinity()
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

    fun popUpModalSidebar(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_sidebar)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        Log.v("jojojo", dialog.window?.statusBarColor.toString())
        dialog.window?.setGravity(Gravity.TOP)
        dialog.window?.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT)

        db.collection("UserDetail").whereEqualTo("email", userEmail).get()
            .addOnSuccessListener {
                documents -> documents.forEach {
                    document ->
                    val storageRef = firebaseStorage.reference.child("default/${document.data.get("picture").toString()}")
                    val localFile = File.createTempFile("profileImage","jpg")
                    storageRef.getFile(localFile).addOnSuccessListener {
                        dialog.findViewById<ImageView>(R.id.user_image_profile).setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                    }
                }
            }

        dialog.findViewById<TextView>(R.id.user_username).setText(userName)
        Log.v("jojojo", userName)

        dialog.show()

        val close_button = dialog.findViewById<ImageView>(R.id.back_button)
        close_button?.setOnClickListener{
            getWindow().setStatusBarColor(ContextCompat.getColor(this,  R.color.secondaryColor))
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.redirect_to_my_target).setOnClickListener{
            var intent = Intent(this, TargetActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.redirect_to_user_profile_sidebar).setOnClickListener {
            var intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.redirect_to_my_card).setOnClickListener {
            startActivity(Intent(this, MyCardActivity::class.java))
            dialog.dismiss()
        }
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }
}