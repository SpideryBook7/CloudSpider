package com.spiderybook.ui.common;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.spiderybook.util.Resource;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\u0010\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0005\b&\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J6\u0010\u0003\u001a\u00020\u00042\'\u0010\u0005\u001a#\b\u0001\u0012\u0004\u0012\u00020\u0007\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b\u0012\u0006\u0012\u0004\u0018\u00010\n0\u0006\u00a2\u0006\u0002\b\u000bH\u0004\u00a2\u0006\u0002\u0010\fJ2\u0010\r\u001a\u00020\t\"\u0004\b\u0000\u0010\u000e*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\u00100\u000f2\u0006\u0010\u0011\u001a\u00020\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0004J\u001e\u0010\u0015\u001a\u00020\t\"\u0004\b\u0000\u0010\u000e*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\u00100\u000fH\u0004J+\u0010\u0016\u001a\u00020\t\"\u0004\b\u0000\u0010\u000e*\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H\u000e0\u00100\u000f2\u0006\u0010\u0017\u001a\u0002H\u000eH\u0004\u00a2\u0006\u0002\u0010\u0018\u00a8\u0006\u0019"}, d2 = {"Lcom/spiderybook/ui/common/BaseViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "launchIO", "Lkotlinx/coroutines/Job;", "block", "Lkotlin/Function2;", "Lkotlinx/coroutines/CoroutineScope;", "Lkotlin/coroutines/Continuation;", "", "", "Lkotlin/ExtensionFunctionType;", "(Lkotlin/jvm/functions/Function2;)Lkotlinx/coroutines/Job;", "setError", "T", "Landroidx/lifecycle/MutableLiveData;", "Lcom/spiderybook/util/Resource;", "message", "", "exception", "", "setLoading", "setSuccess", "data", "(Landroidx/lifecycle/MutableLiveData;Ljava/lang/Object;)V", "app_debug"})
public abstract class BaseViewModel extends androidx.lifecycle.ViewModel {
    
    public BaseViewModel() {
        super();
    }
    
    protected final <T extends java.lang.Object>void setLoading(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<T>> $this$setLoading) {
    }
    
    protected final <T extends java.lang.Object>void setSuccess(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<T>> $this$setSuccess, T data) {
    }
    
    protected final <T extends java.lang.Object>void setError(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.MutableLiveData<com.spiderybook.util.Resource<T>> $this$setError, @org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.Nullable()
    java.lang.Throwable exception) {
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final kotlinx.coroutines.Job launchIO(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super kotlinx.coroutines.CoroutineScope, ? super kotlin.coroutines.Continuation<? super kotlin.Unit>, ? extends java.lang.Object> block) {
        return null;
    }
}