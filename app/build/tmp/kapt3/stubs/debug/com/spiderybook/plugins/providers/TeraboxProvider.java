package com.spiderybook.plugins.providers;

import com.spiderybook.BuildConfig;
import com.spiderybook.domain.model.TvType;
import com.spiderybook.plugins.MainAPI;
import com.spiderybook.plugins.extractors.AlistExtractor;
import com.spiderybook.plugins.extractors.AlistResult;
import kotlinx.coroutines.Dispatchers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;

/**
 * A native provider that interacts directly with a local Terabox/Alist server,
 * fetching actual directories and files as if they were movies/shows.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J&\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u00172\u0006\u0010\u0019\u001a\u00020\u00062\b\b\u0002\u0010\u001a\u001a\u00020\u001bH\u0082@\u00a2\u0006\u0002\u0010\u001cJ\u001c\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00180\u00172\u0006\u0010\u0019\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u001eJ\u0018\u0010\u001f\u001a\u0004\u0018\u00010 2\u0006\u0010!\u001a\u00020\u001bH\u0096@\u00a2\u0006\u0002\u0010\"J\u0018\u0010#\u001a\u0004\u0018\u00010$2\u0006\u0010%\u001a\u00020\u0006H\u0096@\u00a2\u0006\u0002\u0010\u001eJ*\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\u00062\u0012\u0010)\u001a\u000e\u0012\u0004\u0012\u00020+\u0012\u0004\u0012\u00020,0*H\u0096@\u00a2\u0006\u0002\u0010-J&\u0010.\u001a\n\u0012\u0004\u0012\u00020/\u0018\u00010\u00172\u0006\u00100\u001a\u00020\u00062\u0006\u0010!\u001a\u00020\u001bH\u0096@\u00a2\u0006\u0002\u0010\u001cR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u00020\u0006X\u0096\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001a\u0010\u000b\u001a\u00020\u0006X\u0096\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\b\"\u0004\b\r\u0010\nR\u001a\u0010\u000e\u001a\u00020\u0006X\u0096\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\b\"\u0004\b\u0010\u0010\nR\u001a\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u00061"}, d2 = {"Lcom/spiderybook/plugins/providers/TeraboxProvider;", "Lcom/spiderybook/plugins/MainAPI;", "()V", "client", "Lokhttp3/OkHttpClient;", "lang", "", "getLang", "()Ljava/lang/String;", "setLang", "(Ljava/lang/String;)V", "mainUrl", "getMainUrl", "setMainUrl", "name", "getName", "setName", "supportedTypes", "", "Lcom/spiderybook/domain/model/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "fetchAllVideos", "", "Lorg/json/JSONObject;", "path", "depth", "", "(Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchDirectoryFiles", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMainPage", "Lcom/spiderybook/domain/model/HomePageResponse;", "page", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/spiderybook/domain/model/LoadResponse;", "url", "loadLinks", "", "data", "callback", "Lkotlin/Function1;", "Lcom/spiderybook/plugins/MainAPI$ExtractorLink;", "", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "Lcom/spiderybook/domain/model/SearchResponse;", "query", "app_debug"})
public final class TeraboxProvider extends com.spiderybook.plugins.MainAPI {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String mainUrl;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String name = "Terabox";
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<com.spiderybook.domain.model.TvType> supportedTypes = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String lang = "es";
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    
    public TeraboxProvider() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getMainUrl() {
        return null;
    }
    
    public void setMainUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getName() {
        return null;
    }
    
    public void setName(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.util.Set<com.spiderybook.domain.model.TvType> getSupportedTypes() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getLang() {
        return null;
    }
    
    public void setLang(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    /**
     * Replaces the homepage by scanning the root Terabox directory (`/terabox`).
     * Lists all folders/files and returns them as SearchResponse items.
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMainPage(int page, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.spiderybook.domain.model.HomePageResponse> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object search(@org.jetbrains.annotations.NotNull()
    java.lang.String query, int page, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.spiderybook.domain.model.SearchResponse>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object load(@org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.spiderybook.domain.model.LoadResponse> $completion) {
        return null;
    }
    
    /**
     * Resolves the "episode" path into the raw direct MP4 link, executing our `AlistExtractor`.
     */
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object loadLinks(@org.jetbrains.annotations.NotNull()
    java.lang.String data, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.spiderybook.plugins.MainAPI.ExtractorLink, kotlin.Unit> callback, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchAllVideos(java.lang.String path, int depth, kotlin.coroutines.Continuation<? super java.util.List<? extends org.json.JSONObject>> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchDirectoryFiles(java.lang.String path, kotlin.coroutines.Continuation<? super java.util.List<? extends org.json.JSONObject>> $completion) {
        return null;
    }
}