package ch.epfl.chili.libgdx_sample.android;

import android.os.Bundle;
import ch.epfl.chili.libgdx_sample.LibgdxSample;
import ch.epfl.chili.libgdx_sample.DeviceCameraController;
import ch.epfl.chili.libgdx_sample.PlatformDependentMethods;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class MainActivity extends AndroidApplication {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.a = 8;
        cfg.b = 8;
        cfg.g = 8;
        cfg.r = 8;
        
        PlatformDependentMethods platformDeps = new AndroidDependentMethods(getApplicationContext());
        DeviceCameraController cameraControl = new AndroidCameraController();
        initialize(new LibgdxSample(platformDeps, cameraControl), cfg);
        
        graphics.getView().setKeepScreenOn(true);
    }
    
    public void post(Runnable r){
    	handler.post(r);
    }
}