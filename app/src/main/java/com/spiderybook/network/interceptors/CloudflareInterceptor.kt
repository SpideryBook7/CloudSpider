package com.spiderybook.network.interceptors

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class CloudflareInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (isCloudflareChallenge(response)) {
            response.close()
            // In a real implementation, this would trigger a WebView or specialized solver
            // For now, we'll iterate on this later as requested (simplified)
            return chain.proceed(request) 
        }

        return response
    }

    private fun isCloudflareChallenge(response: Response): Boolean {
        return response.code == 503 && response.header("Server")?.contains("cloudflare") == true
    }
}
