package com.spiderybook.ui.home;

import com.spiderybook.data.local.DataStoreManager;
import com.spiderybook.data.repository.HomeRepository;
import com.spiderybook.data.repository.LocalRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<HomeRepository> homeRepositoryProvider;

  private final Provider<PluginManager> pluginManagerProvider;

  private final Provider<DataStoreManager> dataStoreManagerProvider;

  private final Provider<LocalRepository> localRepositoryProvider;

  public HomeViewModel_Factory(Provider<HomeRepository> homeRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider,
      Provider<DataStoreManager> dataStoreManagerProvider,
      Provider<LocalRepository> localRepositoryProvider) {
    this.homeRepositoryProvider = homeRepositoryProvider;
    this.pluginManagerProvider = pluginManagerProvider;
    this.dataStoreManagerProvider = dataStoreManagerProvider;
    this.localRepositoryProvider = localRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(homeRepositoryProvider.get(), pluginManagerProvider.get(), dataStoreManagerProvider.get(), localRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<HomeRepository> homeRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider,
      Provider<DataStoreManager> dataStoreManagerProvider,
      Provider<LocalRepository> localRepositoryProvider) {
    return new HomeViewModel_Factory(homeRepositoryProvider, pluginManagerProvider, dataStoreManagerProvider, localRepositoryProvider);
  }

  public static HomeViewModel newInstance(HomeRepository homeRepository,
      PluginManager pluginManager, DataStoreManager dataStoreManager,
      LocalRepository localRepository) {
    return new HomeViewModel(homeRepository, pluginManager, dataStoreManager, localRepository);
  }
}
