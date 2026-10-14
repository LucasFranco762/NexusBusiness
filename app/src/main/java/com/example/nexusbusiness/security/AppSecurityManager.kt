package com.example.nexusbusiness.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class SecurityStatus(
    val isEnabled: Boolean,
    val remainingAttempts: Int,
    val lockedUntilMillis: Long?
) {
    val isLocked: Boolean
        get() = lockedUntilMillis != null
}

sealed class AuthenticationResult {
    data object Success : AuthenticationResult()
    data class InvalidPassword(val remainingAttempts: Int) : AuthenticationResult()
    data class Locked(val lockedUntilMillis: Long) : AuthenticationResult()
}

class AppSecurityManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    @Synchronized
    fun getStatus(nowMillis: Long = System.currentTimeMillis()): SecurityStatus {
        if (!isPasswordEnabled()) {
            return SecurityStatus(
                isEnabled = false,
                remainingAttempts = MAX_ATTEMPTS,
                lockedUntilMillis = null
            )
        }

        val lockedUntil = preferences.getLong(KEY_LOCKED_UNTIL, 0L)
        if (lockedUntil > nowMillis) {
            return SecurityStatus(
                isEnabled = true,
                remainingAttempts = 0,
                lockedUntilMillis = lockedUntil
            )
        }

        if (lockedUntil > 0L) {
            resetFailedAttempts()
        }

        val failedAttempts = preferences.getInt(KEY_FAILED_ATTEMPTS, 0)
            .coerceIn(0, MAX_ATTEMPTS)
        return SecurityStatus(
            isEnabled = true,
            remainingAttempts = MAX_ATTEMPTS - failedAttempts,
            lockedUntilMillis = null
        )
    }

    fun isPasswordEnabled(): Boolean {
        return preferences.getBoolean(KEY_ENABLED, false) &&
            !preferences.getString(KEY_PASSWORD_HASH, null).isNullOrBlank() &&
            !preferences.getString(KEY_PASSWORD_SALT, null).isNullOrBlank()
    }

    @Synchronized
    fun savePassword(password: String) {
        require(password.length >= MIN_PASSWORD_LENGTH)

        val salt = ByteArray(SALT_LENGTH_BYTES).also(SecureRandom()::nextBytes)
        val passwordHash = derivePasswordHash(password, salt)
        preferences.edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_PASSWORD_SALT, salt.toBase64())
            .putString(KEY_PASSWORD_HASH, passwordHash.toBase64())
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCKED_UNTIL)
            .apply()
    }

    @Synchronized
    fun authenticate(
        password: String,
        nowMillis: Long = System.currentTimeMillis()
    ): AuthenticationResult {
        val status = getStatus(nowMillis)
        if (!status.isEnabled) return AuthenticationResult.Success
        status.lockedUntilMillis?.let { return AuthenticationResult.Locked(it) }

        if (matchesStoredPassword(password)) {
            resetFailedAttempts()
            return AuthenticationResult.Success
        }

        val failedAttempts = preferences.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        if (failedAttempts >= MAX_ATTEMPTS) {
            val lockedUntil = nowMillis + LOCK_DURATION_MILLIS
            preferences.edit()
                .putInt(KEY_FAILED_ATTEMPTS, MAX_ATTEMPTS)
                .putLong(KEY_LOCKED_UNTIL, lockedUntil)
                .apply()
            return AuthenticationResult.Locked(lockedUntil)
        }

        preferences.edit().putInt(KEY_FAILED_ATTEMPTS, failedAttempts).apply()
        return AuthenticationResult.InvalidPassword(MAX_ATTEMPTS - failedAttempts)
    }

    @Synchronized
    fun disablePassword(password: String): Boolean {
        if (!isPasswordEnabled()) return true
        if (!matchesStoredPassword(password)) return false

        preferences.edit().clear().apply()
        return true
    }

    private fun matchesStoredPassword(password: String): Boolean {
        val storedSalt = preferences.getString(KEY_PASSWORD_SALT, null)?.fromBase64()
            ?: return false
        val storedHash = preferences.getString(KEY_PASSWORD_HASH, null)?.fromBase64()
            ?: return false
        val candidateHash = runCatching {
            derivePasswordHash(password, storedSalt)
        }.getOrNull() ?: return false
        return MessageDigest.isEqual(storedHash, candidateHash)
    }

    private fun resetFailedAttempts() {
        preferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCKED_UNTIL)
            .apply()
    }

    private fun derivePasswordHash(password: String, salt: ByteArray): ByteArray {
        val specification = PBEKeySpec(
            password.toCharArray(),
            salt,
            HASH_ITERATIONS,
            HASH_LENGTH_BITS
        )
        return try {
            SecretKeyFactory.getInstance(HASH_ALGORITHM)
                .generateSecret(specification)
                .encoded
        } finally {
            specification.clearPassword()
        }
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

    private fun String.fromBase64(): ByteArray? = runCatching {
        Base64.decode(this, Base64.NO_WRAP)
    }.getOrNull()

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_ATTEMPTS = 5
        const val LOCK_DURATION_MILLIS = 30 * 60 * 1000L

        private const val PREFERENCES_NAME = "nexusbusiness_security"
        private const val KEY_ENABLED = "password_enabled"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PASSWORD_SALT = "password_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKED_UNTIL = "locked_until"
        private const val HASH_ALGORITHM = "PBKDF2WithHmacSHA1"
        private const val HASH_ITERATIONS = 120_000
        private const val HASH_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 16
    }
}
