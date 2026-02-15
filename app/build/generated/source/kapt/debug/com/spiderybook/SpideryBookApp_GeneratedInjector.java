package com.spiderybook;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = SpideryBookApp.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface SpideryBookApp_GeneratedInjector {
  void injectSpideryBookApp(SpideryBookApp spideryBookApp);
}
