package ch.epfl.chili.libgdx_sample.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import ch.epfl.chili.libgdx_sample.PlatformDependentCameraController;
import ch.epfl.chili.libgdx_sample.util.Size2D;
import ch.epfl.chili.chilitags.Chilitags3D;
import ch.epfl.chili.chilitags.Chilitags3D.InputType;
import ch.epfl.chili.chilitags.ObjectTransform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import android.hardware.Camera;
import android.hardware.Camera.Size;

public class AndroidDependentCameraController implements PlatformDependentCameraController, Camera.PreviewCallback {

	private long lastMillis = 0;
	private double fps = 0.0;

	private static byte[] image = null; //The image buffer that will hold the camera image when preview callback arrives

	private Camera camera; //The camera object

	private Size2D cameraSize; //The camera image size
	private Size2D processingSize; //The image size that Chilitags is responsible with

	//The Y and UV buffers that will pass our image channel data to the textures
	private ByteBuffer yBuffer;
	private ByteBuffer uvBuffer;

	ShaderProgram shader; //Our shader
	Texture yTexture; //Our Y texture
	Texture uvTexture; //Our UV texture
	Mesh mesh; //Our mesh that we will draw the texture on

	@Override
	public void init(){

		/*
		 * Initialize the Android camera
		 */
		camera = Camera.open(0);

		//We set the buffer ourselves that will be used to hold the preview image
		camera.setPreviewCallbackWithBuffer(this); 

		//Set the camera parameters
		Camera.Parameters params = camera.getParameters();

		//Get camera size
		List<Size> sizeList = params.getSupportedVideoSizes(); //We don't get the preferred preview size because it may somehow be a 4:3 size on a 16:9 camera
		if(sizeList != null){
			Size[] sizes = sizeList.toArray(new Size[sizeList.size()]);
			cameraSize = new Size2D(sizes[0].width,sizes[0].height);
		}
		else //We might be on the emulator, set a very generic size that is 640x480
			cameraSize = new Size2D(640,480);
		params.setPreviewSize(cameraSize.width,cameraSize.height); 

		//Our YUV image is 12 bits per pixel
		image = new byte[cameraSize.width*cameraSize.height/8*12];

		//Tell camera to autofocus continuously
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

		//Set the parameters
		camera.setParameters(params);

		//Chilitags is too slow for HD so we downsample the image to a more reasonable size
		processingSize = new Size2D();
		processingSize.width = cameraSize.width > 640 ? 640 : cameraSize.width;
		processingSize.height = cameraSize.height * processingSize.width / cameraSize.width;

		/*
		 * Initialize the OpenGL/libgdx stuff
		 */

		//Allocate textures
		yTexture = new Texture(cameraSize.width,cameraSize.height,Format.Intensity); //A 8-bit per pixel format
		uvTexture = new Texture(cameraSize.width/2,cameraSize.height/2,Format.LuminanceAlpha); //A 16-bit per pixel format

		//Allocate buffers on the native memory space, not inside the JVM heap
		yBuffer = ByteBuffer.allocateDirect(cameraSize.width*cameraSize.height);
		uvBuffer = ByteBuffer.allocateDirect(cameraSize.width*cameraSize.height/2); //We have (width/2*height/2) pixels, each pixel is 2 bytes
		yBuffer.order(ByteOrder.nativeOrder());
		uvBuffer.order(ByteOrder.nativeOrder());

		//Our vertex shader code; nothing special
		String vertexShader = 
				"attribute vec4 a_position;							\n" + 
						"attribute vec2 a_texCoord;							\n" + 
						"varying vec2 v_texCoord;							\n" + 

				"void main(){										\n" + 
				"   gl_Position = a_position;						\n" + 
				"   v_texCoord = a_texCoord;						\n" +
				"}													\n";

		//Our fragment shader code; takes Y,U,V values for each pixel and calculates R,G,B colors,
		//Effectively making YUV to RGB conversion
		String fragmentShader = 
				"#ifdef GL_ES										\n" +
						"precision highp float;								\n" +
						"#endif												\n" +

				"varying vec2 v_texCoord;							\n" +
				"varying vec2 v_texCoordChroma;						\n" +
				"uniform sampler2D y_texture;						\n" +
				"uniform sampler2D uv_texture;						\n" +

				"void main (void){									\n" +
				"	float r, g, b, y, u, v;							\n" +

				//We had put the Y values of each pixel to the R,G,B components by GL_LUMINANCE, 
				//that's why we're pulling it from the R component, we could also use G or B
				"	y = texture2D(y_texture, v_texCoord).r;			\n" + 

				//We had put the U and V values of each pixel to the A and R,G,B components of the
				//texture respectively using GL_LUMINANCE_ALPHA. Since U,V bytes are interspread 
				//in the texture, this is probably the fastest way to use them in the shader
				"	u = texture2D(uv_texture, v_texCoord).a - 0.5;	\n" +									
				"	v = texture2D(uv_texture, v_texCoord).r - 0.5;	\n" +


				//The numbers are just YUV to RGB conversion constants
				"	r = y + 1.13983*v;								\n" +
				"	g = y - 0.39465*u - 0.58060*v;					\n" +
				"	b = y + 2.03211*u;								\n" +

				//We finally set the RGB color of our pixel
				"	gl_FragColor = vec4(r, g, b, 1.0);				\n" +
				"}													\n"; 

		//Create and compile our shader
		shader = new ShaderProgram(vertexShader, fragmentShader);

		//Create our mesh that we will draw on, it has 4 vertices corresponding to the 4 corners of the screen
		mesh = new Mesh(true, 4, 6, 
				new VertexAttribute(Usage.Position, 2, "a_position"), 
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord"));

		//The vertices include the screen coordinates (between -1.0 and 1.0) and texture coordinates (between 0.0 and 1.0)
		float[] vertices = {
				-1.0f,	1.0f,	// Position 0
				0.0f,	0.0f,	// TexCoord 0
				-1.0f,	-1.0f,	// Position 1
				0.0f,	1.0f,	// TexCoord 1
				1.0f,	-1.0f,	// Position 2
				1.0f,	1.0f,	// TexCoord 2
				1.0f,	1.0f,	// Position 3
				1.0f,	0.0f	// TexCoord 3
		};

		//The indices come in trios of vertex indices that describe the triangles of our mesh
		short[] indices = {0, 1, 2, 0, 2, 3};

		//Set vertices and indices to our mesh
		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		/*
		 * Start the camera
		 */

		//Start the preview
		camera.startPreview();

		//Set the first buffer, the preview doesn't start unless we set the buffers
		camera.addCallbackBuffer(image);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		long currentMillis = System.currentTimeMillis();
		try{
			double fps_now = 1000/(currentMillis - lastMillis);
			fps = 0.8*fps + 0.2*fps_now;
		}catch(Exception e){}
		lastMillis = currentMillis;

		//Send the buffer reference to the next preview so that a new buffer is not allocated and we use the same space
		camera.addCallbackBuffer(image);
	}

	@Override
	public double getFPS(){
		return fps;
	}

	@Override
	public void renderBackground() {

		/*
		 * Because of Java's limitations, we can't reference the middle of an array and 
		 * we must copy the channels in our byte array into buffers before setting them to textures
		 */

		//Copy the Y channel of the image into its buffer, the first (width*height) bytes are the Y channel
		yBuffer.put(image, 0, cameraSize.width*cameraSize.height);
		yBuffer.position(0);

		//Copy the UV channels of the image into their buffer, the following (width*height/2) bytes are the UV channel; the U and V bytes are interspread
		uvBuffer.put(image, cameraSize.width*cameraSize.height, cameraSize.width*cameraSize.height/2);
		uvBuffer.position(0);

		/*
		 * Prepare the Y channel texture
		 */

		//Set texture slot 0 as active and bind our texture object to it
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		yTexture.bind();

		//Y texture is (width*height) in size and each pixel is one byte; by setting GL_LUMINANCE, OpenGL puts this byte into R,G and B components of the texture
		Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_LUMINANCE, cameraSize.width, cameraSize.height, 0, GL20.GL_LUMINANCE, GL20.GL_UNSIGNED_BYTE, yBuffer);

		//Use linear interpolation when magnifying/minifying the texture to areas larger/smaller than the texture size
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);

		/*
		 * Prepare the UV channel texture
		 */

		//Set texture slot 1 as active and bind our texture object to it
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
		uvTexture.bind();

		//UV texture is (width/2*height/2) in size (downsampled by 2 in both dimensions, each pixel corresponds to 4 pixels of the Y channel) 
		//and each pixel is two bytes. By setting GL_LUMINANCE_ALPHA, OpenGL puts first byte (V) into R,G and B components and of the texture
		//and the second byte (U) into the A component of the texture. That's why we find U and V at A and R respectively in the fragment shader code.
		//Note that we could have also found V at G or B as well. 
		Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_LUMINANCE_ALPHA, cameraSize.width/2, cameraSize.height/2, 0, GL20.GL_LUMINANCE_ALPHA, GL20.GL_UNSIGNED_BYTE, uvBuffer);

		//Use linear interpolation when magnifying/minifying the texture to areas larger/smaller than the texture size
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_CLAMP_TO_EDGE);
		Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_CLAMP_TO_EDGE);

		/*
		 * Draw the textures onto a mesh using our shader
		 */

		shader.begin();

		//Set the uniform y_texture object to the texture at slot 0
		shader.setUniformi("y_texture", 0);

		//Set the uniform uv_texture object to the texture at slot 1
		shader.setUniformi("uv_texture", 1);

		//Render our mesh using the shader, which in turn will use our textures to render their content on the mesh
		mesh.render(shader, GL20.GL_TRIANGLES);

		shader.end();
	}

	@Override
	public void destroy() {
		camera.stopPreview();
		camera.setPreviewCallbackWithBuffer(null);
		camera.release();
	}

	@Override
	public Size2D getCameraSize() {
		return cameraSize;
	}

	@Override
	public Size2D getProcessingSize() {
		return processingSize;
	}

	@Override
	public InputType getImageFormat() {
		return InputType.YUV_NV21;
	}

	@Override
	public ObjectTransform[] getTags(Chilitags3D chilitags) {
		return chilitags.estimate(image);
	}
}
