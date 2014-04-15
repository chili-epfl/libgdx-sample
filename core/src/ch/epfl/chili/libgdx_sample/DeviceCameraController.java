package ch.epfl.chili.libgdx_sample;

import ch.epfl.chili.libgdx_sample.util.Size2D;
import ch.epfl.chili.chilitags.Chilitags3D;
import ch.epfl.chili.chilitags.ObjectTransform;

/**
 * The base interface to describe device camera access in a platform independent manner.
 * 
 * @author Ayberk Özgür
 */
public interface DeviceCameraController {
	
	void init();
	
	Size2D getCameraSize();
	
	Size2D getProcessingSize();
	
	Chilitags3D.InputType getImageFormat();
	
	double getFPS();
	
	void renderBackground();
	
	void destroy();
	
	ObjectTransform[] getTags(Chilitags3D chilitags);
}