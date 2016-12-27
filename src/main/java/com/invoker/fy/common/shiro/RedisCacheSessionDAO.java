/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.common.shiro;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author huafangyuan
 * @date 2016/12/27
 */
public class RedisCacheSessionDAO extends CachingSessionDAO {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(RedisCacheSessionDAO.class);
	private RedisTemplate		redisTemplate;
	private byte[]				prefix;

	public RedisCacheSessionDAO(RedisTemplate redisTemplate, String prefix) {
		this.redisTemplate = redisTemplate;
		this.prefix = prefix.concat(":").getBytes();
	}

	public void setCacheManager(CacheManager cacheManager) {
		if (null == this.getCacheManager()) {
			super.setCacheManager(cacheManager);
		}

	}

	@Override
	protected void doUpdate(Session session) {
		LOGGER.trace("尝试从Redis中更新/保存Session: {}", session.getId());
		final byte[] k = this.computeKey(session.getId());
		final byte[] f = SerialUtils.serialize(session.getId());
		final byte[] v = SerialUtils.serialize(session);
		final int expiration = (int) (session.getTimeout() / 1000L);
		redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				redisConnection.hSet(k, f, v);
				if (expiration > 0) {
					redisConnection.expire(k, expiration);
				}
				return true;
			}
		});
	}

	@Override
	protected void doDelete(Session session) {
		LOGGER.trace("尝试从Redis中删除Session: {}", session.getId());
		final byte[] k = this.computeKey(session.getId());
		redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				return redisConnection.del(k);
			}
		});
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = this.generateSessionId(session);
		this.assignSessionId(session, sessionId);
		LOGGER.trace("分配新Session的ID: {}", sessionId);
		return sessionId;
	}

	@Override
	protected Session doReadSession(final Serializable sessionId) {
		LOGGER.trace("尝试从Redis中读取Session: {}", sessionId);
		Session session = (Session) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
				byte[] bs = redisConnection.hGet(computeKey(sessionId), SerialUtils.serialize(sessionId));
				Object value = SerialUtils.deserialize(bs);
				return null == value ? null : (Session) value;
			}
		});
		return session;
	}

	private byte[] computeKey(Serializable key) {
		if (key instanceof byte[]) {
			return (byte[]) key;
		} else {
			byte[] k;
			if (key instanceof String) {
				k = ((String) key).getBytes();
			} else {
				k = SerializationUtils.serialize(key);
			}

			if (this.prefix != null && this.prefix.length != 0) {
				byte[] result = Arrays.copyOf(this.prefix, this.prefix.length + k.length);
				System.arraycopy(k, 0, result, this.prefix.length, k.length);
				return result;
			} else {
				return k;
			}
		}
	}
}
