/*
 * Copyright GDO - 2004
 */
package com.gdo.util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.gdo.helper.ClassHelper;

/**
 * <p>
 * This class implements the {@link java.util.Map} interface, backed by a
 * {@link java.util.ArrayList}.
 * </p>
 * 
 * <p>
 * It is designed to offer good performance for very small maps, especially
 * those that are frequently created and destroyed. The performance will be
 * extremely bad for large maps...
 * </p>
 */
public class ArrayMap<K, V> implements Map<K, V>, Cloneable {
	private static final int DEFAULT_SIZE = 5;

	private List<K> _keys; // list of keys
	private List<V> _values; // list of values

	/**
	 * Creates the map with an initial size.
	 */
	public ArrayMap(int size) {
		this._keys = new ArrayList<K>(size);
		this._values = new ArrayList<V>(size);
	}

	/**
	 * Creates the map with default size.
	 */
	public ArrayMap() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Clear the map (keys and values).
	 */
	@Override
	public void clear() {
		this._keys.clear();
		this._values.clear();
	}

	/**
	 * @return <tt>true</tt> if this map contains no key-value mappings.
	 */
	@Override
	public boolean isEmpty() {
		return this._keys.isEmpty();
	}

	/**
	 * @return <tt>true</tt> if this map contains a mapping for the specified key.
	 */
	@Override
	public boolean containsKey(Object key) {
		return this._keys.contains(key);
	}

	/**
	 * @return <tt>true</tt> if this map maps one or more keys to the specified
	 *         value.
	 */
	@Override
	public boolean containsValue(Object value) {
		return this._values.contains(value);
	}

	/**
	 * @return the number of key-value mappings in this map.
	 */
	@Override
	public int size() {
		return this._keys.size();
	}

	/**
	 * @return the value assigned to a specified key (<tt>null</tt> if not
	 *         exists).
	 */
	@Override
	public V get(Object key) {
		int index = this._keys.indexOf(key);
		if (index < 0)
			return null;
		return this._values.get(index);
	}

	/**
	 * Assignes a value to a specified key (replaces it if already assigned).
	 * 
	 * @return the element previously at the specified key.
	 */
	@Override
	public V put(K key, V value) {
		int index = this._keys.indexOf(key);
		if (index < 0) {
			this._keys.add(key);
			this._values.add(value);
			return null;
		}
		return this._values.set(index, value);
	}

	/**
	 * Removes the mapping for a key from this map if it is present.
	 * 
	 * @return the value removed (<tt>null</tt> if not exists).
	 */
	@Override
	public V remove(Object key) {
		int index = this._keys.indexOf(key);
		if (index < 0)
			return null;
		V value = this._values.get(index);
		this._keys.remove(index);
		this._values.remove(index);
		return value;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		if (ClassHelper.isEmpty(map))
			return;
		for (K key : map.keySet()) {
			put(key, map.get(key));
		}
	}

	@Override
	public Collection<V> values() {
		return this._values;
	}

	@Override
	public Set<K> keySet() {
		return new MySet<K>(this._keys);
	}

	public List<V> valuesList() {
		return this._values;
	}

	/**
	 * Should be not used.
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException(getClass().getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayMap<K, V> clone() throws CloneNotSupportedException {
		ArrayMap<K, V> clone = (ArrayMap<K, V>) super.clone();
		clone._keys = new ArrayList<K>(this._keys);
		clone._values = new ArrayList<V>(this._values);
		return clone;
	}

	public void trimToSize() {
		if (this._keys instanceof ArrayList<?>) {
			((ArrayList<?>) this._keys).trimToSize();
		}
		if (this._values instanceof ArrayList<?>) {
			((ArrayList<?>) this._values).trimToSize();
		}
	}

	/**
	 * Keys set implementations.
	 */
	private class MySet<O> extends AbstractSet<O> {
		private List<O> _list;

		MySet(List<O> list) {
			this._list = list;
		}

		@Override
		public int size() {
			return this._list.size();
		}

		@Override
		public Iterator<O> iterator() {
			return this._list.iterator();
		}

        @Override
        public Spliterator<O> spliterator() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean removeIf(Predicate<? super O> filter) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Stream<O> stream() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Stream<O> parallelStream() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void forEach(Consumer<? super O> action) {
            // TODO Auto-generated method stub
            
        }
	}

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public V replace(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        // TODO Auto-generated method stub
        return null;
    }
}