package com.spiderybook.data.repository;

import com.spiderybook.plugins.PluginManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class HomeRepository_Factory implements Factory<HomeRepository> {
  private final Provider<PluginManager> pluginManagerProvider;

  public HomeRepository_Factory(Provider<PluginManager> pluginManagerProvider) {
    this.pluginManagerProvider = pluginManagerProvider;
  }

  @Override
  public HomeRepository get() {
    return newInstance(pluginManagerProvider.get());
  }

  public static HomeRepository_Factory create(Provider<PluginManager> pluginManagerProvider) {
    return new HomeRepository_Factory(pluginManagerProvider);
  }

  public static HomeRepository newInstance(PluginManager pluginManager) {
    return new HomeRepository(pluginManager);
  }
}
