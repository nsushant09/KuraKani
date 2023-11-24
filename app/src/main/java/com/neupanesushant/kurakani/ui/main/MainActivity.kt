package com.neupanesushant.kurakani.ui.main

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.neupanesushant.kurakani.R
import com.neupanesushant.kurakani.broadcast_recievers.WiFiBroadcastReceiver
import com.neupanesushant.kurakani.data.UserManager
import com.neupanesushant.kurakani.data.datasource.FirebaseInstance
import com.neupanesushant.kurakani.databinding.ActivityMainBinding
import com.neupanesushant.kurakani.domain.Utils
import com.neupanesushant.kurakani.domain.model.User
import com.neupanesushant.kurakani.domain.usecase.permission.PermissionManager
import com.neupanesushant.kurakani.ui.login.LoginActivity
import com.neupanesushant.kurakani.ui.main.fragments.chat.ChatFragment
import com.neupanesushant.kurakani.ui.main.fragments.chatmessaging.ChatMessagingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val chatFragment = ChatFragment()
    private val userManager = UserManager()
    private val wifiBroadcastReceiver = WiFiBroadcastReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()

        if (FirebaseInstance.firebaseAuth.currentUser == null) {
            navigateToLoginActivity()
            return
        }
        tryNavigateToChatMessagingFragment()
        navigateToChatFragment()
    }

    override fun onStart() {
        super.onStart()
        registerWifiReceiver()
        unregisterReceiver(wifiBroadcastReceiver)
    }

    override fun onStop() {
        super.onStop()
    }

    private fun navigateToChatFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view_tag, chatFragment)
            commit()
        }
    }

    private fun navigateToLoginActivity() {
        Intent(this, LoginActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun tryNavigateToChatMessagingFragment() {
        if (intent.extras == null) return
        val uid = intent.extras!!.getString("userID") ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot = userManager.getSelectedUser(uid)
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                chatMessagingFragment(user)
            }
        }
    }

    private fun chatMessagingFragment(user: User) {
        CoroutineScope(Dispatchers.Main).launch {
            val chatMessagingFragment = ChatMessagingFragment(user)
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view_tag, chatMessagingFragment)
                addToBackStack(null)
                commit()
            }

        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionManager.hasNotificationPermission(this)) {
                PermissionManager.requestNotificationPermission(this)
            }
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Utils.showToast(this, "You will not recieve notifications.")
        }
    }

    private fun registerWifiReceiver() {
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiBroadcastReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}