package com.spiderybook.ui.search;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import com.spiderybook.databinding.FragmentSearchBinding;
import com.spiderybook.ui.common.BaseFragment;
import com.spiderybook.ui.home.ChildItemAdapter;
import com.spiderybook.util.Resource;
import com.spiderybook.domain.model.SearchResponse;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0002!\"B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002J\u001a\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0016J\u0016\u0010\u0015\u001a\u00020\r2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017H\u0002J\b\u0010\u0019\u001a\u00020\rH\u0002J\b\u0010\u001a\u001a\u00020\rH\u0002J\b\u0010\u001b\u001a\u00020\rH\u0002J\u0016\u0010\u001c\u001a\u00020\r2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0017H\u0002J\u0010\u0010\u001e\u001a\u00020\r2\u0006\u0010\u001f\u001a\u00020 H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0006\u001a\u00020\u00078BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\n\u0010\u000b\u001a\u0004\b\b\u0010\t\u00a8\u0006#"}, d2 = {"Lcom/spiderybook/ui/search/SearchFragment;", "Lcom/spiderybook/ui/common/BaseFragment;", "Lcom/spiderybook/databinding/FragmentSearchBinding;", "()V", "searchResultsAdapter", "Lcom/spiderybook/ui/home/ChildItemAdapter;", "viewModel", "Lcom/spiderybook/ui/search/SearchViewModel;", "getViewModel", "()Lcom/spiderybook/ui/search/SearchViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "navigateToDetails", "", "item", "Lcom/spiderybook/domain/model/SearchResponse;", "onViewCreated", "view", "Landroid/view/View;", "savedInstanceState", "Landroid/os/Bundle;", "setupGenreAdapter", "genres", "", "", "setupObservers", "setupRecyclerViews", "setupSearchView", "setupTopSearchesAdapter", "items", "showDefaultView", "show", "", "GenreViewHolder", "TopSearchViewHolder", "app_debug"})
public final class SearchFragment extends com.spiderybook.ui.common.BaseFragment<com.spiderybook.databinding.FragmentSearchBinding> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.spiderybook.ui.home.ChildItemAdapter searchResultsAdapter;
    
    public SearchFragment() {
        super(null);
    }
    
    private final com.spiderybook.ui.search.SearchViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupRecyclerViews() {
    }
    
    private final void navigateToDetails(com.spiderybook.domain.model.SearchResponse item) {
    }
    
    private final void setupSearchView() {
    }
    
    private final void showDefaultView(boolean show) {
    }
    
    private final void setupObservers() {
    }
    
    private final void setupGenreAdapter(java.util.List<java.lang.String> genres) {
    }
    
    private final void setupTopSearchesAdapter(java.util.List<com.spiderybook.domain.model.SearchResponse> items) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/spiderybook/ui/search/SearchFragment$GenreViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/spiderybook/databinding/ItemGenreBinding;", "(Lcom/spiderybook/databinding/ItemGenreBinding;)V", "getBinding", "()Lcom/spiderybook/databinding/ItemGenreBinding;", "app_debug"})
    public static final class GenreViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.spiderybook.databinding.ItemGenreBinding binding = null;
        
        public GenreViewHolder(@org.jetbrains.annotations.NotNull()
        com.spiderybook.databinding.ItemGenreBinding binding) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.spiderybook.databinding.ItemGenreBinding getBinding() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/spiderybook/ui/search/SearchFragment$TopSearchViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/spiderybook/databinding/ItemTopSearchBinding;", "(Lcom/spiderybook/databinding/ItemTopSearchBinding;)V", "getBinding", "()Lcom/spiderybook/databinding/ItemTopSearchBinding;", "app_debug"})
    public static final class TopSearchViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.spiderybook.databinding.ItemTopSearchBinding binding = null;
        
        public TopSearchViewHolder(@org.jetbrains.annotations.NotNull()
        com.spiderybook.databinding.ItemTopSearchBinding binding) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.spiderybook.databinding.ItemTopSearchBinding getBinding() {
            return null;
        }
    }
}