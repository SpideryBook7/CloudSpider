package com.spiderybook.ui.player;

import com.spiderybook.data.repository.LocalRepository;
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
public final class PlayerActivity_MembersInjector implements MembersInjector<PlayerActivity> {
  private final Provider<LocalRepository> localRepositoryProvider;

  public PlayerActivity_MembersInjector(Provider<LocalRepository> localRepositoryProvider) {
    this.localRepositoryProvider = localRepositoryProvider;
  }

  public static MembersInjector<PlayerActivity> create(
      Provider<LocalRepository> localRepositoryProvider) {
    return new PlayerActivity_MembersInjector(localRepositoryProvider);
  }

  @Override
  public void injectMembers(PlayerActivity instance) {
    injectLocalRepository(instance, localRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.spiderybook.ui.player.PlayerActivity.localRepository")
  public static void injectLocalRepository(PlayerActivity instance,
      LocalRepository localRepository) {
    instance.localRepository = localRepository;
  }
}
