package com.spiderybook.ui.player;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.spiderybook.data.repository.LoadRepository;
import com.spiderybook.plugins.MainAPI;
import com.spiderybook.ui.common.BaseViewModel;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011R \u0010\u0005\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\n\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u00070\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/spiderybook/ui/player/PlayerViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "loadRepository", "Lcom/spiderybook/data/repository/LoadRepository;", "(Lcom/spiderybook/data/repository/LoadRepository;)V", "_links", "Landroidx/lifecycle/MutableLiveData;", "Lcom/spiderybook/util/Resource;", "", "Lcom/spiderybook/plugins/MainAPI$ExtractorLink;", "links", "Landroidx/lifecycle/LiveData;", "getLinks", "()Landroidx/lifecycle/LiveData;", "loadLinks", "Lkotlinx/coroutines/Job;", "apiName", "", "data", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class PlayerViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.LoadRepository loadRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.plugins.MainAPI.ExtractorLink>>> _links = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.plugins.MainAPI.ExtractorLink>>> links = null;
    
    @javax.inject.Inject()
    public PlayerViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.LoadRepository loadRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.util.List<com.spiderybook.plugins.MainAPI.ExtractorLink>>> getLinks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadLinks(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName, @org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
}