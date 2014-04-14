package ch.epfl.chili.libgdx_sample.util;

/**
 * Such a class literally doesn't exist inside Android-compatible portion of Java...
 * 
 * @author Ayberk Özgür
 *
 */
public class Size2D {
	
	public int width;
	public int height;
	
	public Size2D(){
		width = 0;
		height = 0;
	}
	
	public Size2D(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
