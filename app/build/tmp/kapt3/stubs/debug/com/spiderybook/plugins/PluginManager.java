package com.spiderybook.plugins;

import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\n\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0005R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u00078F\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\t\u00a8\u0006\u0010"}, d2 = {"Lcom/spiderybook/plugins/PluginManager;", "", "()V", "_apis", "", "Lcom/spiderybook/plugins/MainAPI;", "apis", "", "getApis", "()Ljava/util/List;", "getAPI", "name", "", "register", "", "api", "app_debug"})
public final class PluginManager {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.spiderybook.plugins.MainAPI> _apis = null;
    
    @javax.inject.Inject()
    public PluginManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.spiderybook.plugins.MainAPI> getApis() {
        return null;
    }
    
    public final void register(@org.jetbrains.annotations.NotNull()
    com.spiderybook.plugins.MainAPI api) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.spiderybook.plugins.MainAPI getAPI(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
        return null;
    }
}