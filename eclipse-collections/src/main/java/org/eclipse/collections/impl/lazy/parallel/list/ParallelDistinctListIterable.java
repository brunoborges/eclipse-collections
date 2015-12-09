/*******************************************************************************
 * Copyright (c) 2015 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.collections.impl.lazy.parallel.list;

import java.util.concurrent.ExecutorService;

import org.eclipse.collections.api.LazyIterable;
import org.eclipse.collections.api.annotation.Beta;
import org.eclipse.collections.api.block.function.Function;
import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.block.procedure.Procedure;
import org.eclipse.collections.api.map.MapIterable;
import org.eclipse.collections.api.multimap.set.UnsortedSetMultimap;
import org.eclipse.collections.api.set.ParallelUnsortedSetIterable;
import org.eclipse.collections.impl.lazy.parallel.set.AbstractParallelUnsortedSetIterable;
import org.eclipse.collections.impl.lazy.parallel.set.UnsortedSetBatch;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.collections.impl.multimap.set.UnifiedSetMultimap;

@Beta
class ParallelDistinctListIterable<T> extends AbstractParallelUnsortedSetIterable<T, UnsortedSetBatch<T>>
{
    private final AbstractParallelListIterable<T, ? extends ListBatch<T>> delegate;

    ParallelDistinctListIterable(AbstractParallelListIterable<T, ? extends ListBatch<T>> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public ExecutorService getExecutorService()
    {
        return this.delegate.getExecutorService();
    }

    @Override
    public int getBatchSize()
    {
        return this.delegate.getBatchSize();
    }

    @Override
    public LazyIterable<UnsortedSetBatch<T>> split()
    {
        // TODO: Replace the map with a concurrent set once it's implemented
        final ConcurrentHashMap<T, Boolean> distinct = new ConcurrentHashMap<T, Boolean>();
        return this.delegate.split().collect(new Function<ListBatch<T>, UnsortedSetBatch<T>>()
        {
            public UnsortedSetBatch<T> valueOf(ListBatch<T> listBatch)
            {
                return listBatch.distinct(distinct);
            }
        });
    }

    @Override
    public ParallelUnsortedSetIterable<T> asUnique()
    {
        return this;
    }

    public void forEach(final Procedure<? super T> procedure)
    {
        // TODO: Replace the map with a concurrent set once it's implemented
        final ConcurrentHashMap<T, Boolean> distinct = new ConcurrentHashMap<T, Boolean>();
        this.delegate.forEach(new Procedure<T>()
        {
            public void value(T each)
            {
                if (distinct.put(each, true) == null)
                {
                    procedure.value(each);
                }
            }
        });
    }

    public boolean anySatisfy(Predicate<? super T> predicate)
    {
        return this.delegate.anySatisfy(new DistinctAndPredicate<T>(predicate));
    }

    public boolean allSatisfy(Predicate<? super T> predicate)
    {
        return this.delegate.allSatisfy(new DistinctOrPredicate<T>(predicate));
    }

    public T detect(Predicate<? super T> predicate)
    {
        return this.delegate.detect(new DistinctAndPredicate<T>(predicate));
    }

    @Override
    public Object[] toArray()
    {
        // TODO: Implement in parallel
        return this.delegate.toList().distinct().toArray();
    }

    @Override
    public <E> E[] toArray(E[] array)
    {
        // TODO: Implement in parallel
        return this.delegate.toList().distinct().toArray(array);
    }

    @Override
    public <V> UnsortedSetMultimap<V, T> groupBy(Function<? super T, ? extends V> function)
    {
        // TODO: Implement in parallel
        return this.delegate.toSet().groupBy(function, new UnifiedSetMultimap<V, T>());
    }

    @Override
    public <V> UnsortedSetMultimap<V, T> groupByEach(Function<? super T, ? extends Iterable<V>> function)
    {
        // TODO: Implement in parallel
        return this.delegate.toSet().groupByEach(function, new UnifiedSetMultimap<V, T>());
    }

    @Override
    public <V> MapIterable<V, T> groupByUniqueKey(Function<? super T, ? extends V> function)
    {
        // TODO: Implement in parallel
        return this.delegate.toSet().groupByUniqueKey(function);
    }

    private static final class DistinctAndPredicate<T> implements Predicate<T>
    {
        // TODO: Replace the map with a concurrent set once it's implemented
        private final ConcurrentHashMap<T, Boolean> distinct = new ConcurrentHashMap<T, Boolean>();
        private final Predicate<? super T> predicate;

        private DistinctAndPredicate(Predicate<? super T> predicate)
        {
            this.predicate = predicate;
        }

        public boolean accept(T each)
        {
            return this.distinct.put(each, true) == null && this.predicate.accept(each);
        }
    }

    private static final class DistinctOrPredicate<T> implements Predicate<T>
    {
        // TODO: Replace the map with a concurrent set once it's implemented
        private final ConcurrentHashMap<T, Boolean> distinct = new ConcurrentHashMap<T, Boolean>();
        private final Predicate<? super T> predicate;

        private DistinctOrPredicate(Predicate<? super T> predicate)
        {
            this.predicate = predicate;
        }

        public boolean accept(T each)
        {
            boolean distinct = this.distinct.put(each, true) == null;
            return distinct && this.predicate.accept(each) || !distinct;
        }
    }
}
