package ru.arturvasilov.rxloader.stubs;

import rx.Observer;

/**
 * @author Artur Vasilov
 */
public class EmptyObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {
        // Do nothing
    }

    @Override
    public void onError(Throwable e) {
        // Do nothing
    }

    @Override
    public void onNext(T t) {
        // Do nothing
    }
}
