package droidkit.sqlite;

import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import droidkit.content.StringValue;
import droidkit.util.Lists;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQuery<T> implements SQLiteRawQuery, SQLiteOp {

    private final Class<T> mType;

    private final StringBuilder mWhere = new StringBuilder();

    private final List<Object> mBindArgs = new ArrayList<>();

    private final List<String> mGroupBy = new ArrayList<>();

    private final List<String> mOrderBy = new ArrayList<>();

    private boolean mDistinct;

    private String mHaving;

    private String mLimit;

    SQLiteQuery(@NonNull Class<T> type) {
        mType = type;
    }

    //region conditions
    @NonNull
    public SQLiteQuery<T> distinct() {
        mDistinct = true;
        return this;
    }

    @NonNull
    public SQLiteQuery<T> equalTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> notEqualTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, NOT_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThan(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, LT, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, LT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThan(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, GT, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, GT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> like(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, LIKE, value);
    }

    @NonNull
    public SQLiteQuery<T> between(@NonNull String column, @NonNull Object lv, @NonNull Object rv) {
        return appendWhere(column, BETWEEN, lv, rv);
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
        return appendWhere(column, IS_NULL);
    }

    @NonNull
    public SQLiteQuery<T> notNull(@NonNull String column) {
        return appendWhere(column, NOT_NULL);
    }

    @NonNull
    public SQLiteQuery<T> inSelect(@NonNull String column, @NonNull String select, @NonNull Object... bindArgs) {
        mWhere.append(column).append(" IN(").append(select).append(")");
        Collections.addAll(mBindArgs, bindArgs);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> inSelect(@NonNull String column, @NonNull Collection<?> values) {
        mWhere.append(column).append(" IN(")
                .append(TextUtils.join(COMMA, Collections.nCopies(values.size(), "?")))
                .append(")");
        Collections.addAll(mBindArgs, values);
        return this;
    }

    @Nullable
    public T withId(long id) {
        return equalTo(BaseColumns._ID, id).one();
    }

    @NonNull
    public SQLiteQuery<T> appendWhere(@NonNull String where, @NonNull Object... bindArgs) {
        return appendWhere(where, StringValue.EMPTY, bindArgs);
    }

    @NonNull
    public SQLiteQuery<T> and() {
        mWhere.append(AND);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> or() {
        mWhere.append(OR);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> beginGroup() {
        mWhere.append(LEFT_PARENTHESIS);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> endGroup() {
        mWhere.append(RIGHT_PARENTHESIS);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> groupBy(@NonNull String... columns) {
        Collections.addAll(mGroupBy, columns);
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
        final List<T> list = limit(1).list();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @NonNull
    public List<T> list() {
        return new SQLiteResult<>(this, cursor(), mType);
    }

    @NonNull
    @Override
    public Cursor cursor() {
        final Cursor cursor = SQLite.obtainClient().query(SQLiteQueryBuilder.buildQueryString(
                mDistinct,
                SQLiteSchema.resolveTable(mType),
                null, mWhere.toString(),
                TextUtils.join(COMMA, mGroupBy),
                mHaving,
                TextUtils.join(COMMA, mOrderBy),
                mLimit
        ), Lists.toArray(mBindArgs, Object.class));
        cursor.setNotificationUri(SQLite.obtainResolver(), SQLiteSchema.resolveUri(mType));
        return cursor;
    }

    public int clear() {
        final StringBuilder sql = new StringBuilder("DELETE FROM ").append(SQLiteSchema.resolveTable(mType));
        if (!TextUtils.isEmpty(mWhere)) {
            sql.append(WHERE).append(mWhere);
        }
        return SQLite.obtainClient().executeUpdateDelete(sql.toString(), Lists.toArray(mBindArgs, Object.class));
    }

    //region Functions
    @NonNull
    public Number min(@NonNull String column) {
        return applyFunc("MIN", column);
    }

    @NonNull
    public Number max(@NonNull String column) {
        return applyFunc("MAX", column);
    }

    @NonNull
    public Number sum(@NonNull String column) {
        return applyFunc("SUM", column);
    }

    @NonNull
    public Number count(@NonNull String column) {
        return applyFunc("COUNT", column);
    }
    //endregion

    @NonNull
    public Loader<List<T>> loader() {
        return new SQLiteLoader<>(SQLite.obtainContext(), this, mType);
    }

    @Override
    public String toString() {
        return WHERE + mWhere.toString();
    }

    @NonNull
    private SQLiteQuery<T> appendWhere(@NonNull String column, @NonNull String op, @NonNull Object... values) {
        mWhere.append(column).append(op);
        Collections.addAll(mBindArgs, values);
        return this;
    }

    @NonNull
    private Number applyFunc(@NonNull String func, @NonNull String column) {
        final StringBuilder sql = new StringBuilder("SELECT ").append(func)
                .append(LEFT_PARENTHESIS).append(column).append(RIGHT_PARENTHESIS)
                .append(" FROM ")
                .append(SQLiteSchema.resolveTable(mType));
        if (!TextUtils.isEmpty(mWhere)) {
            sql.append(WHERE).append(mWhere);
        }
        return Double.parseDouble(SQLite.obtainClient().queryForString(sql.toString(),
                Lists.toArray(mBindArgs, Object.class)));
    }

}
