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

public class LibgdxSample implements ApplicationListener {

	private Chilitags3D chilitags;

	private long lastMillis = 0;
	private double fps = 0;

	private final PlatformDependentMethods platformDeps;
	private final DeviceCameraController deviceCameraControl;

	public Environment environment;
	public PerspectiveCamera camera;
	public AssetManager assets;
	public Model treeModel;
	public Map<String, ModelInstance> treeModelInstances;
	public Map<String, AnimationController> treeModelAnimationControllers;
	public ModelBatch modelBatch;
	public BitmapFont font;
	public SpriteBatch spriteBatch;
	public boolean loading;
	
	public LibgdxSample(PlatformDependentMethods platformDependentMethods, DeviceCameraController cameraControl) {
		this.platformDeps = platformDependentMethods;
		this.deviceCameraControl = cameraControl;
	}

	@Override
	public void create() {      
		
		treeModelInstances = new HashMap<String, ModelInstance>();
		treeModelAnimationControllers = new HashMap<String, AnimationController>();
		
		loading = true;

		assets = new AssetManager();
		//assets.load("data/tree.g3db", Model.class);
		assets.load("models/funky_palm_tree/funky_palm_tree.g3db", Model.class);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0f, 0f, 100f));

		modelBatch = new ModelBatch();
		spriteBatch = new SpriteBatch(); 
		font = new BitmapFont();
		font.setColor(Color.RED);
		font.setScale(2);

		// Create the OpenGL Camera
		camera = new PerspectiveCamera(67.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0.0f,0.0f,0.0f);
		camera.up.set(0.0f,-1.0f,0.0f);
		camera.lookAt(0.0f, 0.0f, 1.0f);
		camera.far = 10000.0f;
		camera.near = 0.1f;
		camera.update();

		/*
		 * Create the device camera control
		 */
		
		deviceCameraControl.init();
		
		/*
		 * Create the Chilitags object
		 */
		
		//Get the camera image size
		Size2D cameraSize = deviceCameraControl.getCameraSize();
		
		//Get the image size that will be processed by Chilitags
		Size2D processingSize = deviceCameraControl.getProcessingSize();
		
		//Create the actual Chilitags object
		chilitags = new Chilitags3D(cameraSize.width,cameraSize.height,processingSize.width,processingSize.height,deviceCameraControl.getImageFormat());
		//chilitags.readTagConfiguration(platformDeps.Chilitags_getTagConfigFilename(), false);
		platformDeps.print(cameraSize.width+" "+cameraSize.height+" "+processingSize.width+" "+processingSize.height);
		double[] cc = {
				270*processingSize.width/640,	0,								processingSize.width/2,
				0,								270*processingSize.width/640,	processingSize.height/2,
				0,								0,								1};
		double[] dc = {};
		chilitags.setCalibration(cc,dc);	
	}

	private void doneLoading(){
		treeModel = assets.get("models/funky_palm_tree/funky_palm_tree.g3db", Model.class);

		loading = false;
		platformDeps.print("doneloading");
	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		spriteBatch.dispose();
		deviceCameraControl.destroy();
	}

	@Override
	public void render() { 
		if (loading && assets.update())
			doneLoading();

		long currentMillis = System.currentTimeMillis();
		try{
			double fps_now = 1000/(currentMillis - lastMillis);
			fps = 0.8*fps + 0.2*fps_now;
		}catch(Exception e){}
		lastMillis = currentMillis;

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		deviceCameraControl.renderBackground();
		
		ObjectTransform[] results = deviceCameraControl.getTags(chilitags); //TODO: GET THIS IN ANOTHER THREAD
		
		if(!loading){
			
			modelBatch.begin(camera);
			
			for(Entry<String, AnimationController> instance : treeModelAnimationControllers.entrySet())
				instance.getValue().update(Gdx.graphics.getDeltaTime());
			
			
			for(int k=0;k<results.length;k++){
				
				platformDeps.print(results[k].name);
				
				if(treeModelInstances.containsKey(results[k].name)){
					ModelInstance tree = treeModelInstances.get(results[k].name);
					float[] mat = new float[16];
					for(int i=0;i<4;i++)
						for(int j=0;j<4;j++){
							mat[j*4+i] = (float)results[k].transform[i][j];
						}

					tree.transform.set(mat);
					tree.transform.scale(10.0f, 10.0f, 10.0f);
					
					modelBatch.render(tree, environment);
				}
				else{
					ModelInstance newTree = new ModelInstance(treeModel);
					float[] mat = new float[16];
					for(int i=0;i<4;i++)
						for(int j=0;j<4;j++){
							mat[j*4+i] = (float)results[k].transform[i][j];
						}

					newTree.transform.set(mat);
					newTree.transform.scale(10.0f, 10.0f, 10.0f);
					treeModelInstances.put(results[k].name, newTree);
					
					AnimationController newAnim = new AnimationController(newTree);
					newAnim.setAnimation("grow");
					newAnim.queue("sway", -1, 1.0f, null, 0.5f);
					treeModelAnimationControllers.put(results[k].name, newAnim);
				}
			}
			
			modelBatch.end();
		}

		spriteBatch.begin();
		font.draw(spriteBatch,"RENDER: " + (int)fps+" FPS",30,30);
		font.draw(spriteBatch,"CAM: " + (int)deviceCameraControl.getFPS() +" FPS",30,60);
		spriteBatch.end();
	}
	
	@Override
	public void resize(int width, int height) {
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
