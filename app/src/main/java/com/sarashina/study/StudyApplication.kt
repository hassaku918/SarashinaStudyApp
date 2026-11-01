package com.sarashina.study

import android.app.Application
import com.sarashina.study.util.BackgroundNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class StudyApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isInForeground: Boolean = true

    override fun onCreate() {
        super.onCreate()
        instance = this
        BackgroundNotifier.ensureChannel(this)
    }

    companion object {
        lateinit var instance: StudyApplication
            private set
    }
}
