package ch.epfl.chili.libgdx_sample.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ch.epfl.chili.libgdx_sample.PlatformDependentMethods;
import android.content.Context;
import android.util.Log;

public class AndroidDependentMethods implements PlatformDependentMethods {

	private Context context;
	
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
			
			//Get the file's bytes and write them over
			Scanner configScanner = new Scanner(context.getAssets().open("tag_configuration.yml")).useDelimiter("\\A");
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
		// TODO ÜSTTEKİYLE AYNI ŞEYİ YAP
		return null;
	}
	
	@Override
	public void print(String message) {
		Log.i("AndroidDependentMethods.print",message);
	}

}
