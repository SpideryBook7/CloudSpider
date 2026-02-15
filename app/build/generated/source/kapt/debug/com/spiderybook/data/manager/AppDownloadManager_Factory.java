package com.spiderybook.data.manager;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppDownloadManager_Factory implements Factory<AppDownloadManager> {
  private final Provider<Context> contextProvider;

  public AppDownloadManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppDownloadManager get() {
    return newInstance(contextProvider.get());
  }

  public static AppDownloadManager_Factory create(Provider<Context> contextProvider) {
    return new AppDownloadManager_Factory(contextProvider);
  }

  public static AppDownloadManager newInstance(Context context) {
    return new AppDownloadManager(context);
  }
}
