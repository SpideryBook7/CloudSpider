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
public final class SearchRepository_Factory implements Factory<SearchRepository> {
  private final Provider<PluginManager> pluginManagerProvider;

  public SearchRepository_Factory(Provider<PluginManager> pluginManagerProvider) {
    this.pluginManagerProvider = pluginManagerProvider;
  }

  @Override
  public SearchRepository get() {
    return newInstance(pluginManagerProvider.get());
  }

  public static SearchRepository_Factory create(Provider<PluginManager> pluginManagerProvider) {
    return new SearchRepository_Factory(pluginManagerProvider);
  }

  public static SearchRepository newInstance(PluginManager pluginManager) {
    return new SearchRepository(pluginManager);
  }
}
