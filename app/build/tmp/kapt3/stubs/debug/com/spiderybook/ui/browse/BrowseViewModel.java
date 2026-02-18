package com.spiderybook.ui.browse;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.spiderybook.domain.model.SearchResponse;
import com.spiderybook.plugins.providers.AnimeFlvProvider;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0015\u001a\u00020\u0016R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\b\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000b0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R#\u0010\u0011\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000b0\n0\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/spiderybook/ui/browse/BrowseViewModel;", "Landroidx/lifecycle/ViewModel;", "provider", "Lcom/spiderybook/plugins/providers/AnimeFlvProvider;", "(Lcom/spiderybook/plugins/providers/AnimeFlvProvider;)V", "_currentList", "", "Lcom/spiderybook/domain/model/SearchResponse;", "_items", "Landroidx/lifecycle/MutableLiveData;", "Lcom/spiderybook/util/Resource;", "", "currentPage", "", "isLastPage", "", "isLoading", "items", "Landroidx/lifecycle/LiveData;", "getItems", "()Landroidx/lifecycle/LiveData;", "loadNextPage", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BrowseViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.plugins.providers.AnimeFlvProvider provider = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> _items = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> items = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.spiderybook.domain.model.SearchResponse> _currentList = null;
    private int currentPage = 1;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    
    @javax.inject.Inject()
    public BrowseViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.plugins.providers.AnimeFlvProvider provider) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.domain.model.SearchResponse>>> getItems() {
        return null;
    }
    
    public final void loadNextPage() {
    }
}