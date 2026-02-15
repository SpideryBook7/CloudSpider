package com.spiderybook.data.repository;

import com.spiderybook.domain.model.SearchResponse;
import com.spiderybook.plugins.PluginManager;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J&\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/spiderybook/data/repository/SearchRepository;", "", "pluginManager", "Lcom/spiderybook/plugins/PluginManager;", "(Lcom/spiderybook/plugins/PluginManager;)V", "search", "", "Lcom/spiderybook/domain/model/SearchResponse;", "apiName", "", "query", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SearchRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.plugins.PluginManager pluginManager = null;
    
    @javax.inject.Inject()
    public SearchRepository(@org.jetbrains.annotations.NotNull()
    com.spiderybook.plugins.PluginManager pluginManager) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object search(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName, @org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.spiderybook.domain.model.SearchResponse>> $completion) {
        return null;
    }
}