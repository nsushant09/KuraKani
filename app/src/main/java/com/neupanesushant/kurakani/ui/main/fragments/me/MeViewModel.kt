package com.neupanesushant.kurakani.ui.main.fragments.me

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.domain.usecase.AuthenticatedUser
import com.neupanesushant.kurakani.domain.usecase.databasepersistence.DatabaseImagePersistence
import kotlinx.coroutines.coroutineScope

class MeViewModel(application: Application) : AndroidViewModel(application) {

    suspend fun updateUserProfileImage(profileImageURI: Uri) = coroutineScope {
        val user = AuthenticatedUser.getInstance().getUser()
        val image = DatabaseImagePersistence().save(profileImageURI)
        if (user != null) {
            user.profileImage = image
            FirebaseInstance.firebaseDatabase.getReference("/users/${FirebaseInstance.firebaseAuth.uid}")
                .setValue(user)
        }
    }
}