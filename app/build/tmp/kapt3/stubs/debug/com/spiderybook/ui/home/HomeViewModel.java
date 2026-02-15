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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\tJ\u0006\u0010\u0011\u001a\u00020\u000fR\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0012"}, d2 = {"Lcom/spiderybook/ui/home/HomeViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "homeRepository", "Lcom/spiderybook/data/repository/HomeRepository;", "pluginManager", "Lcom/spiderybook/plugins/PluginManager;", "(Lcom/spiderybook/data/repository/HomeRepository;Lcom/spiderybook/plugins/PluginManager;)V", "_selectedProvider", "Landroidx/lifecycle/MutableLiveData;", "", "selectedProvider", "Landroidx/lifecycle/LiveData;", "getSelectedProvider", "()Landroidx/lifecycle/LiveData;", "loadHomePage", "", "apiName", "loadProviders", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.HomeRepository homeRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.plugins.PluginManager pluginManager = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _selectedProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> selectedProvider = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.HomeRepository homeRepository, @org.jetbrains.annotations.NotNull()
    com.spiderybook.plugins.PluginManager pluginManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getSelectedProvider() {
        return null;
    }
    
    public final void loadProviders() {
    }
    
    public final void loadHomePage(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName) {
    }
}