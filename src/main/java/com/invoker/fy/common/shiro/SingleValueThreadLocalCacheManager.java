/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.common.shiro;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

/**
 * @author huafangyuan
 * @date 2016/12/27
 */
public class SingleValueThreadLocalCacheManager extends AbstractCacheManager {

	@Override
	protected Cache createCache(String name) throws CacheException {
		return new SingleValueThreadLocalCache(name);
	}
}
