package com.spiderybook.ui.result;

import com.spiderybook.data.manager.AppDownloadManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ResultFragment_MembersInjector implements MembersInjector<ResultFragment> {
  private final Provider<AppDownloadManager> downloadManagerProvider;

  public ResultFragment_MembersInjector(Provider<AppDownloadManager> downloadManagerProvider) {
    this.downloadManagerProvider = downloadManagerProvider;
  }

  public static MembersInjector<ResultFragment> create(
      Provider<AppDownloadManager> downloadManagerProvider) {
    return new ResultFragment_MembersInjector(downloadManagerProvider);
  }

  @Override
  public void injectMembers(ResultFragment instance) {
    injectDownloadManager(instance, downloadManagerProvider.get());
  }

  @InjectedFieldSignature("com.spiderybook.ui.result.ResultFragment.downloadManager")
  public static void injectDownloadManager(ResultFragment instance,
      AppDownloadManager downloadManager) {
    instance.downloadManager = downloadManager;
  }
}
