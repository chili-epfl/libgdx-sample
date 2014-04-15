package ch.epfl.chili.libgdx_sample.desktop;

import ch.epfl.chili.libgdx_sample.PlatformDependentMethods;

/**
 * Container for desktop dependent simple methods
 * @author Ayberk Özgür
 */
public class DesktopDependentMethods implements PlatformDependentMethods {
	
	@Override
	public String Chilitags_getTagConfigFilename() {
		//TODO:GET TAG CONFIG OUT OF JAR
		return null;
	}

	@Override
	public String Chilitags_getCalibrationFilename() {
		//TODO:GET TAG CONFIG OUT OF JAR
		return null;
	}
	
	@Override
	public void print(String message) {
		System.out.println(message);
	}
}
