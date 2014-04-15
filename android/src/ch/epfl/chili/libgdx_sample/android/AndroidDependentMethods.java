package ch.epfl.chili.libgdx_sample.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ch.epfl.chili.libgdx_sample.PlatformDependentMethods;
import android.content.Context;
import android.util.Log;

/**
 * Container for Android dependent simple methods
 * @author Ayberk Özgür
 */
public class AndroidDependentMethods implements PlatformDependentMethods {

	private Context context;
	private Scanner scanner;
	
	/**
	 * Creates a new Android dependents method holder.
	 * @param context The Android context needed by the platform dependent code
	 */
	public AndroidDependentMethods(Context context){
		this.context = context;
	}
	
	@Override
	public String Chilitags_getTagConfigFilename() {
		
		//Get the tag_configuration.yml file bundled with our apk and write it to a location accessible by the native code
		try {
			
			scanner = new Scanner(context.getAssets().open("tag_configuration.yml"));
			Scanner configScanner = scanner.useDelimiter("\\A");
			byte[] configBytes = (configScanner.hasNext() ? configScanner.next() : "").getBytes();
			configScanner.close();
			FileOutputStream newConfigFile = context.openFileOutput("tag_configuration.yml", Context.MODE_PRIVATE);
			newConfigFile.write(configBytes);
			newConfigFile.close();
			
			//Return the absolute path of the newly created configuration file
			return context.getFilesDir() + "/tag_configuration.yml";
			
		} catch (IOException e1) {
			
			//There was no tag_configuration.yml in the assets
			e1.printStackTrace();
			return null;
		}
	}

	@Override
	public String Chilitags_getCalibrationFilename() {
		//TODO DO THE SAME THING AS WITH GETCONFIGFILENAME
		return null;
	}
	
	@Override
	public void print(String message) {
		Log.i("AndroidDependentMethods.print",message);
	}

}
