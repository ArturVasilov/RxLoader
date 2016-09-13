package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.v4.app.LoaderManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

/**
 * @author Artur Vasilov
 */
@RunWith(JUnit4.class)
public class RxLoaderTest {

    private LifecycleHandler mLifecycleHandler;

    static {
        MockUtils.setupTestSchedulers();
    }

    @Before
    public void setUp() throws Exception {
        Context context = Mockito.mock(Context.class);
        LoaderManager loaderManager = new MockedLoaderManager();
        mLifecycleHandler = LoaderLifecycleHandler.create(context, loaderManager);
    }

    @Test
    public void testObservableNotExecuted() throws Exception {
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        Observable.just(1, 2, 3)
                .compose(RxSchedulers.<Integer>async(Schedulers.io(), Schedulers.io()))
                .compose(mLifecycleHandler.<Integer>load(1));

        subscriber.assertNoValues();
    }

}
