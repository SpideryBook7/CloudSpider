package com.spiderybook.utils.dlna;

import android.content.Context;
import android.net.wifi.WifiManager;
import kotlinx.coroutines.Dispatchers;
import org.jsoup.Jsoup;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0018\u0010\u0011\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u0012\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0013J\n\u0010\u0014\u001a\u0004\u0018\u00010\u0006H\u0002J*\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u00062\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0006H\u0086@\u00a2\u0006\u0002\u0010\u001aR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/spiderybook/utils/dlna/DLNAManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "SEARCH_MESSAGE", "", "SSDP_IP", "SSDP_PORT", "", "localProxyServer", "Lcom/spiderybook/utils/dlna/LocalProxyServer;", "discoverDevices", "", "Lcom/spiderybook/utils/dlna/DLNADevice;", "timeoutMs", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchDeviceDetails", "locationUrl", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLocalIpAddress", "playMedia", "", "device", "mediaUrl", "mediaReferer", "(Lcom/spiderybook/utils/dlna/DLNADevice;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class DLNAManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String SSDP_IP = "239.255.255.250";
    private final int SSDP_PORT = 1900;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String SEARCH_MESSAGE = null;
    @org.jetbrains.annotations.Nullable()
    private com.spiderybook.utils.dlna.LocalProxyServer localProxyServer;
    
    public DLNAManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final java.lang.String getLocalIpAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object discoverDevices(int timeoutMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.spiderybook.utils.dlna.DLNADevice>> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchDeviceDetails(java.lang.String locationUrl, kotlin.coroutines.Continuation<? super com.spiderybook.utils.dlna.DLNADevice> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object playMedia(@org.jetbrains.annotations.NotNull()
    com.spiderybook.utils.dlna.DLNADevice device, @org.jetbrains.annotations.NotNull()
    java.lang.String mediaUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String mediaReferer, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}