package ch.epfl.chili.libgdx_sample;

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
