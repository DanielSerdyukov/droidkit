package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Daniel Serdyukov
 */
public final class Iterables {

    private Iterables() {
    }

    @SuppressWarnings("unchecked")
    public static <E, T> List<T> transform(@NonNull Iterable<E> iterable, @NonNull Func<E, T> func) {
        final List<T> list = new ArrayList<>();
        for (final E element : iterable) {
            list.add(func.apply(element));
        }
        return list;
    }

    @NonNull
    public static <T> T getFirst(@NonNull Iterable<T> iterable) {
        if (iterable instanceof List) {
            final List<T> list = (List<T>) iterable;
            if (list.isEmpty()) {
                throw new NoSuchElementException();
            }
            return list.get(0);
        }
        return iterable.iterator().next();
    }

    @Nullable
    public static <T> T getFirst(@NonNull Iterable<T> iterable, @Nullable T defaultValue) {
        if (iterable instanceof Collection) {
            final Collection<T> collection = (Collection<T>) iterable;
            if (collection.isEmpty()) {
                return defaultValue;
            } else if (iterable instanceof List) {
                final List<T> list = (List<T>) iterable;
                return list.get(0);
            }
        }
        return iterable.iterator().next();
    }

    @NonNull
    public static <T> T getLast(@NonNull Iterable<T> iterable) {
        if (iterable instanceof List) {
            final List<T> list = (List<T>) iterable;
            if (list.isEmpty()) {
                throw new NoSuchElementException();
            }
            return list.get(list.size() - 1);
        }
        return getLast(iterable.iterator());
    }

    @Nullable
    public static <T> T getLast(@NonNull Iterable<T> iterable, @Nullable T defaultValue) {
        if (iterable instanceof Collection) {
            final Collection<T> collection = (Collection<T>) iterable;
            if (collection.isEmpty()) {
                return defaultValue;
            } else if (iterable instanceof List) {
                final List<T> list = (List<T>) iterable;
                return list.get(list.size() - 1);
            }
        }
        return getLast(iterable.iterator());
    }

    private static <T> T getLast(@NonNull Iterator<T> iterator) {
        T last = iterator.next();
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        return last;
    }

    public interface Func<E, T> {
        T apply(@NonNull E original);
    }

}
