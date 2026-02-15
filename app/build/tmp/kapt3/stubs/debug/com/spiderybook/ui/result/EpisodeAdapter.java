package com.spiderybook.ui.result;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.spiderybook.databinding.ItemEpisodeBinding;
import com.spiderybook.domain.model.Episode;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0013B\'\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tJ\b\u0010\n\u001a\u00020\u000bH\u0016J\u001c\u0010\f\u001a\u00020\b2\n\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\u000bH\u0016J\u001c\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000bH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/spiderybook/ui/result/EpisodeAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/spiderybook/ui/result/EpisodeAdapter$EpisodeViewHolder;", "items", "", "Lcom/spiderybook/domain/model/Episode;", "onClick", "Lkotlin/Function1;", "", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "EpisodeViewHolder", "app_debug"})
public final class EpisodeAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.spiderybook.ui.result.EpisodeAdapter.EpisodeViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.spiderybook.domain.model.Episode> items = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.spiderybook.domain.model.Episode, kotlin.Unit> onClick = null;
    
    public EpisodeAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.spiderybook.domain.model.Episode> items, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.spiderybook.domain.model.Episode, kotlin.Unit> onClick) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.spiderybook.ui.result.EpisodeAdapter.EpisodeViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.spiderybook.ui.result.EpisodeAdapter.EpisodeViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/spiderybook/ui/result/EpisodeAdapter$EpisodeViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/spiderybook/databinding/ItemEpisodeBinding;", "(Lcom/spiderybook/ui/result/EpisodeAdapter;Lcom/spiderybook/databinding/ItemEpisodeBinding;)V", "bind", "", "item", "Lcom/spiderybook/domain/model/Episode;", "app_debug"})
    public final class EpisodeViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.spiderybook.databinding.ItemEpisodeBinding binding = null;
        
        public EpisodeViewHolder(@org.jetbrains.annotations.NotNull()
        com.spiderybook.databinding.ItemEpisodeBinding binding) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.spiderybook.domain.model.Episode item) {
        }
    }
}