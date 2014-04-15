package ch.epfl.chili.libgdx_sample;

import ch.epfl.chili.libgdx_sample.util.Size2D;

/**
 * The base interface to describe device camera access in a platform independent manner.
 * 
 * @author Ayberk Özgür
 */
public interface DeviceCameraController {
	
	/**
	 * The possible image formats used by the camera
	 */
	public enum ImageFormat{
		YUV_NV21,
		RGB565,
		RGB888
	}
	
	/**
	 * Initializes the camera
	 */
	void init();
	
	/**
	 * Gets the camera image size
	 * @return The camera image size in pixels
	 */
	Size2D getCameraSize();
	
	/**
	 * Gets a potentially smaller size that is more suitable for image processing
	 * The size has the same aspect ratio as the camera image
	 * @return The smaller size that is suitable for image processing
	 */
	Size2D getProcessingSize();
	
	/**
	 * Gets the image format that the camera image is in
	 * @return The image format
	 */
	ImageFormat getImageFormat();
	
	/**
	 * Gets the real-time FPS of the camera access
	 * @return The real-time FPS of the camera access
	 */
	double getFPS();
	
	/**
	 * Renders the camera image onto the framebuffer using GLES
	 */
	void renderBackground();
	
	/**
	 * Locks the image for reading, call before getImage()
	 */
	void lockImage();
	
	/**
	 * Gets the last camera image
	 * @return The last camera image
	 */
	byte[] getImage();
	
	/**
	 * Unlocks the image for getting the next frame, call after getImage()
	 */
	void unlockImage();
	
	/**
	 * Releases the camera permanently
	 */
	void destroy();
}
