package edu.bluejack22_1.JoCards

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.bluejack22_1.JoCards.databinding.ActivityClosingBinding

class ClosingActivity : AppCompatActivity() {
    private lateinit var binding : ActivityClosingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClosingBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}