package com.spiderybook.ui.browse;

import com.spiderybook.plugins.providers.AnimeFlvProvider;
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
public final class BrowseViewModel_Factory implements Factory<BrowseViewModel> {
  private final Provider<AnimeFlvProvider> providerProvider;

  public BrowseViewModel_Factory(Provider<AnimeFlvProvider> providerProvider) {
    this.providerProvider = providerProvider;
  }

  @Override
  public BrowseViewModel get() {
    return newInstance(providerProvider.get());
  }

  public static BrowseViewModel_Factory create(Provider<AnimeFlvProvider> providerProvider) {
    return new BrowseViewModel_Factory(providerProvider);
  }

  public static BrowseViewModel newInstance(AnimeFlvProvider provider) {
    return new BrowseViewModel(provider);
  }
}
