package com.spiderybook.plugins.extractors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

sealed class AlistResult {
    data class Success(val url: String) : AlistResult()
    object FileNotFound : AlistResult()
    object AuthError : AlistResult()
    data class NetworkError(val message: String) : AlistResult()
}

class AlistExtractor {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    /**
     * Extrae el link directo de video (raw_url) de Terabox a través de tu servidor Alist local.
     */
    suspend fun getStreamUrl(path: String): AlistResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val alistUrl = com.spiderybook.BuildConfig.ALIST_URL.replace("\"", "").trimEnd('/')
            val endpoint = "$alistUrl/api/fs/get"
            
            val jsonBody = JSONObject().apply {
                put("path", path)
                put("password", "")
            }.toString()

            val body = jsonBody.toRequestBody("application/json".toMediaType())
            val authToken = com.spiderybook.BuildConfig.ALIST_TOKEN.replace("\"", "")

            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .cacheControl(okhttp3.CacheControl.FORCE_NETWORK)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", authToken)
                .build()

            val response = client.newCall(request).execute()
            
            // Si el servidor nos bloquea la conexión o el token es inválido
            if (response.code == 401 || response.code == 403) {
                return@withContext AlistResult.AuthError
            }
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    val code = json.optInt("code")
                    val message = json.optString("message", "")
                    
                    if (code == 200) {
                        val dataObj = json.optJSONObject("data")
                        val rawUrl = dataObj?.optString("raw_url")
                        val sign = dataObj?.optString("sign", "")
                        
                        // Si nos responde OK pero no hay URL o es un folder
                        if (!rawUrl.isNullOrEmpty()) {
                            // Alist can return relative paths (e.g. "/d/terabox/...") if Site URL is not configured
                            var fullUrl = rawUrl
                            if (!fullUrl.startsWith("http")) {
                                val baseUrl = com.spiderybook.BuildConfig.ALIST_URL.replace("\"", "").trimEnd('/')
                                fullUrl = if (fullUrl.startsWith("/")) "$baseUrl$fullUrl" else "$baseUrl/$fullUrl"
                            }
                            
                            // ExoPlayer crashes if the HTTP URL has raw spaces.
                            // Terabox Native URLs are already encoded by the CDN properly, we only encode spaces for Alist proxy paths limit.
                            val exoPlayerSafeUrl = fullUrl.replace(" ", "%20")
                            return@withContext AlistResult.Success(exoPlayerSafeUrl)
                        } else {
                            return@withContext AlistResult.FileNotFound
                        }
                    } else if (code == 500 || message.contains("not found", ignoreCase = true) || message.contains("file", ignoreCase = true)) {
                        // Alist suele regresar mensaje de "path not found" si el archivo no existe
                        return@withContext AlistResult.FileNotFound
                    } else {
                         return@withContext AlistResult.NetworkError("Alist Error Co: $code - $message")
                    }
                }
            }
            
            AlistResult.NetworkError("Error HTTP ${response.code}")
        } catch (e: java.net.ConnectException) {
            e.printStackTrace()
            AlistResult.NetworkError("No se pudo conectar al servidor 192.168.1.252. ¿Está encendido Linux?")
        } catch (e: Exception) {
            e.printStackTrace()
            AlistResult.NetworkError(e.localizedMessage ?: "Fallo desconocido")
        }
    }
}
