package com.clairjour.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import android.util.Base64

/**
 * Provides a stable 32-byte passphrase for SQLCipher, backed by the Android Keystore.
 *
 * The secret is generated once, then persisted inside EncryptedSharedPreferences (whose
 * master key lives in the AndroidKeyStore). Callers get a fresh ByteArray each call —
 * they should zero it after use if possible.
 */
class DatabasePassphraseProvider(context: Context) {

    private val prefs = run {
        val masterKey = MasterKey.Builder(context.applicationContext, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Returns the raw 32-byte passphrase, generating and persisting it on first call. */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val encoded = prefs.getString(SECRET_KEY, null)
        if (encoded != null) {
            return Base64.decode(encoded, Base64.NO_WRAP)
        }
        val fresh = ByteArray(SECRET_LEN).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(SECRET_KEY, Base64.encodeToString(fresh, Base64.NO_WRAP))
            .apply()
        return fresh
    }

    companion object {
        private const val PREFS_FILE = "clairjour_secure_prefs"
        private const val MASTER_KEY_ALIAS = "clairjour_db_key"
        private const val SECRET_KEY = "db_passphrase_v1"
        private const val SECRET_LEN = 32
    }
}
