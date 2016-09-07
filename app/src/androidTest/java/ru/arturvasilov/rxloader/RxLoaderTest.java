package ru.arturvasilov.rxloader;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import rx.Observable;
import rx.functions.Action1;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class RxLoaderTest {

    @Test
    public void testLoader() throws Exception {
        LoaderManager loaderManager = new LoaderManager() {
            @Override
            public <D> Loader<D> initLoader(int id, Bundle args, LoaderCallbacks<D> callback) {
                return null;
            }

            @Override
            public <D> Loader<D> restartLoader(int id, Bundle args, LoaderCallbacks<D> callback) {
                return null;
            }

            @Override
            public void destroyLoader(int id) {

            }

            @Override
            public <D> Loader<D> getLoader(int id) {
                return null;
            }

            @Override
            public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {

            }
        };
        Context context = InstrumentationRegistry.getContext();

        LifecycleHandler lifecycleHandler = LoaderLifecycleHandler.create(context, loaderManager);
        Observable<Integer> observable = Observable.just(1);
        observable.compose(lifecycleHandler.<Integer>load(0))
                .compose(RxSchedulers.<Integer>async())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {

                    }
                });


    }
}