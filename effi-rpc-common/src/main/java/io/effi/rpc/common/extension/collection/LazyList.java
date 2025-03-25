package io.effi.rpc.common.extension.collection;

import io.effi.rpc.common.extension.LazyInitializer;

import java.util.*;
import java.util.function.Supplier;

/**
 * Lazily initialized {@link List} implementation.
 * The underlying list is created only when necessary, which can save memory
 * and improve performance in scenarios where the list may not be used immediately.
 *
 * @param <E> the type of elements in this list
 */
public class LazyList<E> extends LazyInitializer<List<E>> implements List<E> {

    public LazyList(Supplier<List<E>> supplier) {
        super(supplier);
    }

    @Override
    public boolean add(E e) {
        return get(false).add(e);
    }

    @Override
    public void add(int index, E element) {
        get(false).add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return get(false).addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return get(false).addAll(index, c);
    }

    @Override
    public void clear() {
        if (instance != null) {
            instance.clear();
        }
    }

    @Override
    public boolean contains(Object o) {
        return instance != null && instance.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return instance != null && instance.containsAll(c);
    }

    @Override
    public E get(int index) {
        return instance == null ? null : instance.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return instance == null ? -1 : instance.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return instance == null || instance.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return instance == null ? Collections.emptyIterator() : instance.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return instance == null ? -1 : instance.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return instance == null ? Collections.emptyListIterator() : instance.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return instance == null ? Collections.emptyListIterator() : instance.listIterator(index);
    }

    @Override
    public E remove(int index) {
        return instance == null ? null : instance.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return instance != null && instance.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return instance != null && instance.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return instance != null && instance.retainAll(c);
    }

    @Override
    public E set(int index, E element) {
        return get(false).set(index, element);
    }

    @Override
    public int size() {
        return instance == null ? 0 : instance.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return get(false).subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return instance == null ? new Object[0] : instance.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return instance == null ? a : instance.toArray(a);
    }
}
