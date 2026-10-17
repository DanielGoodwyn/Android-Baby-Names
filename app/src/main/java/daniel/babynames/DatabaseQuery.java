package daniel.babynames;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DatabaseQuery<T extends DatabaseObject> {
    private String className;
    private Query query;
    private String orderKey;
    private boolean orderAscending = true;
    private int localLimit = -1;
    
    public DatabaseQuery(String className) {
        this.className = className;
        this.query = FirebaseFirestore.getInstance().collection(className);
    }

    public static <T extends DatabaseObject> DatabaseQuery<T> getQuery(String className) {
        return new DatabaseQuery<>(className);
    }

    public DatabaseQuery<T> whereEqualTo(String key, Object value) {
        query = query.whereEqualTo(key, value);
        return this;
    }

    public DatabaseQuery<T> whereMatches(String key, String regex) {
        String prefix = regex;
        if (prefix != null && prefix.startsWith("^")) prefix = prefix.substring(1);
        if (prefix != null) {
             prefix = prefix.replace(".*", "").replace("$", "");
        }
        if (prefix != null && !prefix.isEmpty()) {
             query = query.whereGreaterThanOrEqualTo(key, prefix)
                          .whereLessThan(key, prefix + "\uf8ff");
        }
        return this;
    }

    public DatabaseQuery<T> orderByAscending(String key) {
        this.orderKey = key;
        this.orderAscending = true;
        return this;
    }

    public DatabaseQuery<T> orderByDescending(String key) {
        this.orderKey = key;
        this.orderAscending = false;
        return this;
    }

    public DatabaseQuery<T> setLimit(int limit) {
        this.localLimit = limit;
        return this;
    }
    
    public interface FindCallback<T> {
        void done(List<T> objects, Exception e);
    }

    public void findInBackground(final FindCallback<T> callback) {
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<T> results = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                DatabaseObject obj = new DatabaseObject(className);
                obj.setObjectId(doc.getId());
                if (doc.getData() != null) {
                    obj.data.putAll(doc.getData());
                }
                results.add((T) obj);
            }
            
            if (orderKey != null) {
                java.util.Collections.sort(results, (a, b) -> {
                    Object valA = ((DatabaseObject)a).data.get(orderKey);
                    Object valB = ((DatabaseObject)b).data.get(orderKey);
                    if (valA instanceof Number && valB instanceof Number) {
                        double diff = ((Number)valA).doubleValue() - ((Number)valB).doubleValue();
                        int res = diff > 0 ? 1 : (diff < 0 ? -1 : 0);
                        return orderAscending ? res : -res;
                    }
                    if (valA != null && valB != null) {
                        int res = valA.toString().compareTo(valB.toString());
                        return orderAscending ? res : -res;
                    }
                    return 0;
                });
            }
            
            if (localLimit > 0 && results.size() > localLimit) {
                results = results.subList(0, localLimit);
            }
            
            if (callback != null) callback.done(results, null);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.done(null, e);
        });
    }

    public com.google.firebase.firestore.ListenerRegistration subscribe(final FindCallback<T> callback) {
        return query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                if (callback != null) callback.done(null, e);
                return;
            }
            if (queryDocumentSnapshots != null) {
                List<T> results = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    DatabaseObject obj = new DatabaseObject(className);
                    obj.setObjectId(doc.getId());
                    if (doc.getData() != null) {
                        obj.data.putAll(doc.getData());
                    }
                    results.add((T) obj);
                }
                
                if (orderKey != null) {
                    java.util.Collections.sort(results, (a, b) -> {
                        Object valA = ((DatabaseObject)a).data.get(orderKey);
                        Object valB = ((DatabaseObject)b).data.get(orderKey);
                        if (valA instanceof Number && valB instanceof Number) {
                            double diff = ((Number)valA).doubleValue() - ((Number)valB).doubleValue();
                            int res = diff > 0 ? 1 : (diff < 0 ? -1 : 0);
                            return orderAscending ? res : -res;
                        }
                        if (valA != null && valB != null) {
                            int res = valA.toString().compareTo(valB.toString());
                            return orderAscending ? res : -res;
                        }
                        return 0;
                    });
                }
                
                if (localLimit > 0 && results.size() > localLimit) {
                    results = results.subList(0, localLimit);
                }
                
                if (callback != null) callback.done(results, null);
            }
        });
    }
}
