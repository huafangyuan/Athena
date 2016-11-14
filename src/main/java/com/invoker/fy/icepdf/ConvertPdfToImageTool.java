/*
 * Copyright 2016 Focus Technology, Co., Ltd. All rights reserved.
 */
package com.invoker.fy.icepdf;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * pdf转图片工具
 * @author huafangyuan
 * @date 2016/11/11
 */
public class ConvertPdfToImageTool {

	private static String	sourceFileDir	= "C:\\PDF";
	private static String	targetFileDir	= "C:\\IMAGE";



	public static void main(String[] args) {
		capturePages(sourceFileDir);
	}

	public static void capturePages(String filePath) {
		System.out.println("PDF文件开始转化图片格式...");
		File[] files = new File(filePath).listFiles();
		for (File file : files) {
			String pdfFile = file.getAbsolutePath();
			Document document = new Document();
			try {
				document.setFile(pdfFile);
			} catch (PDFException e) {
				e.printStackTrace();
			} catch (PDFSecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < document.getNumberOfPages(); i++) {
				BufferedImage image = (BufferedImage) document.getPageImage(i, GraphicsRenderingHints.SCREEN,
						Page.BOUNDARY_CROPBOX, 0f, 2f);
				RenderedImage renderedImage = image;
				File targetFile = new File(targetFileDir);
				if (!targetFile.exists()) {
					targetFile.mkdirs();
				}
				String fileName = file.getName().substring(0, file.getName().indexOf("."));
				File targetIageFile = new File(targetFile.getAbsolutePath() + File.separator + fileName);
				if (!targetIageFile.exists()) {
					targetIageFile.mkdirs();
				}
				String imageName = targetIageFile.getAbsolutePath() + File.separator + fileName + "_" + i + ".png";
				File imageFile = new File(imageName);
				try {
					ImageIO.write(renderedImage, "png", imageFile);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					image.flush();
				}
				System.out.println("图片路径名：" + imageName);
			}
			document.dispose();
		}
	}

}
