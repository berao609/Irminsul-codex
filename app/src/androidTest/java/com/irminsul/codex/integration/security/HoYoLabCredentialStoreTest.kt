package com.irminsul.codex.integration.security

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HoYoLabCredentialStoreTest {
    private lateinit var context: Context
    private lateinit var preferences: android.content.SharedPreferences
    private lateinit var store: HoYoLabCredentialStore

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences("irminsul_prefs", Context.MODE_PRIVATE)
        store = HoYoLabCredentialStore(context)
        preferences.edit().clear().commit()
        store.clearCookie()
    }

    @After
    fun tearDown() {
        store.clearCookie()
        preferences.edit().clear().commit()
    }

    @Test
    fun encryptsCookieAndRestoresItFromANewStoreInstance() {
        val cookie = "ltoken_v2=sample-secret-token; ltuid_v2=123456789"

        store.saveCookie(cookie)

        assertFalse(preferences.all.values.contains(cookie))
        assertNull(preferences.getString("cookie", null))
        assertTrue(preferences.getString("cookie_encrypted", null)!!.startsWith("v1:"))
        assertEquals(cookie, HoYoLabCredentialStore(context).getCookie())
    }

    @Test
    fun migratesLegacyPlaintextCookieAndRemovesLegacyValue() {
        val cookie = "ltoken_v2=legacy-sample; ltuid_v2=123456789"
        preferences.edit().putString("cookie", cookie).commit()

        assertEquals(cookie, store.getCookie())
        assertNull(preferences.getString("cookie", null))
        assertFalse(preferences.all.values.contains(cookie))
        assertTrue(preferences.getString("cookie_encrypted", null)!!.startsWith("v1:"))
    }

    @Test
    fun replacingCookieStoresOnlyTheLatestValue() {
        val first = "ltoken_v2=first; ltuid_v2=123456789"
        val second = "ltoken_v2=second; ltuid_v2=123456789"

        store.saveCookie(first)
        store.saveCookie(second)

        assertEquals(second, store.getCookie())
        assertFalse(preferences.all.values.contains(first))
        assertFalse(preferences.all.values.contains(second))
    }

    @Test
    fun clearingCookieRemovesEncryptedAndLegacyValues() {
        store.saveCookie("ltoken_v2=sample; ltuid_v2=123456789")
        store.clearCookie()

        assertNull(store.getCookie())
        assertNull(preferences.getString("cookie_encrypted", null))
        assertNull(preferences.getString("cookie", null))
    }

    @Test
    fun corruptedEncryptedValueFailsInsteadOfFallingBackToLegacyPlaintext() {
        preferences.edit()
            .putString("cookie", "ltoken_v2=legacy; ltuid_v2=123456789")
            .putString("cookie_encrypted", "v1:broken")
            .commit()

        assertTrue(runCatching { store.getCookie() }.isFailure)
        assertEquals(
            "ltoken_v2=legacy; ltuid_v2=123456789",
            preferences.getString("cookie", null)
        )
    }
}
