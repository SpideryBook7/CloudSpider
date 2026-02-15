package com.spiderybook.plugins.providers;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AnimeFlvProvider_Factory implements Factory<AnimeFlvProvider> {
  @Override
  public AnimeFlvProvider get() {
    return newInstance();
  }

  public static AnimeFlvProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AnimeFlvProvider newInstance() {
    return new AnimeFlvProvider();
  }

  private static final class InstanceHolder {
    private static final AnimeFlvProvider_Factory INSTANCE = new AnimeFlvProvider_Factory();
  }
}
