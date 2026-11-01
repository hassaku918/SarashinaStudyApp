package com.sarashina.study

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarashina.study.ui.chat.ChatScreen
import com.sarashina.study.ui.lock.LockScreen
import com.sarashina.study.ui.theme.SarashinaStudyTheme
import com.sarashina.study.util.AppLockManager
import com.sarashina.study.viewmodel.ChatViewModel

class MainActivity : FragmentActivity() {

    private lateinit var lockManager: AppLockManager
    private var lastBackgroundAt = 0L

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lockManager = AppLockManager(this)

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                StudyApplication.instance.isInForeground = false
                lastBackgroundAt = System.currentTimeMillis()
            }

            override fun onStart(owner: LifecycleOwner) {
                StudyApplication.instance.isInForeground = true
            }
        })

        setContent {
            val viewModel: ChatViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            var unlocked by remember {
                mutableStateOf(!lockManager.lockEnabled)
            }
            var lockError by remember { mutableStateOf<String?>(null) }

            DisposableEffect(Unit) {
                val obs = object : DefaultLifecycleObserver {
                    override fun onStart(owner: LifecycleOwner) {
                        if (!lockManager.lockEnabled) {
                            unlocked = true
                            return
                        }
                        val elapsed = (System.currentTimeMillis() - lastBackgroundAt) / 1000
                        if (lastBackgroundAt > 0 && elapsed >= AppLockManager.RELOCK_AFTER_SECONDS) {
                            unlocked = false
                            lockError = null
                        }
                    }
                }
                ProcessLifecycleOwner.get().lifecycle.addObserver(obs)
                onDispose {
                    ProcessLifecycleOwner.get().lifecycle.removeObserver(obs)
                }
            }

            SarashinaStudyTheme(themeMode = uiState.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!unlocked && lockManager.lockEnabled) {
                        LockScreen(
                            canBiometric = lockManager.canUseBiometric() && lockManager.useBiometric,
                            hasPin = lockManager.hasPin(),
                            error = lockError,
                            onBiometric = {
                                lockManager.authenticate(
                                    activity = this@MainActivity,
                                    onSuccess = {
                                        unlocked = true
                                        lockError = null
                                    },
                                    onError = { msg ->
                                        if (msg != "キャンセル") lockError = msg
                                    }
                                )
                            },
                            onPinSubmit = { pin ->
                                if (lockManager.verifyPin(pin)) {
                                    unlocked = true
                                    lockError = null
                                } else {
                                    lockError = "PINが違います"
                                }
                            }
                        )
                    } else {
                        ChatScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
