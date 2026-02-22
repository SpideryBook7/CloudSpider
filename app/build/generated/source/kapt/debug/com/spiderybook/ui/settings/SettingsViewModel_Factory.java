package com.spiderybook.ui.settings;

import com.spiderybook.data.local.DataStoreManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<DataStoreManager> dataStoreManagerProvider;

  private final Provider<PluginManager> pluginManagerProvider;

  public SettingsViewModel_Factory(Provider<DataStoreManager> dataStoreManagerProvider,
      Provider<PluginManager> pluginManagerProvider) {
    this.dataStoreManagerProvider = dataStoreManagerProvider;
    this.pluginManagerProvider = pluginManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(dataStoreManagerProvider.get(), pluginManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<DataStoreManager> dataStoreManagerProvider,
      Provider<PluginManager> pluginManagerProvider) {
    return new SettingsViewModel_Factory(dataStoreManagerProvider, pluginManagerProvider);
  }

  public static SettingsViewModel newInstance(DataStoreManager dataStoreManager,
      PluginManager pluginManager) {
    return new SettingsViewModel(dataStoreManager, pluginManager);
  }
}
