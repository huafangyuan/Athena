/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.common.shiro;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author huafangyuan
 * @date 2016/12/27
 */
public class SingleValueThreadLocalCache<K, V> implements Cache<K, V> {

	private final ThreadLocal<SingleValueThreadLocalCache<K, V>.Pair<K, V>>	local	= new ThreadLocal();
	private String															name;

	public SingleValueThreadLocalCache(String name) {
		this.name = name;
	}

	@Override
	public V get(K key) throws CacheException {
		if (null == key) {
			return null;
		} else {
			SingleValueThreadLocalCache.Pair pair = (SingleValueThreadLocalCache.Pair) this.local.get();
			return null != pair && key.equals(pair.getKey()) ? (V) pair.getV() : null;
		}
	}

	@Override
	public V put(K key, V value) throws CacheException {
		if (null == key) {
			return null;
		} else {
			SingleValueThreadLocalCache.Pair pair = (SingleValueThreadLocalCache.Pair) this.local.get();
			if (null == pair) {
				pair = new SingleValueThreadLocalCache.Pair();
			}

			Object v = key.equals(pair.getKey()) ? pair.getV() : null;
			pair.setKey(key);
			pair.setV(value);
			this.local.set(pair);
			return (V) v;
		}
	}

	@Override
	public V remove(K key) throws CacheException {
		Object v = this.get(key);
		if (null != v) {
			this.local.remove();
		}

		return (V) v;
	}

	@Override
	public void clear() throws CacheException {
		this.local.remove();
	}

	@Override
	public int size() {
		SingleValueThreadLocalCache.Pair pair = (SingleValueThreadLocalCache.Pair) this.local.get();
		return null != pair && null != pair.getV() ? 1 : 0;
	}

	@Override
	public Set<K> keys() {
		Object keys = null;
		SingleValueThreadLocalCache.Pair pair = (SingleValueThreadLocalCache.Pair) this.local.get();
		if (null != pair && null != pair.getKey()) {
			keys = Sets.newHashSet();
			((Set) keys).add(pair.getKey());
		} else {
			keys = Collections.emptySet();
		}

		return (Set) keys;
	}

	@Override
	public Collection<V> values() {
		Object values = null;
		SingleValueThreadLocalCache.Pair pair = (SingleValueThreadLocalCache.Pair) this.local.get();
		if (null != pair && null != pair.getV()) {
			values = Lists.newArrayList();
			((Collection) values).add(pair.getV());
		} else {
			values = Collections.emptyList();
		}

		return (Collection) values;
	}

	public String toString() {
		return "SingleValueThreadLocalCache \'" + this.name + "\' (" + this.size() + " entries)";
	}

	private final class Pair<KK, VV> {
		private KK	key;
		private VV	value;

		private Pair() {
		}

		public KK getKey() {
			return this.key;
		}

		public void setKey(KK key) {
			this.key = key;
		}

		public VV getV() {
			return this.value;
		}

		public void setV(VV v) {
			this.value = v;
		}
	}
}
