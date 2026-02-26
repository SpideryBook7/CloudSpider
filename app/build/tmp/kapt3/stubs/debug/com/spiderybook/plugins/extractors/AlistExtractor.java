package com.spiderybook.plugins.extractors;

import kotlinx.coroutines.Dispatchers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/spiderybook/plugins/extractors/AlistExtractor;", "", "()V", "client", "Lokhttp3/OkHttpClient;", "getStreamUrl", "Lcom/spiderybook/plugins/extractors/AlistResult;", "path", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AlistExtractor {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    
    public AlistExtractor() {
        super();
    }
    
    /**
     * Extrae el link directo de video (raw_url) de Terabox a través de tu servidor Alist local.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getStreamUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.spiderybook.plugins.extractors.AlistResult> $completion) {
        return null;
    }
}