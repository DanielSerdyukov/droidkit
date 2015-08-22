package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public final class Sets {

    private Sets() {
        //no instance
    }

    @NonNull
    public static <T> T getFirst(@NonNull Set<T> set) {
        checkNotEmpty(set);
        return set.iterator().next();
    }

    @Nullable
    public static <T> T getFirst(@NonNull Set<T> set, @Nullable T emptyValue) {
        if (set.isEmpty()) {
            return emptyValue;
        }
        return set.iterator().next();
    }

    @NonNull
    public static <T> T getLast(@NonNull Set<T> set) {
        checkNotEmpty(set);
        return Iterables.getLast(set.iterator());
    }

    @Nullable
    public static <T> T getLast(@NonNull Set<T> set, @Nullable T emptyValue) {
        if (set.isEmpty()) {
            return emptyValue;
        }
        return Iterables.getLast(set.iterator());
    }

    @NonNull
    public static <T, R> Set<R> transform(@NonNull Set<T> set, @NonNull Func1<T, R> transform) {
        final List<R> transformed = new ArrayList<>(set.size());
        for (final T element : set) {
            transformed.add(transform.call(element));
        }
        return new LinkedHashSet<>(transformed);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(@NonNull Set<T> set, @NonNull Class<T> type) {
        return set.toArray((T[]) Array.newInstance(type, set.size()));
    }

    @VisibleForTesting
    static <T> void checkNotEmpty(@NonNull Set<T> set) {
        if (set.isEmpty()) {
            throw new NoSuchElementException("set is empty");
        }
    }

}
