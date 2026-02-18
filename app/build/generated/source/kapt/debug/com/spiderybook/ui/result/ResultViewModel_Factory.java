package com.spiderybook.ui.result;

import com.spiderybook.data.repository.LoadRepository;
import com.spiderybook.data.repository.LocalRepository;
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

  private final Provider<LocalRepository> localRepositoryProvider;

  public ResultViewModel_Factory(Provider<LoadRepository> loadRepositoryProvider,
      Provider<LocalRepository> localRepositoryProvider) {
    this.loadRepositoryProvider = loadRepositoryProvider;
    this.localRepositoryProvider = localRepositoryProvider;
  }

  @Override
  public ResultViewModel get() {
    return newInstance(loadRepositoryProvider.get(), localRepositoryProvider.get());
  }

  public static ResultViewModel_Factory create(Provider<LoadRepository> loadRepositoryProvider,
      Provider<LocalRepository> localRepositoryProvider) {
    return new ResultViewModel_Factory(loadRepositoryProvider, localRepositoryProvider);
  }

  public static ResultViewModel newInstance(LoadRepository loadRepository,
      LocalRepository localRepository) {
    return new ResultViewModel(loadRepository, localRepository);
  }
}
