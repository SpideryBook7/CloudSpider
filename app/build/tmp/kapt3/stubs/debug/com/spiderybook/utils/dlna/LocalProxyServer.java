package com.spiderybook.utils.dlna;

import fi.iki.elonen.NanoHTTPD;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0016J\"\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00072\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00070\u0006R\u001a\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/spiderybook/utils/dlna/LocalProxyServer;", "Lfi/iki/elonen/NanoHTTPD;", "port", "", "(I)V", "headersToInject", "", "", "targetUrl", "serve", "Lfi/iki/elonen/NanoHTTPD$Response;", "session", "Lfi/iki/elonen/NanoHTTPD$IHTTPSession;", "setMediaSource", "", "url", "headers", "app_debug"})
public final class LocalProxyServer extends fi.iki.elonen.NanoHTTPD {
    @org.jetbrains.annotations.Nullable()
    private java.lang.String targetUrl;
    @org.jetbrains.annotations.NotNull()
    private java.util.Map<java.lang.String, java.lang.String> headersToInject;
    
    public LocalProxyServer(int port) {
        super(0);
    }
    
    public final void setMediaSource(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> headers) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public fi.iki.elonen.NanoHTTPD.Response serve(@org.jetbrains.annotations.NotNull()
    fi.iki.elonen.NanoHTTPD.IHTTPSession session) {
        return null;
    }
}