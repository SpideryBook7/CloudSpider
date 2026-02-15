package com.spiderybook;

import android.app.Application;
import android.content.Context;
import coil3.ImageLoader;
import coil3.SingletonImageLoader;
import coil3.PlatformContext;
import dagger.hilt.android.HiltAndroidApp;
import java.lang.ref.WeakReference;

@dagger.hilt.android.HiltAndroidApp()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \t2\u00020\u0001:\u0001\tB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0015\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004H\u0016\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0007\u001a\u00020\bH\u0016\u00a8\u0006\n"}, d2 = {"Lcom/spiderybook/SpideryBookApp;", "Landroid/app/Application;", "()V", "newImageLoader", "error/NonExistentClass", "context", "(Lerror/NonExistentClass;)Lerror/NonExistentClass;", "onCreate", "", "Companion", "app_debug"})
public final class SpideryBookApp extends android.app.Application implements SingletonImageLoader.Factory {
    @org.jetbrains.annotations.Nullable()
    private static java.lang.ref.WeakReference<com.spiderybook.SpideryBookApp> _instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.spiderybook.SpideryBookApp.Companion Companion = null;
    
    public SpideryBookApp() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public ImageLoader newImageLoader(@org.jetbrains.annotations.NotNull()
    PlatformContext context) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0016\u0010\u0003\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u00078F\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0013\u0010\n\u001a\u0004\u0018\u00010\u00058F\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\r"}, d2 = {"Lcom/spiderybook/SpideryBookApp$Companion;", "", "()V", "_instance", "Ljava/lang/ref/WeakReference;", "Lcom/spiderybook/SpideryBookApp;", "context", "Landroid/content/Context;", "getContext", "()Landroid/content/Context;", "instance", "getInstance", "()Lcom/spiderybook/SpideryBookApp;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final com.spiderybook.SpideryBookApp getInstance() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final android.content.Context getContext() {
            return null;
        }
    }
}