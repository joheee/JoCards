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
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.bluejack22_1.JoCards.databinding.FragmentFirstProfileBinding
import java.io.File


class FirstProfileFragment : Fragment() {

    private var _binding : FragmentFirstProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressDialog : ProgressDialog
    private lateinit var imageUri : Uri
    private var db = Firebase.firestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userProfile : Bitmap
    private lateinit var userEmail : String

    private lateinit var dialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  FragmentFirstProfileBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        if (container != null) {
            dialog = Dialog(container.context)
        }

        val isLoginGoogle = container?.let { GoogleSignIn.getLastSignedInAccount(it.context) }
        if(isLoginGoogle != null){
            Log.v("jojojo","uda login google " + isLoginGoogle.email.toString())
            userEmail = isLoginGoogle.email.toString()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val isLoginAuth = firebaseAuth.currentUser
        if(isLoginAuth != null){
            Log.v("jojojo","uda login firebase auth " + isLoginAuth.email.toString())
            userEmail = isLoginAuth.email.toString()
        }

        if(userEmail != null) {
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

            progressDialog = ProgressDialog(activity)
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

        return binding.root
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

            progressDialog = ProgressDialog(activity)
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

