package com.spiderybook.plugins.providers;

import com.spiderybook.domain.model.HomePageList;
import com.spiderybook.domain.model.HomePageResponse;
import com.spiderybook.domain.model.SearchResponse;
import com.spiderybook.domain.model.TvType;
import com.spiderybook.plugins.MainAPI;
import org.jsoup.Jsoup;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rJ\u0018\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0010\u001a\u00020\u0004H\u0096@\u00a2\u0006\u0002\u0010\u0011J*\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00042\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00180\u0016H\u0096@\u00a2\u0006\u0002\u0010\u0019J\u001e\u0010\u001a\u001a\n\u0012\u0004\u0012\u00020\u001c\u0018\u00010\u001b2\u0006\u0010\u001d\u001a\u00020\u0004H\u0096@\u00a2\u0006\u0002\u0010\u0011R\u0014\u0010\u0003\u001a\u00020\u0004X\u0096D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0096D\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006\u00a8\u0006\u001e"}, d2 = {"Lcom/spiderybook/plugins/providers/AnimeFlvProvider;", "Lcom/spiderybook/plugins/MainAPI;", "()V", "mainUrl", "", "getMainUrl", "()Ljava/lang/String;", "name", "getName", "getMainPage", "Lcom/spiderybook/domain/model/HomePageResponse;", "page", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "load", "Lcom/spiderybook/domain/model/LoadResponse;", "url", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadLinks", "", "data", "callback", "Lkotlin/Function1;", "Lcom/spiderybook/plugins/MainAPI$ExtractorLink;", "", "(Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "", "Lcom/spiderybook/domain/model/SearchResponse;", "query", "app_debug"})
public final class AnimeFlvProvider extends com.spiderybook.plugins.MainAPI {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = "AnimeFLV";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String mainUrl = "https://www3.animeflv.net";
    
    @javax.inject.Inject()
    public AnimeFlvProvider() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getName() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getMainUrl() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMainPage(int page, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.spiderybook.domain.model.HomePageResponse> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object search(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
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
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object loadLinks(@org.jetbrains.annotations.NotNull()
    java.lang.String data, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.spiderybook.plugins.MainAPI.ExtractorLink, kotlin.Unit> callback, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}