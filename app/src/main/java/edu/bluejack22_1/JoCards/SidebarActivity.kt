package edu.bluejack22_1.JoCards

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.bluejack22_1.JoCards.databinding.ActivitySidebarBinding

class SidebarActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySidebarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySidebarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}