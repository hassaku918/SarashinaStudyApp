package com.sarashina.study.util

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class AppLockManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "app_lock_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var lockEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(v) = prefs.edit().putBoolean(KEY_ENABLED, v).apply()

    var useBiometric: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, true)
        set(v) = prefs.edit().putBoolean(KEY_BIOMETRIC, v).apply()

    fun hasPin(): Boolean = !prefs.getString(KEY_PIN_HASH, null).isNullOrBlank()

    fun setPin(pin: String) {
        require(pin.length in 4..8 && pin.all { it.isDigit() }) { "PINは4〜8桁の数字" }
        prefs.edit().putString(KEY_PIN_HASH, sha256(pin)).apply()
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN_HASH).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return stored == sha256(pin)
    }

    fun canUseBiometric(): Boolean {
        val m = BiometricManager.from(context)
        return m.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        onError("キャンセル")
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {}
            }
        )
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sarashina Study のロック解除")
            .setSubtitle("生体認証または端末のPIN・パターン")
            .setAllowedAuthenticators(authenticators)
            .build()
        try {
            prompt.authenticate(info)
        } catch (e: Exception) {
            onError(e.message ?: "認証を開始できません")
        }
    }

    private fun sha256(s: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))
        return d.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_ENABLED = "lock_enabled"
        private const val KEY_BIOMETRIC = "use_biometric"
        private const val KEY_PIN_HASH = "pin_hash"
        const val RELOCK_AFTER_SECONDS = 30L
    }
}
