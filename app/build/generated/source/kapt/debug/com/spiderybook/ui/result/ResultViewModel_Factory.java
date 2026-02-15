package com.spiderybook.ui.result;

import com.spiderybook.data.repository.LoadRepository;
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
public final class ResultViewModel_Factory implements Factory<ResultViewModel> {
  private final Provider<LoadRepository> loadRepositoryProvider;

  public ResultViewModel_Factory(Provider<LoadRepository> loadRepositoryProvider) {
    this.loadRepositoryProvider = loadRepositoryProvider;
  }

  @Override
  public ResultViewModel get() {
    return newInstance(loadRepositoryProvider.get());
  }

  public static ResultViewModel_Factory create(Provider<LoadRepository> loadRepositoryProvider) {
    return new ResultViewModel_Factory(loadRepositoryProvider);
  }

  public static ResultViewModel newInstance(LoadRepository loadRepository) {
    return new ResultViewModel(loadRepository);
  }
}
