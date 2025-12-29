package application.utils;

import java.util.prefs.Preferences;

public class CredentialStore {

    private static final Preferences prefs = Preferences.userNodeForPackage(CredentialStore.class);
    private static final String KEY_USERNAME = "saved_username";
    private static final String KEY_PASSWORD = "saved_password";

    public static void saveCredentials(String username, String password) {
        if(username !=null&&password !=null) {
            prefs.put(KEY_USERNAME, username);
            prefs.put(KEY_PASSWORD, password);
        }
    }

    public static String[] loadCredentials() {
        String u= prefs.get(KEY_USERNAME, null);
        String p= prefs.get(KEY_PASSWORD, null);

        if(u!=null&&p!=null) {
            return new String[]{u,p};
        }
        return null;
    }

    public static void clearCredentials() {
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_PASSWORD);
    }
}
