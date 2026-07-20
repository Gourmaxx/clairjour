package com.clairjour.app.data.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encrypted backup blob format:
 *   magic (10 bytes ASCII "CLAIRJOUR1")
 *   version (1 byte)
 *   salt (16 bytes)
 *   iv (12 bytes)
 *   ciphertext + GCM tag (variable, tag = 16 bytes appended by JCE)
 *
 * Key derivation: PBKDF2WithHmacSHA256, 210_000 iterations, 32-byte key.
 * Cipher: AES-256/GCM/NoPadding.
 */
object BackupCrypto {

    // Header constants
    val MAGIC: ByteArray = "CLAIRJOUR1".toByteArray(Charsets.US_ASCII)
    const val CURRENT_VERSION: Byte = 1
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val GCM_TAG_BITS = 128
    private const val KEY_LEN_BITS = 256
    private const val PBKDF2_ITERATIONS = 210_000
    private const val PBKDF2_ALGO = "PBKDF2WithHmacSHA256"
    private const val CIPHER_TRANSFORM = "AES/GCM/NoPadding"

    private val secureRandom = SecureRandom()

    /**
     * Encrypt [plaintext] using [passphrase]. Returns a self-contained blob (see class doc).
     * The passphrase is not zeroed by this method; the caller is responsible.
     */
    fun encrypt(plaintext: ByteArray, passphrase: CharArray): ByteArray {
        val salt = ByteArray(SALT_LEN).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { secureRandom.nextBytes(it) }
        val key = deriveKey(passphrase, salt)
        try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORM)
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
            val ciphertext = cipher.doFinal(plaintext)
            // Assemble: magic + version + salt + iv + ciphertext (with tag).
            val out = ByteArray(MAGIC.size + 1 + salt.size + iv.size + ciphertext.size)
            var off = 0
            System.arraycopy(MAGIC, 0, out, off, MAGIC.size); off += MAGIC.size
            out[off] = CURRENT_VERSION; off += 1
            System.arraycopy(salt, 0, out, off, salt.size); off += salt.size
            System.arraycopy(iv, 0, out, off, iv.size); off += iv.size
            System.arraycopy(ciphertext, 0, out, off, ciphertext.size)
            return out
        } finally {
            // Best effort to erase the derived key from memory.
            key.fill(0)
        }
    }

    /**
     * Decrypt [blob]. Throws [BackupCorruptException], [BackupUnsupportedVersionException]
     * or [BackupBadPassphraseException] on failure.
     */
    fun decrypt(blob: ByteArray, passphrase: CharArray): ByteArray {
        if (!hasMagic(blob)) throw BackupCorruptException("Missing magic header")
        val headerLen = MAGIC.size + 1 + SALT_LEN + IV_LEN
        if (blob.size < headerLen + 16) throw BackupCorruptException("Blob too short")
        val version = blob[MAGIC.size]
        if (version != CURRENT_VERSION) {
            throw BackupUnsupportedVersionException("Unsupported backup version: $version")
        }
        val salt = blob.copyOfRange(MAGIC.size + 1, MAGIC.size + 1 + SALT_LEN)
        val iv = blob.copyOfRange(MAGIC.size + 1 + SALT_LEN, headerLen)
        val ciphertext = blob.copyOfRange(headerLen, blob.size)
        val key = deriveKey(passphrase, salt)
        try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORM)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_BITS, iv))
            return cipher.doFinal(ciphertext)
        } catch (e: javax.crypto.AEADBadTagException) {
            throw BackupBadPassphraseException("Bad passphrase or tampered blob", e)
        } catch (e: javax.crypto.BadPaddingException) {
            // GCM should surface AEADBadTagException; keep this as a defensive catch.
            throw BackupBadPassphraseException("Bad passphrase or tampered blob", e)
        } finally {
            key.fill(0)
        }
    }

    /** True if [blob] starts with the encrypted-backup magic. */
    fun hasMagic(blob: ByteArray): Boolean {
        if (blob.size < MAGIC.size) return false
        for (i in MAGIC.indices) if (blob[i] != MAGIC[i]) return false
        return true
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LEN_BITS)
        try {
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGO)
            return factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}

class BackupBadPassphraseException(message: String, cause: Throwable? = null) : Exception(message, cause)
class BackupCorruptException(message: String, cause: Throwable? = null) : Exception(message, cause)
class BackupUnsupportedVersionException(message: String, cause: Throwable? = null) : Exception(message, cause)
