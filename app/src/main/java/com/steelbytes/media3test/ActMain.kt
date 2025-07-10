package com.steelbytes.media3test

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.steelbytes.media3test.databinding.ActmainBinding

class ActMain : ActBase(), View.OnClickListener {

    private lateinit var binding: ActmainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActmainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bug1.setOnClickListener(this)
        binding.bug2.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == null)
            return
        when (v.id) {
            R.id.bug1 -> startActivity(Intent(this, ActBug1::class.java))
            R.id.bug2 -> startActivity(Intent(this, ActBug2::class.java))
        }
    }
}
