package ru.arturvasilov.rxloader.stubs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Action1;

import static org.mockito.Matchers.any;

/**
 * @author Artur Vasilov
 */
@RunWith(JUnit4.class)
public class StubActionsTest {

    @Test
    public void testStubActionsCalled() throws Exception {
        Action1<Integer> onNextAction = Mockito.spy(new StubAction<Integer>());
        Action0 onCompleteAction = Mockito.spy(new StubAction0());
        Observable.just(1)
                .subscribe(onNextAction, new StubAction<Throwable>(), onCompleteAction);

        Mockito.verify(onNextAction).call(1);
        Mockito.verify(onCompleteAction).call();
    }

    @Test
    public void testNoErrorWithStubAction() throws Exception {
        Action1<Throwable> errorAction = Mockito.spy(new StubAction<Throwable>());
        Observable.error(new IOException())
                .subscribe(new StubAction<>(), errorAction, new StubAction0());

        Mockito.verify(errorAction).call(any(IOException.class));
    }

    @Test
    public void testEmptyObserverMethodsCalled() throws Exception {
        Observer<Integer> emptyObserver = Mockito.spy(new EmptyObserver<Integer>());
        Observable.just(176).subscribe(emptyObserver);

        Mockito.verify(emptyObserver).onNext(176);
        Mockito.verify(emptyObserver).onCompleted();
    }

    @Test
    public void testNoErrorsWithEmptyObserver() throws Exception {
        Observer<Object> emptyObserver = Mockito.spy(new EmptyObserver<>());
        Observable.error(new IOException()).subscribe(emptyObserver);

        Mockito.verify(emptyObserver).onError(any(IOException.class));
    }
}
