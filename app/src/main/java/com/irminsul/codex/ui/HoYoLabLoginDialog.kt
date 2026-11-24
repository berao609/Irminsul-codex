package com.irminsul.codex.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HoYoLabLoginDialog(
    onCookieCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isPageLoading by remember { mutableStateOf(true) }
    var captured by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    fun wipeWebView(view: WebView?) {
        val cm = CookieManager.getInstance()
        cm.removeAllCookies(null)
        cm.flush()
        view?.apply {
            clearCache(true)
            clearHistory()
            clearFormData()
        }
    }

    fun checkAndExtractCookies(view: WebView?) {
        if (captured) return
        val cm = CookieManager.getInstance()
        val rawCookies = listOf(
            cm.getCookie("https://www.hoyolab.com"),
            cm.getCookie("https://act.hoyolab.com")
        ).filterNotNull().joinToString("; ")

        val cookieMap = mutableMapOf<String, String>()
        rawCookies.split(";").forEach { part ->
            val trimmed = part.trim()
            val eqIdx = trimmed.indexOf('=')
            if (eqIdx > 0) {
                val key = trimmed.substring(0, eqIdx).trim()
                val value = trimmed.substring(eqIdx + 1).trim()
                if (value.isNotEmpty()) {
                    cookieMap[key] = value
                }
            }
        }

        val ltoken = cookieMap["ltoken_v2"]
        val ltuid = cookieMap["ltuid_v2"]

        if (!ltoken.isNullOrEmpty() && !ltuid.isNullOrEmpty()) {
            captured = true
            val cleanCookie = "ltuid_v2=$ltuid; ltoken_v2=$ltoken"
            wipeWebView(view)
            onCookieCaptured(cleanCookie)
        }
    }

    Dialog(
        onDismissRequest = {
            wipeWebView(webViewRef)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        wipeWebView(webViewRef)
                        onDismiss()
                    }) {
                        Text("Cancel")
                    }
                    Text(
                        text = "Sign in to HoYoLAB",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { checkAndExtractCookies(webViewRef) }) {
                        Text("Done")
                    }
                }

                if (isPageLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        val cm = CookieManager.getInstance()
                        cm.removeAllCookies(null)
                        cm.flush()

                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            cm.setAcceptCookie(true)
                            cm.setAcceptThirdPartyCookies(this, true)

                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true

                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean = false

                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    isPageLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isPageLoading = false
                                    checkAndExtractCookies(view)
                                }

                                override fun doUpdateVisitedHistory(
                                    view: WebView?,
                                    url: String?,
                                    isReload: Boolean
                                ) {
                                    checkAndExtractCookies(view)
                                }
                            }

                            webViewRef = this
                            loadUrl("https://www.hoyolab.com/")
                        }
                    },
                    onRelease = { view ->
                        view.destroy()
                    }
                )
            }
        }
    }
}
