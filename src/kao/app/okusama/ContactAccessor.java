package kao.app.okusama;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public abstract class ContactAccessor {
	public abstract Intent getContactPickerIntent();
	
	public abstract Map<String,String> getEmailData(Activity a, Intent data);
	
	   private static ContactAccessor sInstance;

	    public static ContactAccessor getInstance() {
	        if (sInstance == null) {
	            String className;
	            int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
	            Log.i("ContactAccessor","" + sdkVersion);
	            if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
	                className = "ContactAccessorOldApi";
	            } else {
	                className = "ContactAccessorNewApi";
	            }
	            try {
	                Class<? extends ContactAccessor> clazz =
	                      //  Class.forName(ContactAccessor.class.getPackage() + "." + className)
	                        Class.forName("kao.app.okusama." + className)
	                        .asSubclass(ContactAccessor.class);
	                sInstance = clazz.newInstance();
	            } catch (Exception e) {
	                throw new IllegalStateException(e);
	            }
	        }
	        return sInstance;
	    } 
    
}
