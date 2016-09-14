package ru.arturvasilov.rxloader;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;

import rx.Observable;

/**
 * Interface for handling configuration changes and activity stopping
 * <p>
 * Contract of this interface:
 * <p>
 * 1) Each new subscription with load method should return the same result instantly
 * 2) #1 must be carried out event after configuration changes
 * 3) When Activity stopped request should be also stopped and restarted after activity is again visible
 * <p>
 * The only known implementation is {@link LoaderLifecycleHandler} which is base on loaders
 *
 * @author Artur Vasilov
 */
public interface LifecycleHandler {

    /**
     * Use {@link Observable#compose(Observable.Transformer)} with with method
     * That's nothing more that you'll do for most typical cases like loading single list of items
     * <p>
     * You can call this method as many times as you like, for the same id it'll return the same result
     * <p>
     * So the behaviour of this method is just the same as
     * {@link android.support.v4.app.LoaderManager#initLoader(int, Bundle, LoaderManager.LoaderCallbacks)}
     *
     * @param id - unique identifier for request on Activity / Fragment
     */
    @NonNull
    <T> Observable.Transformer<T, T> load(int id);

    /**
     * Use {@link Observable#compose(Observable.Transformer)} with with method
     * <p>
     * This method provides almost the same functionality as {@link LifecycleHandler#load(int)}
     * except it destroys previous request with the specified id and creates the new one
     * <p>
     * So the behaviour of this method is just the same as
     * {@link android.support.v4.app.LoaderManager#restartLoader(int, Bundle, LoaderManager.LoaderCallbacks)}
     *
     * @param id - unique identifier for request on Activity / Fragment
     */
    @NonNull
    <T> Observable.Transformer<T, T> reload(int id);

    /**
     * This method clears subscriptions and destroys observable for the request with specified id
     *
     * @param id - unique identifier for request on Activity / Fragment
     */
    void clear(int id);
}
