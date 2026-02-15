package com.spiderybook.plugins;

import com.spiderybook.domain.model.HomePageResponse;
import com.spiderybook.domain.model.LoadResponse;
import com.spiderybook.domain.model.SearchResponse;
import com.spiderybook.domain.model.TvType;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\"\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\b&\u0018\u00002\u00020\u0001:\u0001\"B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0096@\u00a2\u0006\u0002\u0010\u001cJ\u001e\u0010\u001d\u001a\n\u0012\u0004\u0012\u00020\u001f\u0018\u00010\u001e2\u0006\u0010 \u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010!R\u0014\u0010\u0003\u001a\u00020\u0004X\u0096D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\u0004X\u0096D\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0006R\u0014\u0010\t\u001a\u00020\u0004X\u0096D\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0006R\u0014\u0010\u000b\u001a\u00020\fX\u0096D\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0012\u0010\u000f\u001a\u00020\fX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u000eR\u0012\u0010\u0011\u001a\u00020\fX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u000eR\u001a\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006#"}, d2 = {"Lcom/spiderybook/plugins/MainAPI;", "", "()V", "hasMainPage", "", "getHasMainPage", "()Z", "hasQuickSearch", "getHasQuickSearch", "hasSearch", "getHasSearch", "lang", "", "getLang", "()Ljava/lang/String;", "mainUrl", "getMainUrl", "name", "getName", "supportedTypes", "", "Lcom/spiderybook/domain/model/TvType;", "getSupportedTypes", "()Ljava/util/Set;", "getMainPage", "Lcom/spiderybook/domain/model/HomePageResponse;", "page", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "search", "", "Lcom/spiderybook/domain/model/SearchResponse;", "query", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "MainAPI", "app_debug"})
public abstract class MainAPI {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String lang = "en";
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<com.spiderybook.domain.model.TvType> supportedTypes = null;
    private final boolean hasMainPage = false;
    private final boolean hasQuickSearch = true;
    private final boolean hasSearch = true;
    
    public MainAPI() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getName();
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getMainUrl();
    
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getLang() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public java.util.Set<com.spiderybook.domain.model.TvType> getSupportedTypes() {
        return null;
    }
    
    public boolean getHasMainPage() {
        return false;
    }
    
    public boolean getHasQuickSearch() {
        return false;
    }
    
    public boolean getHasSearch() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getMainPage(int page, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.spiderybook.domain.model.HomePageResponse> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object search(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.spiderybook.domain.model.SearchResponse>> $completion) {
        return null;
    }
}