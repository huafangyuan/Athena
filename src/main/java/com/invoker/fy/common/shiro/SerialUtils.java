/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.common.shiro;

import org.springframework.util.SerializationUtils;

import java.util.Arrays;

/**
 * @author huafangyuan
 * @date 2016/12/27
 */
public abstract class SerialUtils {

	private static final byte[] STREAM_MAGIC_BYTES = new byte[] { (byte) -84, (byte) -19 };

	public SerialUtils() {
	}

	public static byte[] serialize(Object object) {
		return object == null ? null
				: (object instanceof String ? ((String) object).getBytes() : SerializationUtils.serialize(object));
	}

	public static Object deserialize(byte[] bytes) {
		if (bytes == null) {
			return null;
		} else {
			byte[] header = Arrays.copyOf(bytes, Math.min(bytes.length, 2));
			return Arrays.equals(header, STREAM_MAGIC_BYTES) ? SerializationUtils.deserialize(bytes)
					: new String(bytes);
		}
	}
}
