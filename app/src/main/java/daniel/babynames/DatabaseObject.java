package daniel.babynames;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class DatabaseObject {
    protected String className;
    protected String objectId;
    public Map<String, Object> data = new HashMap<>();

    public DatabaseObject(String className) {
        this.className = className;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    public int getInt(String key) {
        Object val = data.get(key);
        if (val instanceof Number) return ((Number)val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String)val); } catch(Exception e){}
        }
        return 0;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String id) {
        this.objectId = id;
    }

    public void saveInBackground() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (objectId == null) {
            db.collection(className).add(data)
                .addOnSuccessListener(docRef -> {
                    objectId = docRef.getId();
                });
        } else {
            db.collection(className).document(objectId).set(data);
        }
    }
    
    public void deleteInBackground() {
        if (objectId != null) {
            FirebaseFirestore.getInstance().collection(className).document(objectId).delete();
        }
    }
}
