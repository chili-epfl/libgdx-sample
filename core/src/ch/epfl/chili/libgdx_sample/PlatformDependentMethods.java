package ch.epfl.chili.libgdx_sample;

/**
 * A platform independent base for all the simple and short platform dependent methods (camera interface is not one of them)
 * @author Ayberk Özgür
 */
public interface PlatformDependentMethods {
	
	/**
	 * Gets the tag configuration filename with absolute path. Creates a temporary file if necessary.
	 * 
	 * @return The tag configuration filename with absolute path
	 */
	public String Chilitags_getTagConfigFilename();
	
	/**
	 * Gets the camera calibration filename with absolute path. Creates a temporary file if necessary.
	 * 
	 * @return The camera calibration filename with absolute path
	 */
	public String Chilitags_getCalibrationFilename();
	
	/**
	 * Prints the given message to the platform's default (standard) output.
	 * 
	 * @param message The message to be shown
	 */
	public void print(String message);
}
