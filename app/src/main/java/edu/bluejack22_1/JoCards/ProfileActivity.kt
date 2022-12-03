package edu.bluejack22_1.JoCards

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import edu.bluejack22_1.JoCards.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Profile.profile = getString(R.string.profile)
        Profile.setting = getString(R.string.setting)

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        var viewPager = binding.profileViewPager
        viewPager.adapter = ProfilePageAdapter(supportFragmentManager)
        binding.profileTabLayout.setupWithViewPager(viewPager)

    }

    override fun onBackPressed() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}