/*******************************************************************************
 * Copyright (c) 2015 Goldman Sachs.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.collections.impl.partition.set.sorted;

import org.eclipse.collections.api.partition.set.sorted.PartitionImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import net.jcip.annotations.Immutable;

@Immutable
public class PartitionImmutableSortedSetImpl<T> implements PartitionImmutableSortedSet<T>
{
    private final ImmutableSortedSet<T> selected;
    private final ImmutableSortedSet<T> rejected;

    public PartitionImmutableSortedSetImpl(PartitionTreeSortedSet<T> partitionTreeSortedSet)
    {
        this.selected = partitionTreeSortedSet.getSelected().toImmutable();
        this.rejected = partitionTreeSortedSet.getRejected().toImmutable();
    }

    public ImmutableSortedSet<T> getSelected()
    {
        return this.selected;
    }

    public ImmutableSortedSet<T> getRejected()
    {
        return this.rejected;
    }
}
