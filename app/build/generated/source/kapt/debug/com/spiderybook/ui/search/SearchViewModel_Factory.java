package com.spiderybook.ui.search;

import com.spiderybook.data.local.DataStoreManager;
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

  private final Provider<DataStoreManager> dataStoreManagerProvider;

  public SearchViewModel_Factory(Provider<SearchRepository> searchRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider,
      Provider<DataStoreManager> dataStoreManagerProvider) {
    this.searchRepositoryProvider = searchRepositoryProvider;
    this.pluginManagerProvider = pluginManagerProvider;
    this.dataStoreManagerProvider = dataStoreManagerProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(searchRepositoryProvider.get(), pluginManagerProvider.get(), dataStoreManagerProvider.get());
  }

  public static SearchViewModel_Factory create(Provider<SearchRepository> searchRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider,
      Provider<DataStoreManager> dataStoreManagerProvider) {
    return new SearchViewModel_Factory(searchRepositoryProvider, pluginManagerProvider, dataStoreManagerProvider);
  }

  public static SearchViewModel newInstance(SearchRepository searchRepository,
      PluginManager pluginManager, DataStoreManager dataStoreManager) {
    return new SearchViewModel(searchRepository, pluginManager, dataStoreManager);
  }
}
