package com.spiderybook.ui.home;

import com.spiderybook.data.repository.HomeRepository;
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

  public HomeViewModel_Factory(Provider<HomeRepository> homeRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider) {
    this.homeRepositoryProvider = homeRepositoryProvider;
    this.pluginManagerProvider = pluginManagerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(homeRepositoryProvider.get(), pluginManagerProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<HomeRepository> homeRepositoryProvider,
      Provider<PluginManager> pluginManagerProvider) {
    return new HomeViewModel_Factory(homeRepositoryProvider, pluginManagerProvider);
  }

  public static HomeViewModel newInstance(HomeRepository homeRepository,
      PluginManager pluginManager) {
    return new HomeViewModel(homeRepository, pluginManager);
  }
}
