package ch.epfl.chili.libgdx_sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.epfl.chili.libgdx_sample.util.Size2D;
import ch.epfl.chili.chilitags.Chilitags3D;
import ch.epfl.chili.chilitags.ObjectTransform;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

/**
 * The main application object
 * @author Ayberk Özgür
 */
public class LibgdxSample implements ApplicationListener {

	private Chilitags3D chilitags; //The Chilitags object

	private long lastMillis = 0;
	private double fps = 0;

	private final PlatformDependentMethods platformDeps; //The platform dependent global methods
	private final DeviceCameraController deviceCameraControl; //The webcam controller

	/*
	 * Libgdx specific fields
	 */
	private Environment environment; //Our environment that contains the lights/camera/models etc.
	private PerspectiveCamera camera; //The camera that we use to view our 3D environment
	private AssetManager assets; //The manager that loads the 3D models/sprites etc.
	private Model treeModel; //Our tree model, we can create multiple ModelInstances from this
	private Map<String, ModelInstance> treeModelInstances; //The container that maps tag names to model instances
	private Map<String, AnimationController> treeModelAnimationControllers; //The container that maps tag names to animation controllers of model instances
	private ModelBatch modelBatch; //The object that we use to render all tree models in one frame
	private BitmapFont font; //The font that we use to draw the FPS messages
	private SpriteBatch spriteBatch; //The object that we use to render all fonts in one frame
	private boolean loading; //Indicates whether the model data is still loading in the beginning
	
	/**
	 * Creates a new main libgdx-sample object
	 * Every platform calls this method with their own platform-dependent instantiated objects
	 * @param platformDependentMethods The platform-dependent method container
	 * @param cameraControl The platform-dependent device camera controller
	 */
	public LibgdxSample(PlatformDependentMethods platformDependentMethods, DeviceCameraController cameraControl) {
		this.platformDeps = platformDependentMethods;
		this.deviceCameraControl = cameraControl;
	}

	@Override
	public void create() {      
		
		/*
		 * Libgdx objects
		 */
		
		//Create the maps for our model instances
		treeModelInstances = new HashMap<String, ModelInstance>();
		treeModelAnimationControllers = new HashMap<String, AnimationController>();
		modelBatch = new ModelBatch();
		
		//Create asset manager and load our model
		assets = new AssetManager();
		loading = true;
		assets.load("models/funky_palm_tree/funky_palm_tree.g3db", Model.class);

		//Create the environment and set the light in there
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0f, 0f, 100f));

		//Create the objects needed to render text
		spriteBatch = new SpriteBatch(); 
		font = new BitmapFont();
		font.setColor(Color.RED);
		font.setScale(2);

		//Create the GL Camera
		camera = new PerspectiveCamera(67.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0.0f,0.0f,0.0f); //Camera position
		camera.up.set(0.0f,-1.0f,0.0f); //Up vector
		camera.lookAt(0.0f, 0.0f, 1.0f); //Forward vector
		camera.far = 10000.0f; //Far clipping plane distance
		camera.near = 0.1f; //Near clipping plane distance
		camera.update();

		/*
		 * Device camera controller
		 */
		
		deviceCameraControl.init();
		
		/*
		 * Chilitags object
		 */
		
		//Get the camera image size
		Size2D cameraSize = deviceCameraControl.getCameraSize();
		
		//Get the image size that will be processed by Chilitags
		Size2D processingSize = deviceCameraControl.getProcessingSize();
		
		//Create the actual Chilitags object
		chilitags = new Chilitags3D(
				cameraSize.width,cameraSize.height,
				processingSize.width,processingSize.height,
				Chilitags3D.InputType.values()[deviceCameraControl.getImageFormat().ordinal()]);
		double[] cc = {
				270*processingSize.width/640,	0,								processingSize.width/2,
				0,								270*processingSize.width/640,	processingSize.height/2,
				0,								0,								1};
		double[] dc = {};
		chilitags.setCalibration(cc,dc);	
	}

	/**
	 * Creates the models when the assets are done loading
	 */
	private void doneLoading(){
		treeModel = assets.get("models/funky_palm_tree/funky_palm_tree.g3db", Model.class);
		loading = false;
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		spriteBatch.dispose();
		deviceCameraControl.destroy();
	}

	@Override
	public void render() { 

		//Create model if assets are done loading
		if (loading && assets.update())
			doneLoading();

		//Calculate render loop FPS
		long currentMillis = System.currentTimeMillis();
		try{
			double fpsNow = 1000/(currentMillis - lastMillis);
			fps = 0.8*fps + 0.2*fpsNow;
		}catch(Exception e){}
		lastMillis = currentMillis;

		//Necessary GL calls
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		/*
		 * Render the camera image first
		 */
		
		deviceCameraControl.renderBackground();
		
		/*
		 * Detect tags and draw trees on top of detected tags
		 */
		
		ObjectTransform[] results = chilitags.estimate(deviceCameraControl.getImage());
		
		if(!loading){
			
			//Announce that we are rendering 3D models now; this will render the 3D models on top of the camera image
			modelBatch.begin(camera);
			
			//Update all animations of instances
			for(Entry<String, AnimationController> instance : treeModelAnimationControllers.entrySet())
				instance.getValue().update(Gdx.graphics.getDeltaTime());
			
			//Traverse all detected tags
			for(int k=0;k<results.length;k++){
				
				if(treeModelInstances.containsKey(results[k].name)){ //If detected tag already has a model instance associated with it
					
					//Update the model's transform with the new one from Chilitags
					ModelInstance tree = treeModelInstances.get(results[k].name);
					float[] mat = new float[16];
					for(int i=0;i<4;i++)
						for(int j=0;j<4;j++)
							mat[j*4+i] = (float)results[k].transform[i][j];
					tree.transform.set(mat);
					tree.transform.scale(10.0f, 10.0f, 10.0f);
					
					//Render current model instance
					modelBatch.render(tree, environment);
				}
				else{ //If detected tag does not yet have a model instance associated with it
					
					//Create new model instance
					ModelInstance newTree = new ModelInstance(treeModel);
					
					//Set transform with the one calculated by Chilitags
					float[] mat = new float[16];
					for(int i=0;i<4;i++)
						for(int j=0;j<4;j++)
							mat[j*4+i] = (float)results[k].transform[i][j];
					newTree.transform.set(mat);
					newTree.transform.scale(10.0f, 10.0f, 10.0f);
					
					//Add the new instance to the map
					treeModelInstances.put(results[k].name, newTree);
					
					//Create the animation controller for the new instance
					AnimationController newAnim = new AnimationController(newTree);
					
					//First, play the grow animation
					newAnim.setAnimation("grow");
					
					//Then, play the sway animation in a loop forever
					newAnim.queue("sway", -1, 1.0f, null, 0.5f);
					
					//Add the animation controller to the map
					treeModelAnimationControllers.put(results[k].name, newAnim);
				}
			}
			
			//Announce that we are done rendering 3D models
			modelBatch.end();
		}

		/*
		 * Render FPS texts
		 */
		
		//Announce that we are rendering sprites now; this will render the text above the camera image and 3D models
		spriteBatch.begin();
		
		//Draw FPS texts
		font.draw(spriteBatch,"RENDER: " + (int)fps+" FPS",30,30);
		font.draw(spriteBatch,"CAM: " + (int)deviceCameraControl.getFPS() +" FPS",30,60);
		
		//Announce that we are done rendering sprites
		spriteBatch.end();
	}
	
	@Override
	public void resize(int width, int height) {
		
		//Recreate the GL camera
		camera = new PerspectiveCamera(67.0f, 2.0f * width / height, 2.0f);
		camera.position.set(0.0f,0.0f,0.0f);
		camera.up.set(0.0f,-1.0f,0.0f);
		camera.lookAt(0.0f, 0.0f, 1.0f);
		camera.far = 10000.0f;
		camera.near = 0.1f;
		camera.update();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
