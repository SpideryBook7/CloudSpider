package com.spiderybook.utils.dlna

import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Full HLS Reverse Proxy.
 *
 * Problem: Vidhide / Streamwish CDN requires Referer + User-Agent headers on every request.
 * The TV cannot supply these, so we must act as a transparent middleman.
 *
 * Routes:
 *  - GET /video.m3u8  → Fetches real M3U8 from CDN (with auth headers), resolves master→variant,
 *                        rewrites segment URLs so they point back to us, returns modified playlist.
 *  - GET /seg?u=<url> → Fetches the actual .ts segment from CDN (with auth headers), pipes to TV.
 *  - GET /other        -> Falls through to direct proxy for MP4 / any other content type.
 */
class LocalProxyServer(port: Int) : NanoHTTPD(port) {

    private var targetUrl: String? = null
    private var headersToInject: Map<String, String> = emptyMap()

    // The IP/port we are bound to (needed to rewrite segment URLs to point at ourselves)
    var localAddress: String = "127.0.0.1:8192"

    fun setMediaSource(url: String, headers: Map<String, String>) {
        this.targetUrl = url
        // Ensure a User-Agent is always present so CDN doesn't reject requests
        val mutableHeaders = headers.toMutableMap()
        if (!mutableHeaders.containsKey("User-Agent")) {
            mutableHeaders["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
        this.headersToInject = mutableHeaders
    }

    override fun serve(session: IHTTPSession): Response {
        val path = session.uri ?: "/"
        android.util.Log.d("SpideryProxy", "Incoming: ${session.method} $path")

        return when {
            path.endsWith("video.m3u8", ignoreCase = true) -> servePlaylist()
            path.startsWith("/seg", ignoreCase = true)     -> serveSegment(session)
            else                                           -> serveDirectProxy()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Route 1: serve the (rewritten) M3U8 playlist
    // ─────────────────────────────────────────────────────────────────────────
    private fun servePlaylist(): Response {
        val url = targetUrl ?: return notFound("No media source set")

        return try {
            android.util.Log.d("SpideryProxy", "Fetching playlist: $url")
            val content = fetchText(url) ?: return notFound("Empty M3U8 from CDN")

            // Resolve master → best variant
            val playlistText = if (content.contains("#EXT-X-STREAM-INF")) {
                resolveMasterPlaylist(content, url)
            } else {
                content
            }

            // Rewrite segment URLs so the TV requests them through us
            val rewritten = rewriteSegmentUrls(playlistText, url)
            android.util.Log.d("SpideryProxy", "Serving rewritten playlist (${rewritten.lines().size} lines)")

            newFixedLengthResponse(
                Response.Status.OK,
                "application/vnd.apple.mpegurl",
                rewritten
            ).also { r ->
                r.addHeader("Access-Control-Allow-Origin", "*")
                r.addHeader("Cache-Control", "no-cache")
            }
        } catch (e: Exception) {
            android.util.Log.e("SpideryProxy", "Playlist error: ${e.message}", e)
            error500(e.message)
        }
    }

    /** Pick the highest-bandwidth stream from a master playlist and fetch its sub-playlist. */
    private fun resolveMasterPlaylist(master: String, masterUrl: String): String {
        val lines = master.lines()
        var bestBandwidth = -1
        var bestUrl: String? = null

        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                val bwMatch = Regex("BANDWIDTH=(\\d+)").find(line)
                val bw = bwMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                val nextLine = lines.getOrNull(i + 1)?.trim() ?: continue
                if (nextLine.isNotEmpty() && !nextLine.startsWith("#") && bw > bestBandwidth) {
                    bestBandwidth = bw
                    bestUrl = nextLine
                }
            }
        }

        val resolvedVariant = bestUrl?.let { URI(masterUrl).resolve(it).toString() } ?: return master
        android.util.Log.d("SpideryProxy", "Selected variant: $resolvedVariant (bandwidth=$bestBandwidth)")
        return fetchText(resolvedVariant) ?: master
    }

    /**
     * Rewrite each .ts (and any relative .m3u8) URL in the playlist to route through our proxy.
     * Format: http://<localAddress>/seg?u=<URLEncoded-absolute-url>
     */
    private fun rewriteSegmentUrls(playlist: String, playlistUrl: String): String {
        val baseUri = URI(playlistUrl).resolve(".")
        return playlist.lines().joinToString("\n") { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("#") || line.isEmpty() -> line
                else -> {
                    val absolute = if (line.startsWith("http")) line else baseUri.resolve(line).toString()
                    val encoded = URLEncoder.encode(absolute, "UTF-8")
                    "http://$localAddress/seg?u=$encoded"
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Route 2: serve a single TS segment
    // ─────────────────────────────────────────────────────────────────────────
    private fun serveSegment(session: IHTTPSession): Response {
        val encodedUrl = session.parameters["u"]?.firstOrNull()
            ?: return notFound("Missing segment URL")
        val segmentUrl = URLDecoder.decode(encodedUrl, "UTF-8")
        android.util.Log.d("SpideryProxy", "Fetching segment: $segmentUrl")

        return try {
            val connection = openWithHeaders(segmentUrl)
            // Forward Range header from TV if present (needed for seeking)
            session.headers["range"]?.let { connection.setRequestProperty("Range", it) }
            connection.connect()

            val code = connection.responseCode
            val status = when (code) {
                200 -> Response.Status.OK
                206 -> Response.Status.PARTIAL_CONTENT
                else -> Response.Status.lookup(code) ?: Response.Status.INTERNAL_ERROR
            }

            val stream: InputStream = if (code in 200..299) connection.inputStream
                                      else connection.errorStream ?: "".byteInputStream()
            val mime = connection.contentType ?: "video/MP2T"
            val length = connection.contentLength.toLong()

            val response = if (length > 0) newFixedLengthResponse(status, mime, stream, length)
                           else newChunkedResponse(status, mime, stream)

            connection.headerFields.forEach { (k, vs) ->
                if (k != null && vs.isNotEmpty()) response.addHeader(k, vs[0])
            }
            response
        } catch (e: Exception) {
            android.util.Log.e("SpideryProxy", "Segment fetch error: ${e.message}", e)
            error500(e.message)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Route 3: direct proxy for MP4 / non-HLS content
    // ─────────────────────────────────────────────────────────────────────────
    private fun serveDirectProxy(): Response {
        val url = targetUrl ?: return notFound("No media source set")
        return try {
            val connection = openWithHeaders(url)
            connection.connect()

            val code = connection.responseCode
            val status = when (code) {
                200 -> Response.Status.OK
                206 -> Response.Status.PARTIAL_CONTENT
                else -> Response.Status.lookup(code) ?: Response.Status.INTERNAL_ERROR
            }
            val stream: InputStream = if (code in 200..299) connection.inputStream
                                      else connection.errorStream ?: "".byteInputStream()
            val mime = connection.contentType ?: "video/mp4"

            val response = newChunkedResponse(status, mime, stream)
            connection.headerFields.forEach { (k, vs) ->
                if (k != null && vs.isNotEmpty()) response.addHeader(k, vs[0])
            }
            response
        } catch (e: Exception) {
            android.util.Log.e("SpideryProxy", "Direct proxy error: ${e.message}", e)
            error500(e.message)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private fun openWithHeaders(url: String): HttpURLConnection {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout    = 20_000
        headersToInject.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        return conn
    }

    private fun fetchText(url: String): String? {
        val conn = openWithHeaders(url)
        return try {
            conn.connect()
            val code = conn.responseCode
            if (code !in 200..299) {
                android.util.Log.w("SpideryProxy", "fetchText $url -> $code")
                null
            } else {
                conn.inputStream.bufferedReader().readText()
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun notFound(msg: String)   = newFixedLengthResponse(Response.Status.NOT_FOUND,    MIME_PLAINTEXT, msg)
    private fun error500(msg: String?)  = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, msg ?: "Error")
}