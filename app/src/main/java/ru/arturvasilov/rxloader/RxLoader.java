package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * @author Artur Vasilov
 */
public class RxLoader<D> extends Loader<D> {

    private Observable<D> mObservable;
    private Subscriber<? super D> mSubscriber;

    @Nullable
    private D mData;

    @Nullable
    private Throwable mError;

    public RxLoader(Context context, @NonNull Observable<D> observable) {
        super(context);
        mObservable = observable;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        mObservable.lift(new Observable.Operator<D, D>() {
            @Override
            public Subscriber<? super D> call(Subscriber<? super D> subscriber) {
                mSubscriber = subscriber;
                if (mData != null) {
                    mSubscriber.onNext(mData);
                } else if (mError != null) {
                    mSubscriber.onError(mError);
                }
                return subscriber;
            }
        })
                .doOnNext(new Action1<D>() {
                    @Override
                    public void call(@NonNull D d) {
                        mData = d;
                        mError = null;
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(@NonNull Throwable throwable) {
                        mError = throwable;
                        mData = null;
                    }
                });
    }

    @Override
    protected void onStopLoading() {
        if (mSubscriber != null) {
            mSubscriber.unsubscribe();
            mSubscriber = null;
        }
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        if (mSubscriber != null) {
            mSubscriber.unsubscribe();
        }
        mObservable = null;
        mData = null;
        mError = null;
        super.onReset();
    }
}
