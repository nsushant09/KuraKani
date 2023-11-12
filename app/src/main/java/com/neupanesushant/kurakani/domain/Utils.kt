package com.neupanesushant.kurakani.domain

import android.content.Context
import android.text.TextUtils
import android.widget.Toast

object Utils {
    fun showToast(context : Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun String.isEmptyAfterTrim(): Boolean {
        return TextUtils.isEmpty(this.trim { it <= ' ' })
    }
}