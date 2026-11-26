package com.irminsul.codex.integration.security

import android.content.Context
import android.util.Base64
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the HoYoLAB session cookie as authenticated ciphertext.
 *
 * The AES key is generated inside Android Keystore and is never persisted
 * in application preferences or exported by this class.
 */
class HoYoLabCredentialStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Returns the saved cookie. A legacy plaintext value is encrypted and
     * removed only after encryption succeeds.
     *
     * Decryption errors are deliberately propagated so callers can ask the
     * user to clear the unusable credential rather than silently treating it
     * as an absent session.
     */
    fun getCookie(): String? {
        val encryptedCookie = preferences.getString(ENCRYPTED_COOKIE_KEY, null)
        if (encryptedCookie != null) {
            return decrypt(encryptedCookie)
        }

        val legacyCookie = preferences.getString(LEGACY_COOKIE_KEY, null)
            ?: return null

        if (legacyCookie.isBlank()) {
            preferences.edit().remove(LEGACY_COOKIE_KEY).apply()
            return null
        }

        saveCookie(legacyCookie)
        return legacyCookie
    }

    fun saveCookie(cookie: String) {
        if (cookie.isBlank()) {
            clearCookie()
            return
        }

        val encryptedCookie = encrypt(cookie)
        preferences.edit()
            .putString(ENCRYPTED_COOKIE_KEY, encryptedCookie)
            .remove(LEGACY_COOKIE_KEY)
            .apply()
    }

    /**
     * Removes both current and legacy cookie values and retires the key.
     * Removing the preference first ensures the credential is no longer
     * available even if Android Keystore cleanup itself fails.
     */
    fun clearCookie() {
        preferences.edit()
            .remove(ENCRYPTED_COOKIE_KEY)
            .remove(LEGACY_COOKIE_KEY)
            .apply()

        runCatching {
            val keyStore = loadKeyStore()
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
        }
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        cipher.updateAAD(appContext.packageName.toByteArray(StandardCharsets.UTF_8))

        val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val payload = cipher.iv + encrypted
        return FORMAT_PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(storedValue: String): String {
        if (!storedValue.startsWith(FORMAT_PREFIX)) {
            throw GeneralSecurityException("Unsupported encrypted credential format")
        }

        val payload = Base64.decode(
            storedValue.removePrefix(FORMAT_PREFIX),
            Base64.NO_WRAP
        )
        if (payload.size <= IV_LENGTH_BYTES + GCM_TAG_LENGTH_BITS / 8) {
            throw GeneralSecurityException("Encrypted credential payload is incomplete")
        }

        val iv = payload.copyOfRange(0, IV_LENGTH_BYTES)
        val encrypted = payload.copyOfRange(IV_LENGTH_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        )
        cipher.updateAAD(appContext.packageName.toByteArray(StandardCharsets.UTF_8))

        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = loadKeyStore()
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        )
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun loadKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    private companion object {
        const val PREFERENCES_NAME = "irminsul_prefs"
        const val LEGACY_COOKIE_KEY = "cookie"
        const val ENCRYPTED_COOKIE_KEY = "cookie_encrypted"
        const val KEY_ALIAS = "irminsul_hoyolab_cookie_aes_gcm_v1"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val FORMAT_PREFIX = "v1:"
        const val IV_LENGTH_BYTES = 12
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
