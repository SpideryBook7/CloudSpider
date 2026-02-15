package com.spiderybook.ui.result;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import coil3.load;
import com.spiderybook.databinding.FragmentResultBinding;
import com.spiderybook.ui.common.BaseFragment;
import com.spiderybook.util.Resource;
import dagger.hilt.android.AndroidEntryPoint;
import com.spiderybook.data.manager.AppDownloadManager;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001a\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0016J\b\u0010\u0016\u001a\u00020\u0011H\u0002R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0017"}, d2 = {"Lcom/spiderybook/ui/result/ResultFragment;", "Lcom/spiderybook/ui/common/BaseFragment;", "Lcom/spiderybook/databinding/FragmentResultBinding;", "()V", "downloadManager", "Lcom/spiderybook/data/manager/AppDownloadManager;", "getDownloadManager", "()Lcom/spiderybook/data/manager/AppDownloadManager;", "setDownloadManager", "(Lcom/spiderybook/data/manager/AppDownloadManager;)V", "viewModel", "Lcom/spiderybook/ui/result/ResultViewModel;", "getViewModel", "()Lcom/spiderybook/ui/result/ResultViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "setupObservers", "app_debug"})
public final class ResultFragment extends com.spiderybook.ui.common.BaseFragment<com.spiderybook.databinding.FragmentResultBinding> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @javax.inject.Inject()
    public com.spiderybook.data.manager.AppDownloadManager downloadManager;
    
    public ResultFragment() {
        super(null);
    }
    
    private final com.spiderybook.ui.result.ResultViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.spiderybook.data.manager.AppDownloadManager getDownloadManager() {
        return null;
    }
    
    public final void setDownloadManager(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.manager.AppDownloadManager p0) {
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupObservers() {
    }
}