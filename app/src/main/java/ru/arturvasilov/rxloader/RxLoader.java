package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import rx.AsyncEmitter;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * @author Artur Vasilov
 */
public class RxLoader<D> extends Loader<D> {

    private Observable<D> mObservable;

    private AsyncEmitter<D> mEmitter;

    private Subscription mSubscription;

    @Nullable
    private D mData;

    @Nullable
    private Throwable mError;

    private boolean mIsCompleted = false;

    public RxLoader(@NonNull Context context, @NonNull Observable<D> observable) {
        super(context);
        mObservable = observable;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        mSubscription = mObservable.subscribe(new Action1<D>() {
            @Override
            public void call(D d) {
                mData = d;
                if (mEmitter != null) {
                    mEmitter.onNext(d);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mError = throwable;
                if (mEmitter != null) {
                    mEmitter.onError(throwable);
                }
            }
        }, new Action0() {
            @Override
            public void call() {
                mIsCompleted = true;
                if (mEmitter != null) {
                    mEmitter.onCompleted();
                }
            }
        });
    }

    @Override
    protected void onStopLoading() {
        clearSubscription();
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        clearSubscription();
        mObservable = null;
        mData = null;
        mError = null;
        super.onReset();
    }

    @NonNull
    public Observable<D> createObservable() {
        return Observable.fromEmitter(new Action1<AsyncEmitter<D>>() {
            @Override
            public void call(AsyncEmitter<D> asyncEmitter) {
                mEmitter = asyncEmitter;
                if (mData != null) {
                    mEmitter.onNext(mData);
                } else if (mError != null) {
                    mEmitter.onError(mError);
                }
                if (mIsCompleted) {
                    mEmitter.onCompleted();
                }
            }
        }, AsyncEmitter.BackpressureMode.LATEST);
    }

    private void clearSubscription() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
            mEmitter = null;
        }
    }
}
