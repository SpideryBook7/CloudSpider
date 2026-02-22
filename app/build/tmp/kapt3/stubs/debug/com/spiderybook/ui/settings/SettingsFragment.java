package com.spiderybook.ui.settings;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatDelegate;
import com.spiderybook.databinding.FragmentSettingsBinding;
import com.spiderybook.ui.common.BaseFragment;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001a\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0016J\b\u0010\u0010\u001a\u00020\u000bH\u0002J\b\u0010\u0011\u001a\u00020\u000bH\u0002J\b\u0010\u0012\u001a\u00020\u000bH\u0002J\b\u0010\u0013\u001a\u00020\u000bH\u0002R\u001b\u0010\u0004\u001a\u00020\u00058BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0014"}, d2 = {"Lcom/spiderybook/ui/settings/SettingsFragment;", "Lcom/spiderybook/ui/common/BaseFragment;", "Lcom/spiderybook/databinding/FragmentSettingsBinding;", "()V", "viewModel", "Lcom/spiderybook/ui/settings/SettingsViewModel;", "getViewModel", "()Lcom/spiderybook/ui/settings/SettingsViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "setupCacheClearing", "setupDownloadsClearing", "setupProviderSelection", "setupThemeSelection", "app_debug"})
public final class SettingsFragment extends com.spiderybook.ui.common.BaseFragment<com.spiderybook.databinding.FragmentSettingsBinding> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    
    public SettingsFragment() {
        super(null);
    }
    
    private final com.spiderybook.ui.settings.SettingsViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupThemeSelection() {
    }
    
    private final void setupProviderSelection() {
    }
    
    @kotlin.OptIn(markerClass = {coil.annotation.ExperimentalCoilApi.class})
    private final void setupCacheClearing() {
    }
    
    private final void setupDownloadsClearing() {
    }
}