package com.spiderybook.ui.downloads;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.spiderybook.databinding.FragmentDownloadsBinding;
import com.spiderybook.ui.common.BaseFragment;
import com.spiderybook.ui.result.EpisodeAdapter;
import com.spiderybook.domain.model.Episode;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.File;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u001a\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0016\u00a8\u0006\n"}, d2 = {"Lcom/spiderybook/ui/downloads/DownloadsFragment;", "Lcom/spiderybook/ui/common/BaseFragment;", "Lcom/spiderybook/databinding/FragmentDownloadsBinding;", "()V", "onViewCreated", "", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "app_debug"})
public final class DownloadsFragment extends com.spiderybook.ui.common.BaseFragment<com.spiderybook.databinding.FragmentDownloadsBinding> {
    
    public DownloadsFragment() {
        super(null);
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
}