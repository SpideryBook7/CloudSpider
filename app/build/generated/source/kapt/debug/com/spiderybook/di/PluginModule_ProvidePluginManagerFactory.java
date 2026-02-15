package com.spiderybook.di;

import com.spiderybook.plugins.PluginManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class PluginModule_ProvidePluginManagerFactory implements Factory<PluginManager> {
  @Override
  public PluginManager get() {
    return providePluginManager();
  }

  public static PluginModule_ProvidePluginManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PluginManager providePluginManager() {
    return Preconditions.checkNotNullFromProvides(PluginModule.INSTANCE.providePluginManager());
  }

  private static final class InstanceHolder {
    private static final PluginModule_ProvidePluginManagerFactory INSTANCE = new PluginModule_ProvidePluginManagerFactory();
  }
}
