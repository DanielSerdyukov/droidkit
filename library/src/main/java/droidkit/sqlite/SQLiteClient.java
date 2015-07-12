package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.io.IOUtils;
import droidkit.util.Lists;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteClient {

    private final ConcurrentMap<String, SQLiteStatement> mStmtCache = new ConcurrentHashMap<>();

    public void beginTransaction() {
        final SQLiteDatabase db = getWritableDatabase();
        if (!db.inTransaction()) {
            db.beginTransaction();
        }
    }

    public void endTransaction(boolean successful) {
        final SQLiteDatabase db = getWritableDatabase();
        if (db.inTransaction()) {
            db.endTransaction(successful);
            for (final SQLiteStatement stmt : mStmtCache.values()) {
                IOUtils.closeQuietly(stmt);
            }
            mStmtCache.clear();
        }
    }

    public void execute(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getWritableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            stmt.execute();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public long executeInsert(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getWritableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            return stmt.executeInsert();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getWritableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            return stmt.executeUpdateDelete();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @NonNull
    public Cursor query(@NonNull String sql, @NonNull Object... bindArgs) {
        return getReadableDatabase().rawQuery(sql, Lists.transform(Arrays.asList(bindArgs),
                new Func1<Object, String>() {
                    @NonNull
                    @Override
                    public String call(@NonNull Object o) {
                        return String.valueOf(o);
                    }
                }).toArray(new String[bindArgs.length]));
    }

    @NonNull
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public String queryForString(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getReadableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            return stmt.simpleQueryForString();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    protected abstract SQLiteDatabase getReadableDatabase();

    protected abstract SQLiteDatabase getWritableDatabase();

    protected abstract void shutdown();

    @NonNull
    private SQLiteStatement compileStatement(@NonNull SQLiteDatabase db, @NonNull String sql) {
        if (db.inTransaction()) {
            SQLiteStatement stmt = mStmtCache.get(sql);
            if (stmt == null) {
                final SQLiteStatement newStmt = db.compileStatement(sql);
                stmt = mStmtCache.putIfAbsent(sql, newStmt);
                if (stmt == null) {
                    stmt = newStmt;
                } else {
                    IOUtils.closeQuietly(stmt);
                }
            }
            return stmt;
        }
        return db.compileStatement(sql);
    }

    interface Callbacks {

        void onDatabaseConfigure(@NonNull SQLiteDatabase db);

        void onDatabaseCreate(@NonNull SQLiteDatabase db);

        void onDatabaseUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion);

    }

}
