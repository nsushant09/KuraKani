package com.neupanesushant.kurakani.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val uid : String? = null,
    val firstName : String? = null,
    val lastName : String? = null,
    val fullName : String? = null,
    var profileImage : String? = null,
    var fcmToken : String? = null
        ) : Parcelable