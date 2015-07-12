package droidkit.sqlite;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Daniel Serdyukov
 */
class SQLiteOnSubscribe<T> implements Observable.OnSubscribe<List<T>> {

    private final Reference<LoaderManager> mLmRef;

    private final Loader<List<T>> mLoader;

    private final int mLoaderId;

    public SQLiteOnSubscribe(@NonNull LoaderManager lm, @NonNull Loader<List<T>> loader, int loaderId) {
        mLmRef = new WeakReference<>(lm);
        mLoader = loader;
        mLoaderId = loaderId;
    }

    @Override
    public void call(Subscriber<? super List<T>> subscriber) {
        final LoaderManager lm = mLmRef.get();
        if (lm != null) {
            lm.initLoader(mLoaderId, Bundle.EMPTY, new SQLiteLoaderCallbacks<>(subscriber, mLoader));
        }
    }

}
