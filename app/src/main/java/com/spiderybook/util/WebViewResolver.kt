package com.spiderybook.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.*

object WebViewResolver {

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun extractAnimeFlvVideos(context: Context, url: String): String? = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<String?>()
        val webView = WebView(context)
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("SpiderWebView-JS", "JS: ${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                return super.onConsoleMessage(consoleMessage)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("SpiderWebView", "Page finished loading: $url. Checking for 'videos' variable...")
                checkVideosVariable(view)
            }

            private fun checkVideosVariable(view: WebView?, attempts: Int = 0) {
                if (deferred.isCompleted || attempts > 10) {
                    if (!deferred.isCompleted) deferred.complete(null)
                    return
                }

                view?.evaluateJavascript("typeof videos !== 'undefined' ? JSON.stringify(videos) : 'null';") { result ->
                    Log.d("SpiderWebView", "JS Eval Result (Attempt $attempts): $result")
                    
                    if (result != null && result != "null" && result != "\"null\"") {
                        var cleanResult = result
                        if (cleanResult.startsWith("\"") && cleanResult.endsWith("\"")) {
                            cleanResult = cleanResult.substring(1, cleanResult.length - 1)
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                        }
                        
                        if (!deferred.isCompleted) {
                            deferred.complete(cleanResult)
                            view?.destroy()
                        }
                    } else {
                        // Try again in 500ms
                        view?.postDelayed({ checkVideosVariable(view, attempts + 1) }, 500)
                    }
                }
            }
        }

        Log.d("SpiderWebView", "Starting AnimeFLV extraction for: $url")
        webView.loadUrl(url)
        
        return@withContext try {
            withTimeout(25000) {
                deferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("SpiderWebView", "Timeout reached while extracting AnimeFLV videos.")
            webView.destroy()
            null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun interceptVideoUrl(context: Context, url: String, referer: String? = null): String? = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<String?>()
        val webView = WebView(context)
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
            mediaPlaybackRequiresUserGesture = false // Crucial for auto-playing to trigger network requests
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val reqUrl = request?.url?.toString() ?: ""
                Log.d("SpiderWebView", "Intercepting resource: $reqUrl")
                
                if (reqUrl.contains(".m3u8") || reqUrl.contains(".mp4")) {
                    Log.d("SpiderWebView", "SUCCESS! Found media payload: $reqUrl")
                    if (!deferred.isCompleted) {
                        deferred.complete(reqUrl)
                        view?.post { view.destroy() }
                    }
                    return WebResourceResponse("text/plain", "UTF-8", null)
                }
                
                return super.shouldInterceptRequest(view, request)
            }
        }
        
        if (referer != null) {
            val extraHeaders = mutableMapOf<String, String>()
            extraHeaders["Referer"] = referer
            Log.d("SpiderWebView", "Starting Player interception for: $url (Referer: $referer)")
            webView.loadUrl(url, extraHeaders)
        } else {
            Log.d("SpiderWebView", "Starting Player interception for: $url (No Referer)")
            webView.loadUrl(url)
        }

        return@withContext try {
            withTimeout(15000) {
                deferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("SpiderWebView", "Timeout reached while intercepting player requests.")
            webView.destroy()
            null
        }
    }
}
