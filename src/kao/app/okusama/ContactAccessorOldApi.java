package kao.app.okusama;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.Contacts;
import android.provider.Contacts.ContactMethodsColumns;
import android.provider.Contacts.People;

public class ContactAccessorOldApi extends ContactAccessor {

	public Intent getContactPickerIntent() {
		Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
		//IS01不具合対処（詳細はメールで）
		intent.setType(android.provider.Contacts.People.CONTENT_TYPE);
		
		return intent;
		
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
				id = c.getString(c.getColumnIndexOrThrow(People._ID));
			}

			if (c.moveToFirst()) {
				name = c.getString(c.getColumnIndexOrThrow(People.NAME));
			}
		} finally {
			if (c != null)
				c.close();
		}

		try {
			ec = a.getContentResolver().query(
					Contacts.ContactMethods.CONTENT_EMAIL_URI, null,
					Contacts.ContactMethods.PERSON_ID + " = ?",
					new String[] { id }, null);

			if (ec.moveToFirst()) {
				int emailIndex = ec
						.getColumnIndexOrThrow(ContactMethodsColumns.DATA);
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
