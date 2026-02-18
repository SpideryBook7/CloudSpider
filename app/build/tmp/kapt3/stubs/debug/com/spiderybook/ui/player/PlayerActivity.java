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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u000eH\u0002J\b\u0010\u0018\u001a\u00020\u0019H\u0002J\u001a\u0010\u001a\u001a\u00020\u00192\u0006\u0010\u001b\u001a\u00020\u00162\b\u0010\u001c\u001a\u0004\u0018\u00010\u0016H\u0003J\u0012\u0010\u001d\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\b\u0010 \u001a\u00020\u0019H\u0014J\b\u0010!\u001a\u00020\u0019H\u0002J\b\u0010\"\u001a\u00020\u0019H\u0002J\u0016\u0010#\u001a\u00020\u00192\f\u0010$\u001a\b\u0012\u0004\u0012\u00020&0%H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0005\u001a\u00020\u00068\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\'"}, d2 = {"Lcom/spiderybook/ui/player/PlayerActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/spiderybook/databinding/ActivityPlayerBinding;", "localRepository", "Lcom/spiderybook/data/repository/LocalRepository;", "getLocalRepository", "()Lcom/spiderybook/data/repository/LocalRepository;", "setLocalRepository", "(Lcom/spiderybook/data/repository/LocalRepository;)V", "player", "Landroidx/media3/exoplayer/ExoPlayer;", "startPosition", "", "viewModel", "Lcom/spiderybook/ui/player/PlayerViewModel;", "getViewModel", "()Lcom/spiderybook/ui/player/PlayerViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "generatedTime", "", "ms", "hideSystemUi", "", "initializePlayer", "url", "referer", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onStop", "releasePlayer", "setupObservers", "showSourceSelectionDialog", "links", "", "Lcom/spiderybook/plugins/MainAPI$ExtractorLink;", "app_debug"})
public final class PlayerActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.spiderybook.databinding.ActivityPlayerBinding binding;
    @org.jetbrains.annotations.Nullable()
    private androidx.media3.exoplayer.ExoPlayer player;
    private long startPosition = 0L;
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
}