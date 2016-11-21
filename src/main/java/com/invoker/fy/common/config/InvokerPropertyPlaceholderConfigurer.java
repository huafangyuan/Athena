/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.common.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 属性文件占位配置类
 * @author huafangyuan
 * @date 2016/11/17
 */
public class InvokerPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements ResourceLoaderAware {

	public static final String	XML_FILE_EXTENSION	= ".xml";
	/**
	 * 资源路径
	 */
	private Resource[]			invokerLocations;
	/**
	 * 资源加载器
	 */
	private ResourceLoader		resourceLoader;

	public Resource[] getInvokerLocations() {
		return invokerLocations;
	}

	public void setInvokerLocations(Resource[] invokerLocations) {
		this.invokerLocations = invokerLocations;
	}

	/**
	 * 复写函数,实现对占位变量的解析
	 */
	@Override
	protected void loadProperties(Properties props) throws IOException {
		if (invokerLocations != null) {
			// (1)先筛选出所有真实的属性文件路径和变量属性文件路径
			List<Resource> realResourceList = new ArrayList<Resource>();
			List<Resource> paramResourceList = new ArrayList<Resource>();
			for (Resource location : invokerLocations) {
				ServletContextResource scLocation = (ServletContextResource) location;
				String path = scLocation.getPath();
				if (!path.startsWith("/$")) {
					realResourceList.add(scLocation);
				} else {
					paramResourceList.add(scLocation);
				}
			}

			// (2)加载真实属性文件
			if (!CollectionUtils.isEmpty(realResourceList)) {
				for (Resource location : realResourceList) {
					loadPropResource(props, location);
				}
			}

			// (3)解析属性变量为属性文件路径,并进行加载
			if (!CollectionUtils.isEmpty(paramResourceList)) {
				for (Resource location : paramResourceList) {
					ServletContextResource scLocation = (ServletContextResource) location;
					String path = scLocation.getPath();
					String propKey = path.substring(path.indexOf("{") + 1, path.lastIndexOf("}"));
					String pathValue = props.getProperty(propKey);
					if (!StringUtils.isEmpty(pathValue)) {
						pathValue = "WEB-INF/context/" + pathValue + ".properties";
						Resource resource = resourceLoader.getResource(pathValue);
						loadPropResource(props, resource);
					}
				}
			}
		}
	}

	/**
	 * 资源文件加载
	 *
	 * @param props
	 * @param resource
	 * @throws IOException
	 */
	private void loadPropResource(Properties props, Resource resource) throws IOException {
		PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();
		InputStream is = null;
		try {
			is = resource.getInputStream();
			if (resource.getFilename().endsWith(XML_FILE_EXTENSION)) {
				propertiesPersister.loadFromXml(props, is);
			} else {
				propertiesPersister.load(props, is);
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
}
