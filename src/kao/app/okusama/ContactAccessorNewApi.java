package kao.app.okusama;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactAccessorNewApi extends ContactAccessor {    
    @Override
    public Intent getContactPickerIntent() {
        return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    }
    
    
    
    @Override
	public Map<String, String> getEmailData(Activity a, Intent data) {

		Map<String, String> result = new HashMap<String, String>();
		Cursor c = null;
		String id = "";
		String name = "";
		Cursor ec = null;
		String email = "";

		try {
			c = a.managedQuery(data.getData(), null, null, null, null);

			if (c.moveToFirst()) {
				id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
			}

			if (c.moveToFirst()) {
				name = c
						.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
			}
		} finally {
			if (c != null)
				c.close();
		}
		try {
			ec = a.getContentResolver().query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
					new String[] { id }, null);

			if (ec.moveToFirst()) {
				int emailIndex = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
				email = ec.getString(emailIndex);
			}
		} finally {
			if (ec != null)
				ec.close();
		}
	

		result.put("name", name);
		result.put("email", email);

		return result;

	}
    
}
