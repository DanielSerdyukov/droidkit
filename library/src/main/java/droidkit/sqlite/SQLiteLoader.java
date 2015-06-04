package droidkit.sqlite;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.support.annotation.NonNull;

import droidkit.concurrent.MainQueue;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoader<T> extends AsyncTaskLoader<SQLiteResult<T>> {

    private final ContentObserver mObserver =
            new ContentObserver(MainQueue.getHandler()) {
                @Override
                public boolean deliverSelfNotifications() {
                    return true;
                }

                @Override
                public void onChange(boolean selfChange) {
                    onContentChanged();
                }
            };

    private final SQLiteQuery<T> mQuery;

    private SQLiteResult<T> mResult;

    public SQLiteLoader(@NonNull Context context, @NonNull Class<T> type) {
        this(context, SQLite.of(context).where(type));
    }

    public SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query) {
        super(context);
        mQuery = query;
    }

    @Override
    public SQLiteResult<T> loadInBackground() {
        final SQLiteResult<T> result = mQuery.result();
        getContext().getContentResolver().registerContentObserver(SQLite.uriOf(mQuery.getType()), true, mObserver);
        return result;
    }

    @Override
    public void deliverResult(SQLiteResult<T> result) {
        if (isReset()) {
            if (result != null) {
                IOUtils.closeQuietly(result);
            }
            return;
        }
        final SQLiteResult<T> oldResult = mResult;
        mResult = result;
        if (isStarted()) {
            super.deliverResult(result);
        }
        if (oldResult != null && oldResult != result) {
            IOUtils.closeQuietly(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        getContext().getContentResolver().unregisterContentObserver(mObserver);
        if (mResult != null) {
            IOUtils.closeQuietly(mResult);
        }
        mResult = null;
    }

}
