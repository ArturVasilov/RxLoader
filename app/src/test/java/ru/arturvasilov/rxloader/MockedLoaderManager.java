package ru.arturvasilov.rxloader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Artur Vasilov
 */
@SuppressWarnings("unchecked")
public class MockedLoaderManager extends LoaderManager {

    private final Map<Integer, Loader> mLoadersMap;

    public MockedLoaderManager() {
        mLoadersMap = new HashMap<>();
    }

    @Override
    public Loader initLoader(int id, Bundle args, LoaderCallbacks callback) {
        Loader loader = mLoadersMap.get(id);
        if (loader == null) {
            loader = callback.onCreateLoader(id, args);
            mLoadersMap.put(id, loader);
        }
        loader.startLoading();
        return loader;
    }

    @Override
    public Loader restartLoader(int id, Bundle args, LoaderCallbacks callback) {
        mLoadersMap.remove(id);
        Loader loader = callback.onCreateLoader(id, args);
        mLoadersMap.put(id, loader);
        loader.startLoading();
        return loader;
    }

    @Override
    public void destroyLoader(int id) {
        mLoadersMap.remove(id);
    }

    @Override
    public <D> Loader<D> getLoader(int id) {
        return mLoadersMap.get(id);
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        // Do nothing
    }
}
