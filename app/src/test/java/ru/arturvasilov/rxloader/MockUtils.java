package ru.arturvasilov.rxloader;

import android.content.Context;
import android.support.annotation.NonNull;

import org.mockito.Mockito;

/**
 * @author Artur Vasilov
 */
public final class MockUtils {

    private MockUtils() {
    }

    @NonNull
    public static Context mockContext() {
        return Mockito.mock(Context.class);
    }

}
