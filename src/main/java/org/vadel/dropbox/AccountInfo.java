package org.vadel.dropbox;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountInfo {
	public String referralLink;
	public String displayName;
	public String uid;
	public String email;
	public String country;
	public long quotaShared;
	public long quota;
	public long quotaNormal;

	@Override
	public String toString() {
		return "{ " + displayName + ", " + uid + ", " + country + " }";
	}
	
	public static AccountInfo fromJSON(String s) {
		try {
			return fromJSON(new JSONObject(s));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static AccountInfo fromJSON(JSONObject obj) {
		try {
			AccountInfo account = new AccountInfo();
			account.referralLink = obj.getString("referral_link");
			account.displayName = obj.getString("display_name");
			account.uid = obj.getString("uid");
			account.country = obj.getString("country");
			if (obj.has("email"))
				account.email = obj.getString("email");
			JSONObject quota = obj.getJSONObject("quota_info");
			if (quota != null) {
				account.quota = quota.getLong("quota");
				account.quotaShared = quota.getLong("shared");
				account.quotaNormal = quota.getLong("normal");
			} else {
				account.quota = 0l;
				account.quotaShared = 0l;
				account.quotaNormal = 0l;
			}
			return account;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}}
