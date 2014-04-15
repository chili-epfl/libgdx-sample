package ch.epfl.chili.libgdx_sample.desktop;

import java.awt.Dimension;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

import ch.epfl.chili.libgdx_sample.DeviceCameraController;
import ch.epfl.chili.libgdx_sample.util.Size2D;
import ch.epfl.chili.chilitags.Chilitags3D;
import ch.epfl.chili.chilitags.ObjectTransform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.sarxos.webcam.Webcam;

public class DesktopCameraController implements DeviceCameraController {

	private long lastMillis = 0;
	private double fps = 0.0;
	
	private Webcam camera = null;
	private Thread cameraThread;

	private final int width = 640;
	private final int height = 480;
	
	private static byte[] image = null; //The image buffer that will hold the camera image when preview callback arrives
	private ReentrantLock lock_image = new ReentrantLock();
	private static ByteBuffer imageBuffer;
	private ReentrantLock lock_imageBuffer = new ReentrantLock();
	
	private boolean running = false;
	
	private SpriteBatch spriteBatch;
	private Texture background;
	
	@Override
	public void init() {
		spriteBatch = new SpriteBatch();
		background = new Texture(width,height,Format.RGB888);
		
		image = new byte[width*height*3];
		imageBuffer = ByteBuffer.allocateDirect(width*height*3);

		camera = Webcam.getDefault();
		camera.setViewSize(new Dimension(width,height));
		camera.open();

		cameraThread = new Thread(){
			@Override
			public void run(){
				webcamRunner();
			}
		};
		running = true;
		cameraThread.start();
	}

	private void webcamRunner(){
		while(running){
			
			long currentMillis = System.currentTimeMillis();
			try{
				double fps_now = 1000/(currentMillis - lastMillis);
				fps = 0.8*fps + 0.2*fps_now;
			}catch(Exception e){}
			lastMillis = currentMillis;
			
			//lock_imageBuffer.lock();
			imageBuffer.position(0);
			imageBuffer.put(((DataBufferByte)camera.getImage().getRaster().getDataBuffer()).getData());
			imageBuffer.position(0);
			lock_image.lock();
			imageBuffer.get(image);
			lock_image.unlock();
			imageBuffer.position(0);
			//lock_imageBuffer.unlock();
		}
	}

	@Override
	public double getFPS() {
		return fps;
	}

	@Override
	public void renderBackground() {
		spriteBatch.begin();
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		background.bind();

		//lock_imageBuffer.lock();
		//Y texture is (width*height) in size and each pixel is one byte; by setting GL_LUMINANCE, OpenGL puts this byte into R,G and B components of the texture
		imageBuffer.position(0);
		Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, width, height, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, imageBuffer);
		//lock_imageBuffer.unlock();
		
		//Use linear interpolation when magnifying/minifying the texture to areas larger/smaller than the texture size
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);
		spriteBatch.draw(background, 0, 0);
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
	public Chilitags3D.InputType getImageFormat() {
		return Chilitags3D.InputType.RGB888;
	}

	@Override
	public ObjectTransform[] getTags(Chilitags3D chilitags) {
		ObjectTransform[] tags;
		lock_image.lock();
		tags = chilitags.estimate(image);
		lock_image.unlock();
		return tags;
	}
}
