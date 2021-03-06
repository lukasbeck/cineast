package org.vitrivr.cineast.core.data;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ImageCacheConfig.Policy;

public class MultiImageFactory {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private MultiImageFactory(){
	}
	
	
	public static MultiImage newMultiImage(BufferedImage bimg){
		return newMultiImage(bimg, null);
	}
	
	public static MultiImage newMultiImage(BufferedImage bimg, BufferedImage thumb){
		if(keepInMemory()){
			return new InMemoryMultiImage(bimg, thumb);
		}else{
			return new CachedMultiImage(bimg, thumb);
		}
	}
	
	public static MultiImage newMultiImage(int width, int height, int[] colors){
		if(keepInMemory()){
			height = MultiImageFactory.checkHeight(width, height, colors);
			BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			bimg.setRGB(0, 0, width, height, colors, 0, width);
			return new InMemoryMultiImage(bimg);
		}else{
			return new CachedMultiImage(width, height, colors);
		}
	}
	
	public static MultiImage newInMemoryMultiImage(BufferedImage bimg){
		if(Config.getImageMemoryConfig().getCachingPolicy() == Policy.FORCE_DISK_CACHE){
			LOGGER.warn("creating cached instead of in memory MultiImage because of policy");
			return new CachedMultiImage(bimg);
		}
		return new InMemoryMultiImage(bimg);
	}
	
	/**
	 * determines whether or not an image should be held in memory or cached to disk
	 * @return
	 */
	private static boolean keepInMemory(){
		long freeMemory = Runtime.getRuntime().freeMemory();
		
		Policy cachePolicy = Config.getImageMemoryConfig().getCachingPolicy();
		long hardMinMemory = Config.getImageMemoryConfig().getHardMinMemory();
		long softMinMemory = Config.getImageMemoryConfig().getSoftMinMemory();
		
		if(cachePolicy == Policy.AVOID_CACHE){
			if(freeMemory > hardMinMemory){
				return true;
			}else{
				System.gc();
				return false;
			}
		}
		
		if(cachePolicy == Policy.FORCE_DISK_CACHE || cachePolicy == Policy.DISK_CACHE){
			return false;
		}
		
		//check if access comes from preferred source
		boolean isVideoDecoder = false;
		
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for(StackTraceElement element : stackTraceElements){
			if(element.getClassName().toLowerCase().contains("videodecoder")){
				isVideoDecoder = true;
				break;
			}
		}
		
		if(freeMemory > hardMinMemory){
			if(!isVideoDecoder){
				return true;
			}
			return freeMemory > softMinMemory;
		}else{
			System.gc();
			return false;
		}
		
	}
	
	static int checkHeight(int width, int height, int[] colors){
		if(colors.length / width != height){
			LOGGER.debug("dimension missmatch in MultiImage, setting height from {} to {}", height, (height = colors.length / width));
		}
		return height;
	}

	public static BufferedImage copyBufferedImg(BufferedImage img) {
		ColorModel cm = img.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = img.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
}
