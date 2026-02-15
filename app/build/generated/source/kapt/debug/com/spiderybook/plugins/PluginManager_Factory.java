package com.spiderybook.plugins;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class PluginManager_Factory implements Factory<PluginManager> {
  @Override
  public PluginManager get() {
    return newInstance();
  }

  public static PluginManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static PluginManager newInstance() {
    return new PluginManager();
  }

  private static final class InstanceHolder {
    private static final PluginManager_Factory INSTANCE = new PluginManager_Factory();
  }
}
