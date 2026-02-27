package com.spiderybook.ui.player;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import com.spiderybook.databinding.ActivityPlayerBinding;
import com.spiderybook.util.Resource;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\u0014H\u0002J\u0018\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u00062\u0006\u0010 \u001a\u00020!H\u0003J\b\u0010\"\u001a\u00020#H\u0002J\u001a\u0010$\u001a\u00020#2\u0006\u0010\u001f\u001a\u00020\u00062\b\u0010%\u001a\u0004\u0018\u00010\u0006H\u0003J\u0012\u0010&\u001a\u00020#2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0014J\b\u0010)\u001a\u00020#H\u0014J\b\u0010*\u001a\u00020#H\u0002J\b\u0010+\u001a\u00020#H\u0003J\u0016\u0010,\u001a\u00020#2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020/0.H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u000b\u001a\u00020\f8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0015\u001a\u00020\u00168BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0019\u0010\u001a\u001a\u0004\b\u0017\u0010\u0018\u00a8\u00060"}, d2 = {"Lcom/spiderybook/ui/player/PlayerActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/spiderybook/databinding/ActivityPlayerBinding;", "currentMediaReferer", "", "currentMediaTitle", "currentMediaUrl", "dlnaManager", "Lcom/spiderybook/utils/dlna/DLNAManager;", "localRepository", "Lcom/spiderybook/data/repository/LocalRepository;", "getLocalRepository", "()Lcom/spiderybook/data/repository/LocalRepository;", "setLocalRepository", "(Lcom/spiderybook/data/repository/LocalRepository;)V", "player", "Landroidx/media3/exoplayer/ExoPlayer;", "startPosition", "", "viewModel", "Lcom/spiderybook/ui/player/PlayerViewModel;", "getViewModel", "()Lcom/spiderybook/ui/player/PlayerViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "generatedTime", "ms", "getStreamwishSource", "Landroidx/media3/exoplayer/source/MediaSource;", "url", "dataSourceFactory", "Landroidx/media3/datasource/okhttp/OkHttpDataSource$Factory;", "hideSystemUi", "", "initializePlayer", "referer", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onStop", "releasePlayer", "setupObservers", "showSourceSelectionDialog", "links", "", "Lcom/spiderybook/plugins/MainAPI$ExtractorLink;", "app_debug"})
public final class PlayerActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.spiderybook.databinding.ActivityPlayerBinding binding;
    @org.jetbrains.annotations.Nullable()
    private androidx.media3.exoplayer.ExoPlayer player;
    private long startPosition = 0L;
    private com.spiderybook.utils.dlna.DLNAManager dlnaManager;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentMediaUrl;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentMediaReferer;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentMediaTitle;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @javax.inject.Inject()
    public com.spiderybook.data.repository.LocalRepository localRepository;
    
    public PlayerActivity() {
        super();
    }
    
    private final com.spiderybook.ui.player.PlayerViewModel getViewModel() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.spiderybook.data.repository.LocalRepository getLocalRepository() {
        return null;
    }
    
    public final void setLocalRepository(@org.jetbrains.annotations.NotNull()
    com.spiderybook.data.repository.LocalRepository p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    private final void setupObservers() {
    }
    
    private final void showSourceSelectionDialog(java.util.List<com.spiderybook.plugins.MainAPI.ExtractorLink> links) {
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    private final void initializePlayer(java.lang.String url, java.lang.String referer) {
    }
    
    @java.lang.Override()
    protected void onStop() {
    }
    
    private final void releasePlayer() {
    }
    
    private final void hideSystemUi() {
    }
    
    private final java.lang.String generatedTime(long ms) {
        return null;
    }
    
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    private final androidx.media3.exoplayer.source.MediaSource getStreamwishSource(java.lang.String url, androidx.media3.datasource.okhttp.OkHttpDataSource.Factory dataSourceFactory) {
        return null;
    }
}