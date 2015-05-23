package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
class AndroidSQLiteClient extends SQLiteOpenHelper implements SQLiteClient {

    private final Context mContext;

    public AndroidSQLiteClient(@NonNull Context context) {
        super(context, SQLite.DATABASE_NAME.get(), null, SQLite.DATABASE_VERSION.get());
        mContext = context;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void beginTransaction() {
        final SQLiteDatabase db = getWritableDatabase();
        if (!db.inTransaction()) {
            db.beginTransactionNonExclusive();
        }
    }

    @Override
    public void endTransaction(boolean successful) {
        final SQLiteDatabase db = getWritableDatabase();
        if (db.inTransaction()) {
            if (successful) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
        }
    }

    @NonNull
    @Override
    public Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs) {
        return getReadableDatabase().rawQuery(sql, bindArgs);
    }

    @Nullable
    @Override
    public String simpleQueryForString(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                DatabaseUtils.bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.simpleQueryForString();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    @Override
    public long simpleQueryForLong(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                DatabaseUtils.bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.simpleQueryForLong();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    @Override
    public void execSQL(@NonNull String sql, @NonNull Object... bindArgs) {
        getWritableDatabase().execSQL(sql, bindArgs);
    }

    @Override
    public int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                DatabaseUtils.bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.executeUpdateDelete();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    //region SQLiteOpenHelper implementation
    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        for (final String pragma : SQLite.PRAGMA) {
            db.execSQL(pragma);
        }
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        for (final String create : SQLite.CREATE) {
            db.execSQL(create);
        }
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        if (SQLite.UPGRADE.isEmpty()) {
            final Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master" +
                    " WHERE type='table'" +
                    " AND name <> 'android_metadata'", null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(0) + ";");
                    } while (cursor.moveToNext());
                }
            } finally {
                IOUtils.closeQuietly(cursor);
            }
            onCreate(db);
        } else {
            for (final String upgrade : SQLite.UPGRADE) {
                db.execSQL(upgrade);
            }
        }
    }
    //endregion

}
