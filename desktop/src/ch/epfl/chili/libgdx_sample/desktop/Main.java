package ch.epfl.chili.libgdx_sample.desktop;

import ch.epfl.chili.libgdx_sample.LibgdxSample;
import ch.epfl.chili.libgdx_sample.DeviceCameraController;
import ch.epfl.chili.libgdx_sample.PlatformDependentMethods;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * The entry point object that runs the application on desktop
 * @author Ayberk Özgür
 */
public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "libgdx Sample";
		cfg.width = 640;
		cfg.height = 480;

		DeviceCameraController camController = new DesktopCameraController();
		PlatformDependentMethods platformDeps = new DesktopDependentMethods();

		new LwjglApplication(new LibgdxSample(platformDeps,camController), cfg);
	}
}
