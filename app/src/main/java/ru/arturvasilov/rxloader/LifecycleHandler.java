package ru.arturvasilov.rxloader;

import android.support.annotation.NonNull;

import rx.Observable;

/**
 * @author Artur Vasilov
 */
public interface LifecycleHandler {

    @NonNull
    <T> Observable.Transformer<T, T> load(int id);

    @NonNull
    <T> Observable.Transformer<T, T> reload(int id);

    void clear(int id);
}
