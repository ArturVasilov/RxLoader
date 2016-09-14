package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.v4.app.LoaderManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import rx.Observable;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;

/**
 * @author Artur Vasilov
 */
@Config(sdk = 21, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class RxLoaderTest {

    @Rule
    public RxSchedulersTestRule mRule = new RxSchedulersTestRule();

    private LifecycleHandler mLifecycleHandler;

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
                .compose(RxUtils.<Integer>async(Schedulers.io(), Schedulers.io()))
                .compose(mLifecycleHandler.<Integer>load(1));

        subscriber.assertNoValues();
    }

    @Test
    public void testObservableLoaded() throws Exception {
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        Observable.just(1)
                .compose(RxUtils.<Integer>async())
                .compose(mLifecycleHandler.<Integer>load(2))
                .subscribe(subscriber);

        subscriber.assertValue(1);
        subscriber.assertCompleted();
    }

    @Test
    public void testObservableError() throws Exception {
        TestSubscriber<Object> subscriber = new TestSubscriber<>();
        Observable.error(new IOException())
                .compose(RxUtils.async())
                .compose(mLifecycleHandler.load(3))
                .subscribe(subscriber);

        subscriber.assertError(IOException.class);
    }

    @Test
    public void testMultipleSubscriptionsReceiveSameResult() throws Exception {
        Observable<String> observable = Observable.just("1");

        Action1<String> testAction = new Action1<String>() {
            @Override
            public void call(String s) {
                assertEquals("1", s);
            }
        };

        observable
                .compose(mLifecycleHandler.<String>load(4))
                .subscribe(testAction);

        observable
                .compose(mLifecycleHandler.<String>load(4))
                .subscribe(testAction);
    }

    @Test
    public void testSecondSubscriptionReceiveLastResult() throws Exception {
        Observable<Integer> observable = Observable.just(6, 8, 10);

        TestSubscriber<Integer> testSubscriber1 = new TestSubscriber<>();
        TestSubscriber<Integer> testSubscriber2 = new TestSubscriber<>();

        observable
                .compose(mLifecycleHandler.<Integer>load(5))
                .subscribe(testSubscriber1);

        observable
                .compose(mLifecycleHandler.<Integer>load(5))
                .subscribe(testSubscriber2);

        testSubscriber1.assertValues(6, 8, 10);
        testSubscriber1.assertCompleted();

        testSubscriber2.assertValue(10);
        testSubscriber2.assertCompleted();
    }

    @Test
    public void testSecondSubscriptionDoesNotReceiveError() throws Exception {
        Observable<Object> resultObservable = Observable.concat(Observable.just(2, 3), Observable.error(new Exception()));

        TestSubscriber<Object> testSubscriber1 = new TestSubscriber<>();
        TestSubscriber<Object> testSubscriber2 = new TestSubscriber<>();

        resultObservable
                .compose(mLifecycleHandler.load(6))
                .subscribe(testSubscriber1);

        resultObservable
                .compose(mLifecycleHandler.load(6))
                .subscribe(testSubscriber2);

        testSubscriber1.assertValues(2, 3);
        testSubscriber1.assertError(Exception.class);

        testSubscriber2.assertValue(3);
        testSubscriber2.assertNotCompleted();
        testSubscriber2.assertNoErrors();
    }

    @Test
    public void testRestartCreatesNewSubscription() throws Exception {
        Observable<Integer> observable = Observable.just(6, 8, 10);

        TestSubscriber<Integer> testSubscriber1 = new TestSubscriber<>();
        TestSubscriber<Integer> testSubscriber2 = new TestSubscriber<>();

        observable
                .compose(mLifecycleHandler.<Integer>load(7))
                .subscribe(testSubscriber1);
        testSubscriber1.assertValues(6, 8, 10);
        testSubscriber1.assertCompleted();

        observable
                .compose(mLifecycleHandler.<Integer>reload(7))
                .subscribe(testSubscriber2);

        testSubscriber2.assertValues(6, 8, 10);
        testSubscriber2.assertCompleted();
    }

    @Test
    public void testClearAndLoad() throws Exception {
        Observable<Integer> observable = Observable.just(1, 2);

        TestSubscriber<Integer> testSubscriber1 = new TestSubscriber<>();
        TestSubscriber<Integer> testSubscriber2 = new TestSubscriber<>();

        observable
                .compose(mLifecycleHandler.<Integer>load(8))
                .subscribe(testSubscriber1);

        testSubscriber1.assertValues(1, 2);
        testSubscriber1.assertCompleted();

        mLifecycleHandler.clear(8);
        observable
                .compose(mLifecycleHandler.<Integer>load(8))
                .subscribe(testSubscriber2);

        testSubscriber2.assertValues(1, 2);
        testSubscriber2.assertCompleted();
    }

}
