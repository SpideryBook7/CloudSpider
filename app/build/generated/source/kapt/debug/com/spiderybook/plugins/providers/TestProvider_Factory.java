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
public final class TestProvider_Factory implements Factory<TestProvider> {
  @Override
  public TestProvider get() {
    return newInstance();
  }

  public static TestProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TestProvider newInstance() {
    return new TestProvider();
  }

  private static final class InstanceHolder {
    private static final TestProvider_Factory INSTANCE = new TestProvider_Factory();
  }
}
