# SQLite [![Apache License](https://img.shields.io/badge/license-Apache%20v2-blue.svg)](https://github.com/ArturVasilov/RxLoader/blob/master/LICENSE) [![Build Status](https://travis-ci.org/ArturVasilov/RxLoader.png?branch=master)](https://github.com/ArturVasilov/RxLoader)

### RxLoader

Library for handling lifecycle events with Observables and loader in single line!

### Gradle

```groovy
compile 'ru.arturvasilov:rx-loader:0.1.1'
```

### RxLoader

In your Activity / Presenter make a request with RxJava as you're usually doing:

```java
@Override
protected void onStart() {
    super.onStart();
    Observable<Integer> observable = Repository.provideSomeService()
            .someMethod()
            .compose(RxSchedulers.async())
            .subscribe(value -> Log.i("RxLoader", String.valueOf(value)));
```

Now, if you want to handle configuration changes and activity pauses, you can achieve this with LifecycleHandler class like this:

```java
@Override
protected void onStart() {
    super.onStart();
    LifecycleHandler handler = LoaderLifecycleHandler.create(this, getSupportLoaderManager());
    
    Observable<Integer> observable = Repository.provideSomeService()
            .someMethod()
            .compose(RxSchedulers.async())
            .compose(handler.load(R.id.some_request))
            .subscribe(value -> Log.i("RxLoader", String.valueOf(value)));
    }
```

And that's it! 

When your device will be rotated you will get the last value from onNext and you observable will continue it's work.

When you leave the screen or press home button, observable will be automatically destroyed. 

### LifecycleHandler

Everything is achieved just with interface LifecycleHandler:

```java
public interface LifecycleHandler {

    @NonNull
    <T> Observable.Transformer<T, T> load(int id);

    @NonNull
    <T> Observable.Transformer<T, T> reload(int id);

    void clear(int id);
}
```

Load methods executes one observable only once and it doesn't matter how many times you're subscribing to it. 

If you want to restart execution, you may use reload method.

If you want to clear everything data from handler you may use clear method.

### Under the hood

It's great to handle configuration changes each with one line of code, but how it works?

In fact, when you call load or reload methods from LifecycleHandler, you will get only a facade for the original observable.
Original observable and some of it's input is stored in loaders (which makes them independent for configuration changes). 
So, original observable is executed in loader and you're get its' results in the observable. So you can call subscribe multiple times.
