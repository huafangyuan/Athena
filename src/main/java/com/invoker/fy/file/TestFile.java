/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author huafangyuan
 * @date 2016/11/16
 */
public class TestFile implements java.io.Serializable {

	private transient String userName;

	public TestFile(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return this.userName.toString();
	}

	public static void main(String[] args) {

		TestFile file = new TestFile("张三");
		System.out.println(file.toString());
		try {
			String filePath = "E:\\ceshi";
			File folder = new File(filePath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(""));
			TestFile testFile = (TestFile) in.readObject();
			System.out.println(testFile.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		/*
		 * // 1.获取当前目录绝对路径 System.out.println(System.getProperty("user.dir"));
		 */
	}

	/**
	 * 创建目录
	 * @param folderPath
	 */
	public static void newFolder(String folderPath) {
		File file = new File(folderPath);
		if (!file.exists()) {
			file.mkdirs();
		}

	}

	/**
	 * 创建文件
	 * @param filePath
	 */
	public static void newFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("创建文件操作出错");
			}
		}
	}

}
