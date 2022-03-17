package com.mingo.progresslayer

import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        anim.duration = 10_000
        anim.interpolator = LinearInterpolator()
        btn_start.setOnClickListener {
            animStart()
        }
        anim.addUpdateListener {
            val value = it.animatedValue as Float
            progressView.progress = (value / 1000f * progressView.max).toInt()
        }
    }

    private val anim = ValueAnimator.ofFloat(0f, 1000f)

    private fun animStart() {
        anim.cancel()
        anim.start()
    }

}