package com.neupanesushant.kurakani.ui.helper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.neupanesushant.kurakani.R

class ProgressDialog(private val context: Context) :
    Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_progress)
        window?.setBackgroundDrawable(ColorDrawable(context.resources.getColor(R.color.transparent_black)))
        setCancelable(false)
    }
}