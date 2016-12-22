/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.web;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * @author huafangyuan
 * @date 2016/11/25
 */
public class QueueOneLitener implements MessageListener {

	@Override
	public void onMessage(Message message) {
		System.out.println(" data :" + message.getBody());
	}
}
