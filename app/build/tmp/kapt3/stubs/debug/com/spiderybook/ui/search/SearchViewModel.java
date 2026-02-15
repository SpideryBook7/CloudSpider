package com.spiderybook.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.spiderybook.data.repository.SearchRepository;
import com.spiderybook.domain.model.SearchResponse;
import com.spiderybook.ui.common.BaseViewModel;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011R \u0010\u0005\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\n\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0013"}, d2 = {"Lcom/spiderybook/ui/search/SearchViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "searchRepository", "Lcom/spiderybook/data/repository/SearchRepository;", "(Lcom/spiderybook/data/repository/SearchRepository;)V", "_searchResults", "Landroidx/lifecycle/MutableLiveData;", "Lcom/spiderybook/util/Resource;", "", "Lcom/spiderybook/domain/model/SearchResponse;", "searchResults", "Landroidx/lifecycle/LiveData;", "getSearchResults", "()Landroidx/lifecycle/LiveData;", "search", "Lkotlinx/coroutines/Job;", "apiName", "", "query", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SearchViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.SearchRepository searchRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> _searchResults = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> searchResults = null;
    
    @javax.inject.Inject()
    public SearchViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.SearchRepository searchRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> getSearchResults() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job search(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName, @org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
}