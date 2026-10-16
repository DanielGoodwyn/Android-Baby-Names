package daniel.babynames;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class AppPrefs {
    private static final String PREFS_NAME = "BabyNamesPrefs";

    public static void saveString(Context context, String key, String value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(key, value).apply();
        syncToFirebase(key, value);
    }

    public static void saveInt(Context context, String key, int value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt(key, value).apply();
        syncToFirebase(key, value);
    }

    public static String getString(Context context, String key, String defValue) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(key, defValue);
    }

    public static int getInt(Context context, String key, int defValue) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(key, defValue);
    }

    public static void syncToFirebase(String key, Object value) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> update = new HashMap<>();
            update.put(key, value);
            // use set with merge so it creates document if it doesn't exist
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).set(update, SetOptions.merge());
        }
    }

    public static void loadFromFirebase(Context context, final Runnable onLoaded) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        SharedPreferences.Editor ed = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                        if (doc.contains("username")) ed.putString("username", doc.getString("username"));
                        if (doc.contains("gender")) ed.putString("gender", doc.getString("gender"));
                        if (doc.contains("letter")) ed.putString("letter", doc.getString("letter"));
                        if (doc.contains("name")) ed.putString("name", doc.getString("name"));
                        if (doc.contains("limit")) {
                             Object limitObj = doc.get("limit");
                             if (limitObj instanceof Number) ed.putInt("limit", ((Number)limitObj).intValue());
                             else if (limitObj instanceof String) {
                                  try { ed.putInt("limit", Integer.parseInt((String)limitObj)); } catch (Exception ignored) {}
                             }
                        }
                        if (doc.contains("sort")) ed.putString("sort", doc.getString("sort"));
                        ed.apply();
                    }
                    if (onLoaded != null) onLoaded.run();
                }).addOnFailureListener(e -> {
                    if (onLoaded != null) onLoaded.run();
                });
        } else {
            if (onLoaded != null) onLoaded.run();
        }
    }

    public static void logout(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
    }
}
