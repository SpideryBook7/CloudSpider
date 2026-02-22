package com.spiderybook.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.spiderybook.data.repository.HomeRepository;
import com.spiderybook.domain.model.HomePageResponse;
import com.spiderybook.plugins.PluginManager;
import com.spiderybook.ui.common.BaseViewModel;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020\u000eJ\u0006\u0010/\u001a\u00020-J\b\u00100\u001a\u00020-H\u0002J\u000e\u00101\u001a\u00020-2\u0006\u00102\u001a\u00020\u000eJ\u000e\u00103\u001a\u00020-2\u0006\u00104\u001a\u00020\u000eR\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000e0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000e0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0014\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001e0\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00100\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001cR\u0019\u0010!\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001cR\u001d\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001cR\u0010\u0010%\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00150\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001cR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001cR\u0017\u0010*\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001c\u00a8\u00065"}, d2 = {"Lcom/spiderybook/ui/home/HomeViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "homeRepository", "Lcom/spiderybook/data/repository/HomeRepository;", "pluginManager", "Lcom/spiderybook/plugins/PluginManager;", "dataStoreManager", "Lcom/spiderybook/data/local/DataStoreManager;", "localRepository", "Lcom/spiderybook/data/repository/LocalRepository;", "(Lcom/spiderybook/data/repository/HomeRepository;Lcom/spiderybook/plugins/PluginManager;Lcom/spiderybook/data/local/DataStoreManager;Lcom/spiderybook/data/repository/LocalRepository;)V", "_availableProviders", "Landroidx/lifecycle/MutableLiveData;", "", "", "_displayedContent", "", "_featuredItem", "Lcom/spiderybook/domain/model/SearchResponse;", "_filterCategories", "_homePage", "Lcom/spiderybook/util/Resource;", "Lcom/spiderybook/domain/model/HomePageResponse;", "_selectedCategory", "_selectedProvider", "availableProviders", "Landroidx/lifecycle/LiveData;", "getAvailableProviders", "()Landroidx/lifecycle/LiveData;", "currentHistoryList", "Lcom/spiderybook/data/local/entity/HistoryEntity;", "displayedContent", "getDisplayedContent", "featuredItem", "getFeaturedItem", "filterCategories", "getFilterCategories", "fullHomePageResponse", "homePage", "getHomePage", "selectedCategory", "getSelectedCategory", "selectedProvider", "getSelectedProvider", "loadHomePage", "", "apiName", "loadProviders", "observeHistory", "search", "query", "selectCategory", "category", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.HomeRepository homeRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.plugins.PluginManager pluginManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.local.DataStoreManager dataStoreManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.LocalRepository localRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _selectedProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> selectedProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.HomePageResponse>> _homePage = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.HomePageResponse>> homePage = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<java.lang.String>> _availableProviders = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<java.lang.String>> availableProviders = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.domain.model.SearchResponse> _featuredItem = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.domain.model.SearchResponse> featuredItem = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<java.lang.String>> _filterCategories = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<java.lang.String>> filterCategories = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _selectedCategory = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> selectedCategory = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Object> _displayedContent = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Object> displayedContent = null;
    @org.jetbrains.annotations.Nullable()
    private com.spiderybook.domain.model.HomePageResponse fullHomePageResponse;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.spiderybook.data.local.entity.HistoryEntity> currentHistoryList;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.HomeRepository homeRepository, @org.jetbrains.annotations.NotNull()
    com.spiderybook.plugins.PluginManager pluginManager, @org.jetbrains.annotations.NotNull()
    com.spiderybook.data.local.DataStoreManager dataStoreManager, @org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.LocalRepository localRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getSelectedProvider() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.HomePageResponse>> getHomePage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<java.lang.String>> getAvailableProviders() {
        return null;
    }
    
    public final void loadProviders() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.domain.model.SearchResponse> getFeaturedItem() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<java.lang.String>> getFilterCategories() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getSelectedCategory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Object> getDisplayedContent() {
        return null;
    }
    
    private final void observeHistory() {
    }
    
    public final void loadHomePage(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName) {
    }
    
    public final void selectCategory(@org.jetbrains.annotations.NotNull()
    java.lang.String category) {
    }
    
    public final void search(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
}