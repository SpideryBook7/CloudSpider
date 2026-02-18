package com.spiderybook.ui.search;

import com.spiderybook.data.repository.SearchRepository;
import com.spiderybook.plugins.PluginManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<SearchRepository> searchRepositoryProvider;

  private final Provider<PluginManager> pluginManagerProvider;

  public SearchViewModel_Factory(Provider<SearchRepository> searchRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider) {
    this.searchRepositoryProvider = searchRepositoryProvider;
    this.pluginManagerProvider = pluginManagerProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(searchRepositoryProvider.get(), pluginManagerProvider.get());
  }

  public static SearchViewModel_Factory create(Provider<SearchRepository> searchRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider) {
    return new SearchViewModel_Factory(searchRepositoryProvider, pluginManagerProvider);
  }

  public static SearchViewModel newInstance(SearchRepository searchRepository,
      PluginManager pluginManager) {
    return new SearchViewModel(searchRepository, pluginManager);
  }
}
