package com.spiderybook.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.spiderybook.data.local.DataStoreManager;
import com.spiderybook.data.repository.SearchRepository;
import com.spiderybook.domain.model.SearchResponse;
import com.spiderybook.domain.model.TvType;
import com.spiderybook.plugins.PluginManager;
import com.spiderybook.ui.common.BaseViewModel;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010!\u001a\u00020\"H\u0002J\"\u0010#\u001a\u00020\"2\b\u0010$\u001a\u0004\u0018\u00010\f2\u0006\u0010%\u001a\u00020\f2\b\b\u0002\u0010&\u001a\u00020\u001cR\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\r\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000b0\u000e0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u001d\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000b0\u000e0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u001d\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000b0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001a\u00a8\u0006\'"}, d2 = {"Lcom/spiderybook/ui/search/SearchViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "searchRepository", "Lcom/spiderybook/data/repository/SearchRepository;", "pluginManager", "Lcom/spiderybook/plugins/PluginManager;", "dataStoreManager", "Lcom/spiderybook/data/local/DataStoreManager;", "(Lcom/spiderybook/data/repository/SearchRepository;Lcom/spiderybook/plugins/PluginManager;Lcom/spiderybook/data/local/DataStoreManager;)V", "_genres", "Landroidx/lifecycle/MutableLiveData;", "", "", "_searchResults", "Lcom/spiderybook/util/Resource;", "Lcom/spiderybook/domain/model/SearchResponse;", "_topSearches", "accumulatedResults", "", "currentApiName", "currentPage", "", "currentQuery", "genres", "Landroidx/lifecycle/LiveData;", "getGenres", "()Landroidx/lifecycle/LiveData;", "isSearching", "", "searchResults", "getSearchResults", "topSearches", "getTopSearches", "loadDefaultContent", "Lkotlinx/coroutines/Job;", "search", "apiName", "query", "isLoadMore", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SearchViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.SearchRepository searchRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.plugins.PluginManager pluginManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.local.DataStoreManager dataStoreManager = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> _searchResults = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> searchResults = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.spiderybook.domain.model.SearchResponse>> _topSearches = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.spiderybook.domain.model.SearchResponse>> topSearches = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<java.lang.String>> _genres = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<java.lang.String>> genres = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentApiName;
    private int currentPage = 1;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String currentQuery = "";
    private boolean isSearching = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.spiderybook.domain.model.SearchResponse> accumulatedResults = null;
    
    @javax.inject.Inject()
    public SearchViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.SearchRepository searchRepository, @org.jetbrains.annotations.NotNull()
    com.spiderybook.plugins.PluginManager pluginManager, @org.jetbrains.annotations.NotNull()
    com.spiderybook.data.local.DataStoreManager dataStoreManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> getSearchResults() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.spiderybook.domain.model.SearchResponse>> getTopSearches() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<java.lang.String>> getGenres() {
        return null;
    }
    
    private final kotlinx.coroutines.Job loadDefaultContent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job search(@org.jetbrains.annotations.Nullable()
    java.lang.String apiName, @org.jetbrains.annotations.NotNull()
    java.lang.String query, boolean isLoadMore) {
        return null;
    }
}