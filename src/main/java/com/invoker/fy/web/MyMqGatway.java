/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.web;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huafangyuan
 * @date 2016/11/25
 */
public class MyMqGatway {

	@Autowired
	private AmqpTemplate amqpTemplate;

	public void sendDataToCrQueue(Object obj) {
		amqpTemplate.convertAndSend("queue_one_key", obj);
	}
}
