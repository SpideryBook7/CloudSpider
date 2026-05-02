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
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Inject JS to forcefully start video playback and click overlays
                view?.evaluateJavascript("""
                    setTimeout(function() {
                        try {
                            const vids = document.querySelectorAll('video');
                            if (vids.length > 0) { vids[0].play(); }
                            const plays = document.querySelectorAll('.play-btn, .vjs-play-control, .jw-icon-playback, .fp-play');
                            if (plays.length > 0) { plays[0].click(); }
                            const ev = new MouseEvent('click', { view: window, bubbles: true, cancelable: true });
                            document.body.dispatchEvent(ev);
                        } catch(e) {}
                    }, 1500);
                    
                    // Keep trying in case of asynchronous player loads
                    setInterval(function() {
                         try {
                             const vids = document.querySelectorAll('video');
                             if (vids.length > 0 && vids[0].paused) { vids[0].play(); }
                             document.body.click();
                         } catch(e) {}
                    }, 3000);
                """.trimIndent(), null)
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val reqUrl = request?.url?.toString() ?: ""
                Log.d("SpiderWebView", "Intercepting resource: $reqUrl")
                
                // M3U8 or MP4 payloads indicate a successful video resolution
                if (reqUrl.contains(".m3u8") || reqUrl.contains(".mp4")) {
                    // Filter out common ad/tracker MP4s
                    if (!reqUrl.contains("blank.mp4") && !reqUrl.contains("ad.mp4")) {
                        Log.d("SpiderWebView", "SUCCESS! Found media payload: $reqUrl")
                        if (!deferred.isCompleted) {
                            deferred.complete(reqUrl)
                            view?.post { view.destroy() }
                        }
                        return WebResourceResponse("text/plain", "UTF-8", null)
                    }
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
            withTimeout(25000) {
                deferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("SpiderWebView", "Timeout reached while intercepting player requests.")
            webView.destroy()
            null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun resolveCloudflareHtml(context: Context, url: String, referer: String? = null): String? = withContext(Dispatchers.Main) {
        val deferred = CompletableDeferred<String?>()
        val webView = WebView(context)
        
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pollForHTML(view)
            }

            private fun pollForHTML(view: WebView?, attempts: Int = 0) {
                if (deferred.isCompleted || attempts > 15) {
                    if (!deferred.isCompleted) {
                        deferred.complete(null)
                        view?.destroy()
                    }
                    return
                }
                view?.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
                    var cleanHtml = html ?: ""
                    if (cleanHtml.contains("eval(function(p,a,c,k,e,d)")) {
                        // Found the packed script!
                        if (cleanHtml.startsWith("\"") && cleanHtml.endsWith("\"")) {
                            cleanHtml = cleanHtml.substring(1, cleanHtml.length - 1)
                                .replace("\\u003C", "<").replace("\\\"", "\"")
                                .replace("\\n", "\n").replace("\\\\", "\\")
                        }
                        if (!deferred.isCompleted) {
                            deferred.complete(cleanHtml)
                            view.destroy()
                        }
                    } else {
                        view?.postDelayed({ pollForHTML(view, attempts + 1) }, 1000)
                    }
                }
            }
        }
        
        if (referer != null) {
            val extraHeaders = mutableMapOf<String, String>()
            extraHeaders["Referer"] = referer
            webView.loadUrl(url, extraHeaders)
        } else {
            webView.loadUrl(url)
        }

        return@withContext try {
            withTimeout(20000) {
                deferred.await()
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("SpiderWebView", "Timeout reached while resolving Cloudflare HTML.")
            webView.destroy()
            null
        }
    }
}
