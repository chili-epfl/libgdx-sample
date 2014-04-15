package ch.epfl.chili.libgdx_sample.desktop;

import java.awt.Dimension;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import ch.epfl.chili.libgdx_sample.DeviceCameraController;
import ch.epfl.chili.libgdx_sample.util.Size2D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.sarxos.webcam.Webcam;

/**
 * The device camera interface object for desktop
 * @author Ayberk Özgür
 */
public class DesktopCameraController implements DeviceCameraController {

	private long lastMillis = 0;
	private double fps = 0.0;
	
	private Webcam camera = null; //The device camera interface
	private Thread cameraThread; //The thread that will get the camera image
	private boolean running = false; //Indicator that we want the camera thread to abort
	
	private final int width = 640;
	private final int height = 480;
	
	private static byte[] image = null; //The image buffer that will hold the camera image when preview callback arrives
	private ReentrantLock imageLock = new ReentrantLock(); //Mutex to give exclusive access to image byte array
	private static ByteBuffer imageByteBuffer; //ByteBuffer to contain the byte array while it's passed to the texture

	private SpriteBatch spriteBatch; //Since we are not using a special shader for color space conversion, we can render the background with SpriteBatch's default shader
	private Texture background; //The background texture that holds the camera image
	
	@Override
	public void init() {
		
		//Initialize the libgdx objects
		spriteBatch = new SpriteBatch();
		background = new Texture(width,height,Format.RGB888);
		
		//Initialize the image buffers
		image = new byte[width*height*3];
		imageByteBuffer = ByteBuffer.allocateDirect(width*height*3);

		//Initialize the device camera interface
		camera = Webcam.getDefault();
		camera.setViewSize(new Dimension(width,height));
		camera.open();

		//Launch the device camera listener
		cameraThread = new Thread(){
			@Override
			public void run(){
				webcamRunner();
			}
		};
		running = true;
		cameraThread.start();
	}

	/**
	 * Listens to the device camera interface and fills the image buffer
	 */
	private void webcamRunner(){
		while(running){
			
			//Calculate the FPS
			long currentMillis = System.currentTimeMillis();
			try{
				double fpsNow = 1000/(currentMillis - lastMillis);
				fps = 0.8*fps + 0.2*fpsNow;
			}catch(Exception e){} //Guard against divide by zero
			lastMillis = currentMillis;
			
			//Get the image inside our byte array
			imageByteBuffer.position(0);
			imageByteBuffer.put(((DataBufferByte)camera.getImage().getRaster().getDataBuffer()).getData());
			imageByteBuffer.position(0);
			lockImage();
			imageByteBuffer.get(image);
			unlockImage();
		}
	}

	@Override
	public double getFPS() {
		return fps;
	}

	@Override
	public void renderBackground() {
		
		//Announce that we are rendering 2D images
		spriteBatch.begin();
		
		//Set the texture handle and bind our texture to it
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		background.bind();

		//Pass the camera image buffer's data to the texture
		//Y texture is (width*height) in size and each pixel is one byte; by setting GL_LUMINANCE, OpenGL puts this byte into R,G and B components of the texture
		imageByteBuffer.position(0);
		Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width, height, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, imageByteBuffer);
		
		//Use linear interpolation when magnifying/minifying the texture to areas larger/smaller than the texture size
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
		
		//Draw the texture
		spriteBatch.draw(background, 0, 0);
		
		//Announce that we are done rendering 2D images
		spriteBatch.end();
	}

	@Override
	public void destroy() {
		running = false;
		camera.close();
	}

	@Override
	public Size2D getCameraSize() {
		return new Size2D(width,height);
	}

	@Override
	public Size2D getProcessingSize() {
		return new Size2D(width,height);
	}

	@Override
	public ImageFormat getImageFormat() {
		return ImageFormat.RGB888;
	}

	@Override
	public byte[] getImage(){
		return image;
	}

	@Override
	public void lockImage() {
		imageLock.lock();
	}

	@Override
	public void unlockImage() {
		imageLock.unlock();
	}
}
