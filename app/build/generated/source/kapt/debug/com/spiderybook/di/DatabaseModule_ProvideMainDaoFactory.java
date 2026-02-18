package com.spiderybook.di;

import com.spiderybook.data.local.AppDatabase;
import com.spiderybook.data.local.dao.MainDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideMainDaoFactory implements Factory<MainDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideMainDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MainDao get() {
    return provideMainDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideMainDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideMainDaoFactory(dbProvider);
  }

  public static MainDao provideMainDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMainDao(db));
  }
}
