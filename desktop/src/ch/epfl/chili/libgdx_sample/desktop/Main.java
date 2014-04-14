package ch.epfl.chili.libgdx_sample.desktop;

import ch.epfl.chili.libgdx_sample.LibgdxSample;
import ch.epfl.chili.libgdx_sample.PlatformDependentCameraController;
import ch.epfl.chili.libgdx_sample.PlatformDependentMethods;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "libgdx Sample";
		cfg.width = 640;
		cfg.height = 480;

		PlatformDependentCameraController camController = new DesktopDependentCameraController();
		PlatformDependentMethods platformDeps = new DesktopDependentMethods();

		new LwjglApplication(new LibgdxSample(platformDeps,camController), cfg);
	}
}
