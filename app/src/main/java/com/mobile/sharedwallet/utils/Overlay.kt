package com.mobile.sharedwallet.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.view.View
import com.mobile.sharedwallet.R

class Overlay(private val act : Activity) {

    private var visibility : Int = View.GONE

    private fun  switchVisibility() {

        val overlay : View? = act.findViewById(R.id.overlay)

        overlay?.let { ov : View ->
            if (visibility == View.VISIBLE) {
                ov.alpha = 0f
            }
            ov.visibility = View.VISIBLE
            ov.animate()
                .setDuration(50.toLong())
                .alpha(if (visibility == View.VISIBLE) 0.5f else 0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        ov.visibility = visibility
                    }
                })
        }
    }

    fun show() {
        if (visibility == View.GONE) {
            visibility = View.VISIBLE
            switchVisibility()
        }
    }

    fun hide() {
        if (visibility == View.VISIBLE) {
            visibility = View.GONE
            switchVisibility()
        }
    }
}
