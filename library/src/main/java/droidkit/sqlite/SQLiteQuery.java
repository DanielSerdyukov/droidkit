package droidkit.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQuery<T> {

    //region operators and values
    private static final String EQ = " = ?";

    private static final String NOT_EQ = " <> ?";

    private static final String LT = " < ?";

    private static final String LT_OR_EQ = " <= ?";

    private static final String GT = " > ?";

    private static final String GT_OR_EQ = " >= ?";

    private static final String LIKE = " LIKE ?";

    private static final String BETWEEN = " BETWEEN ? AND ?";

    private static final int TRUE = 1;

    private static final int FALSE = 0;

    private static final String IS_NULL = " IS NULL";

    private static final String NOT_NULL = " NOT NULL";

    private static final String COMMA = ", ";

    private static final String ASC = " ASC";

    private static final String DESC = " DESC";

    private static final String AND = " AND ";

    private static final String OR = " OR ";
    //endregion

    private final WeakReference<SQLiteClient> mClient;

    private final Class<T> mType;

    private final List<Object> mBindArgs = new ArrayList<>();

    private final List<String> mOrderBy = new ArrayList<>();

    private StringBuilder mWhere;

    private boolean mDistinct;

    private String mGroupBy;

    private String mHaving;

    private String mLimit;

    public SQLiteQuery(@NonNull SQLiteClient client, @NonNull Class<T> type) {
        mClient = new WeakReference<>(client);
        mType = type;
    }

    //region where conditions
    @NonNull
    public SQLiteQuery<T> distinct() {
        mDistinct = true;
        return this;
    }

    @NonNull
    public SQLiteQuery<T> equalTo(@NonNull String column, @NonNull Object value) {
        return where(column, EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> notEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, NOT_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThan(@NonNull String column, @NonNull Object value) {
        return where(column, LT, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, LT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThan(@NonNull String column, @NonNull Object value) {
        return where(column, GT, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, GT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> like(@NonNull String column, @NonNull Object value) {
        return where(column, LIKE, value);
    }

    @NonNull
    public SQLiteQuery<T> between(@NonNull String column, @NonNull Object lv, @NonNull Object rv) {
        return where(column, BETWEEN, lv, rv);
    }

    @NonNull
    public SQLiteQuery<T> isTrue(@NonNull String column) {
        return equalTo(column, TRUE);
    }

    @NonNull
    public SQLiteQuery<T> isFalse(@NonNull String column) {
        return equalTo(column, FALSE);
    }

    @NonNull
    public SQLiteQuery<T> isNull(@NonNull String column) {
        return where(column, IS_NULL);
    }

    @NonNull
    public SQLiteQuery<T> notNull(@NonNull String column) {
        return where(column, NOT_NULL);
    }

    @NonNull
    public SQLiteQuery<T> appendWhere(@NonNull String where, @NonNull Object... bindArgs) {
        return where(where, "", bindArgs); // TODO: replace "" with StringValue.EMPTY
    }

    @NonNull
    public SQLiteQuery<T> groupBy(@NonNull String... columns) {
        mGroupBy = TextUtils.join(COMMA, columns);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> having(@NonNull String having, @NonNull Object... bindArgs) {
        mHaving = having;
        Collections.addAll(mBindArgs, bindArgs);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> orderBy(@NonNull String column) {
        return orderBy(column, true);
    }

    @NonNull
    public SQLiteQuery<T> orderBy(@NonNull String column, boolean ascending) {
        if (ascending) {
            mOrderBy.add(column + ASC);
        } else {
            mOrderBy.add(column + DESC);
        }
        return this;
    }

    @NonNull
    public SQLiteQuery<T> limit(int limit) {
        mLimit = String.valueOf(limit);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> offsetLimit(int offset, int limit) {
        mLimit = offset + COMMA + limit;
        return this;
    }
    //endregion

    @Nullable
    public T one() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public List<T> list() {
        return SQLiteResultReference.wrap(new SQLiteResult<>(this, cursor()));
    }

    @NonNull
    public Cursor cursor() {
        String[] bindArgs = new String[mBindArgs.size()];
        for (int i = 0; i < mBindArgs.size(); ++i) {
            bindArgs[i] = String.valueOf(mBindArgs.get(i));
        }
        return mClient.get().rawQuery(toString(), bindArgs);
    }

    public int remove() {
        final StringBuilder sql = new StringBuilder("DELETE FROM ")
                .append(getTableName());
        if (mWhere.length() > 0) {
            sql.append(" WHERE ").append(mWhere);
        }
        return mClient.get().executeUpdateDelete(sql.toString(), bindArgs());
    }

    @Override
    public String toString() {
        String where = null;
        if (mWhere != null) {
            where = mWhere.toString();
        }
        String orderBy = null;
        if (!mOrderBy.isEmpty()) {
            orderBy = TextUtils.join(COMMA, mOrderBy);
        }
        return SQLiteQueryBuilder.buildQueryString(mDistinct, SQLite.tableOf(mType), null, where,
                mGroupBy, mHaving, orderBy, mLimit);
    }

    //region package internal
    Object[] bindArgs() {
        if (!mBindArgs.isEmpty()) {
            return mBindArgs.toArray(new Object[mBindArgs.size()]);
        }
        return null;
    }

    @NonNull
    String getTableName() {
        return SQLite.tableOf(mType);
    }

    @NonNull
    SQLiteClient getClient() {
        return mClient.get();
    }

    @NonNull
    public Class<T> getType() {
        return mType;
    }

    private SQLiteQuery<T> where(@NonNull String column, @NonNull String op, @NonNull Object... values) {
        if (mWhere == null) {
            mWhere = new StringBuilder();
        }
        mWhere.append(column).append(op);
        Collections.addAll(mBindArgs, values);
        return this;
    }
    //endregion

}
