package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import rx.AsyncEmitter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.MainThreadSubscription;
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

    /**
     * {@link LoaderLifecycleHandler#load(int)} immediately starts loading,
     * but subscription is only possible in subscribe methods
     * <p/>
     * To solve this problem this list will cache all the data arrived before the first subscriber
     */
    private final List<D> mCachedData = new ArrayList<>();

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
        if (mError != null && mEmitter != null) {
            mEmitter.onError(mError);
        } else if (mIsCompleted && mEmitter != null) {
            if (mData != null) {
                mEmitter.onNext(mData);
            }
            mEmitter.onCompleted();
        }

        mSubscription = mObservable.subscribe(new LoaderSubscriber());
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
                mEmitter.setSubscription(new MainThreadSubscription() {
                    @Override
                    protected void onUnsubscribe() {
                        clearSubscription();
                    }
                });

                if (!mCachedData.isEmpty()) {
                    for (D d : mCachedData) {
                        asyncEmitter.onNext(d);
                    }
                    mCachedData.clear();
                    if (mIsCompleted) {
                        mEmitter.onCompleted();
                    }
                } else if (mError != null) {
                    mEmitter.onError(mError);
                } else if (mIsCompleted) {
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

    private class LoaderSubscriber extends Subscriber<D> {

        @Override
        public void onNext(D d) {
            mData = d;
            if (mEmitter != null) {
                mEmitter.onNext(d);
            } else {
                mCachedData.add(d);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            mError = throwable;
            if (mEmitter != null) {
                mEmitter.onError(throwable);
            }
        }

        @Override
        public void onCompleted() {
            mIsCompleted = true;
            if (mEmitter != null) {
                mEmitter.onCompleted();
            }
        }
    }
}
