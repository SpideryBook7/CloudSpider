package com.spiderybook.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.spiderybook.data.repository.LoadRepository;
import com.spiderybook.domain.model.LoadResponse;
import com.spiderybook.ui.common.BaseViewModel;
import com.spiderybook.util.Resource;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0012"}, d2 = {"Lcom/spiderybook/ui/result/ResultViewModel;", "Lcom/spiderybook/ui/common/BaseViewModel;", "loadRepository", "Lcom/spiderybook/data/repository/LoadRepository;", "(Lcom/spiderybook/data/repository/LoadRepository;)V", "_result", "Landroidx/lifecycle/MutableLiveData;", "Lcom/spiderybook/util/Resource;", "Lcom/spiderybook/domain/model/LoadResponse;", "result", "Landroidx/lifecycle/LiveData;", "getResult", "()Landroidx/lifecycle/LiveData;", "load", "Lkotlinx/coroutines/Job;", "apiName", "", "url", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ResultViewModel extends com.spiderybook.ui.common.BaseViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.spiderybook.data.repository.LoadRepository loadRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.LoadResponse>> _result = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.spiderybook.util.Resource<com.spiderybook.domain.model.LoadResponse>> result = null;
    
    @javax.inject.Inject()
    public ResultViewModel(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.LoadRepository loadRepository) {
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
}