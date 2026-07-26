package com.clairjour.app.data.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BackupCryptoTest {

    private val plaintext = """{"hello":"world"}""".toByteArray(Charsets.UTF_8)
    private val pass = "correct horse battery staple".toCharArray()

    @Test
    fun `encrypt then decrypt returns original plaintext`() {
        val blob = BackupCrypto.encrypt(plaintext, pass.copyOf())
        val decoded = BackupCrypto.decrypt(blob, pass.copyOf())
        assertArrayEquals(plaintext, decoded)
    }

    @Test(expected = BackupBadPassphraseException::class)
    fun `decrypt with wrong passphrase throws BadPassphrase`() {
        val blob = BackupCrypto.encrypt(plaintext, pass.copyOf())
        BackupCrypto.decrypt(blob, "wrong".toCharArray())
    }

    @Test(expected = BackupBadPassphraseException::class)
    fun `decrypt with tampered ciphertext throws BadPassphrase`() {
        val blob = BackupCrypto.encrypt(plaintext, pass.copyOf())
        blob[blob.size - 1] = (blob[blob.size - 1].toInt() xor 0x01).toByte()
        BackupCrypto.decrypt(blob, pass.copyOf())
    }

    @Test(expected = BackupCorruptException::class)
    fun `decrypt with missing magic throws Corrupt`() {
        BackupCrypto.decrypt(ByteArray(64), pass.copyOf())
    }

    @Test(expected = BackupUnsupportedVersionException::class)
    fun `decrypt with unknown version throws UnsupportedVersion`() {
        val blob = BackupCrypto.encrypt(plaintext, pass.copyOf())
        blob[BackupCrypto.MAGIC.size] = 99
        BackupCrypto.decrypt(blob, pass.copyOf())
    }

    @Test
    fun `hasMagic returns false for legacy JSON payload`() {
        assertFalse(BackupCrypto.hasMagic("""{"json":true}""".toByteArray()))
    }

    @Test
    fun `encrypt produces distinct blobs due to random salt and iv`() {
        val a = BackupCrypto.encrypt(plaintext, pass.copyOf())
        val b = BackupCrypto.encrypt(plaintext, pass.copyOf())
        assertFalse(a.contentEquals(b))
    }
}
