package com.vcc.widgetpro

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class DurationPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = arrayOf("15 minutes", "30 minutes", "Infinite")

        AlertDialog.Builder(this)
            .setTitle("Select Duration")
            .setItems(options) { _, which ->

                val duration = when (which) {
                    0 -> 15
                    1 -> 30
                    else -> -1 // infinite
                }

                val intent = Intent(this, CaffeinateActivity::class.java).apply {
                    putExtra("duration", duration)
                }

                startActivity(intent)
                finish()
            }
            .setOnCancelListener {
                finish()
            }
            .show()
    }
}