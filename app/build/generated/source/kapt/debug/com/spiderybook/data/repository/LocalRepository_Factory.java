package com.spiderybook.data.repository;

import com.spiderybook.data.local.dao.MainDao;
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
public final class LocalRepository_Factory implements Factory<LocalRepository> {
  private final Provider<MainDao> mainDaoProvider;

  public LocalRepository_Factory(Provider<MainDao> mainDaoProvider) {
    this.mainDaoProvider = mainDaoProvider;
  }

  @Override
  public LocalRepository get() {
    return newInstance(mainDaoProvider.get());
  }

  public static LocalRepository_Factory create(Provider<MainDao> mainDaoProvider) {
    return new LocalRepository_Factory(mainDaoProvider);
  }

  public static LocalRepository newInstance(MainDao mainDao) {
    return new LocalRepository(mainDao);
  }
}
