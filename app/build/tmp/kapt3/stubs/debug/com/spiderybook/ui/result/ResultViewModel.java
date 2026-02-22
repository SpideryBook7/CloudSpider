package com.spiderybook.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.spiderybook.data.repository.LoadRepository;
import com.spiderybook.domain.model.LoadResponse;
import com.spiderybook.ui.common.BaseViewModel;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\fJ&\u0010\u001a\u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\n2\u0006\u0010\u001d\u001a\u00020\n2\u0006\u0010\u001e\u001a\u00020\u001fJ\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u000e2\u0006\u0010\"\u001a\u00020\nJ\u0016\u0010#\u001a\u00020\u00182\u0006\u0010\u001b\u001a\u00020\n2\u0006\u0010\"\u001a\u00020\nJ\u000e\u0010$\u001a\u00020\u00182\u0006\u0010\"\u001a\u00020\nJ\u0010\u0010%\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\fR\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00120\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\t0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010\u00a8\u0006&"}, d2 = {"Lcom/spiderybook/ui/result/ResultViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "loadRepository", "Lcom/spiderybook/data/repository/LoadRepository;", "localRepository", "Lcom/spiderybook/data/repository/LocalRepository;", "(Lcom/spiderybook/data/repository/LoadRepository;Lcom/spiderybook/data/repository/LocalRepository;)V", "_downloadStatus", "Landroidx/lifecycle/MutableLiveData;", "Lcom/spiderybook/util/Resource;", "", "_result", "Lcom/spiderybook/domain/model/LoadResponse;", "downloadStatus", "Landroidx/lifecycle/LiveData;", "getDownloadStatus", "()Landroidx/lifecycle/LiveData;", "history", "", "Lcom/spiderybook/data/local/entity/HistoryEntity;", "getHistory", "result", "getResult", "addToFavorites", "Lkotlinx/coroutines/Job;", "currentItem", "downloadEpisode", "apiName", "episodeUrl", "fileName", "downloadManager", "Lcom/spiderybook/data/manager/AppDownloadManager;", "isFavorite", "", "url", "load", "removeFromFavorites", "toggleFavorite", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ResultViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.LoadRepository loadRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.LocalRepository localRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.LoadResponse>> _result = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.LoadResponse>> result = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<java.lang.String>> _downloadStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.lang.String>> downloadStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.spiderybook.data.local.entity.HistoryEntity>> history = null;
    
    @javax.inject.Inject()
    public ResultViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.LoadRepository loadRepository, @org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.LocalRepository localRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.LoadResponse>> getResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job load(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<java.lang.String>> getDownloadStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job downloadEpisode(@org.jetbrains.annotations.NotNull()
    java.lang.String apiName, @org.jetbrains.annotations.NotNull()
    java.lang.String episodeUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String fileName, @org.jetbrains.annotations.NotNull()
    com.spiderybook.data.manager.AppDownloadManager downloadManager) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Boolean> isFavorite(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.spiderybook.data.local.entity.HistoryEntity>> getHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job toggleFavorite(@org.jetbrains.annotations.Nullable()
    com.spiderybook.domain.model.LoadResponse currentItem) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job addToFavorites(@org.jetbrains.annotations.NotNull()
    com.spiderybook.domain.model.LoadResponse currentItem) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job removeFromFavorites(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
}