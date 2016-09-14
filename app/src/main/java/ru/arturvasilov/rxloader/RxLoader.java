package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import rx.AsyncEmitter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.MainThreadSubscription;
import rx.functions.Action1;

/**
 * @author Artur Vasilov
 */
class RxLoader<D> extends Loader<D> {

    private Observable<D> mObservable;

    private AsyncEmitter<D> mEmitter;

    private Subscription mSubscription;

    @Nullable
    private D mData;

    private boolean mIsErrorReported = false;

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
        subscribe();
    }

    @Override
    protected void onStopLoading() {
        /**
         * TODO : allow configure clearing subscription and caching policy in release 0.2.0
         */
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        clearSubscription();
        mObservable = null;
        mData = null;
        mError = null;
        mIsCompleted = false;
        mIsErrorReported = false;
        mEmitter = null;
        super.onReset();
    }

    private void subscribe() {
        if (mEmitter != null && mSubscription == null && !mIsCompleted && mError == null) {
            mSubscription = mObservable.subscribe(new LoaderSubscriber());
        }
    }

    @NonNull
    Observable<D> createObservable() {
        return Observable.fromEmitter(new Action1<AsyncEmitter<D>>() {
            @Override
            public void call(AsyncEmitter<D> asyncEmitter) {
                mEmitter = asyncEmitter;
                mEmitter.setSubscription(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        clearSubscription();
                    }
                });

                /**
                 * TODO : fix in 0.2.0
                 *
                 * here is possible data loosing if Observable emits items too quickly
                 * since we cache only last result, previous items could be lost during rotation
                 */
                if (mData != null) {
                    mEmitter.onNext(mData);
                }
                if (mIsCompleted) {
                    mEmitter.onCompleted();
                } else if (mError != null && !mIsErrorReported) {
                    mEmitter.onError(mError);
                    mIsErrorReported = true;
                }

                subscribe();
            }
        }, AsyncEmitter.BackpressureMode.LATEST);
    }

    private void clearSubscription() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    private class LoaderSubscriber extends Subscriber<D> {

        @Override
        public void onNext(D d) {
            mData = d;
            if (mEmitter != null && isStarted()) {
                mEmitter.onNext(d);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            mError = throwable;
            if (mEmitter != null && isStarted()) {
                mEmitter.onError(throwable);
                mIsErrorReported = true;
            }
        }

        @Override
        public void onCompleted() {
            mIsCompleted = true;
            if (mEmitter != null && isStarted()) {
                mEmitter.onCompleted();
            }
        }
    }
}