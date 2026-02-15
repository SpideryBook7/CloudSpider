package com.spiderybook.data.local;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.Preferences;
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
public final class DataStoreManager_Factory implements Factory<DataStoreManager> {
  private final Provider<DataStore<Preferences>> dataStoreProvider;

  public DataStoreManager_Factory(Provider<DataStore<Preferences>> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public DataStoreManager get() {
    return newInstance(dataStoreProvider.get());
  }

  public static DataStoreManager_Factory create(
      Provider<DataStore<Preferences>> dataStoreProvider) {
    return new DataStoreManager_Factory(dataStoreProvider);
  }

  public static DataStoreManager newInstance(DataStore<Preferences> dataStore) {
    return new DataStoreManager(dataStore);
  }
}
