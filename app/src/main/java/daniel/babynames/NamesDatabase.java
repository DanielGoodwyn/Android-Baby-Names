package daniel.babynames;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NamesDatabase {
    private static final String TAG = "NamesDatabase";
    private static NamesDatabase instance;

    public static class NameItem {
        public String name;
        public String gender;
        public int rank;
        public int count;

        public NameItem(String name, String gender, int rank, int count) {
            this.name = name;
            this.gender = gender;
            this.rank = rank;
            this.count = count;
        }
    }

    private final List<NameItem> allNames = new ArrayList<>();
    private final List<NameItem> boyNames = new ArrayList<>();
    private final List<NameItem> girlNames = new ArrayList<>();
    private boolean loaded = false;

    public static synchronized NamesDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new NamesDatabase(context.getApplicationContext());
        }
        return instance;
    }

    private NamesDatabase(Context context) {
        loadData(context);
    }

    private void loadData(Context context) {
        if (loaded) return;
        try {
            InputStream is = context.getAssets().open("names.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int read = is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, 0, read, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(jsonStr);
            int len = array.length();
            for (int i = 0; i < len; i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name");
                String gender = obj.optString("gender");
                int rank = obj.optInt("rank", 99999);
                int count = obj.optInt("count", 0);
                NameItem item = new NameItem(name, gender, rank, count);
                allNames.add(item);
                if ("M".equalsIgnoreCase(gender)) {
                    boyNames.add(item);
                } else if ("F".equalsIgnoreCase(gender)) {
                    girlNames.add(item);
                }
            }
            loaded = true;
            Log.d(TAG, "Loaded " + allNames.size() + " names (" + boyNames.size() + " boys, " + girlNames.size() + " girls)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load names.json", e);
        }
    }

    public List<NameItem> getNames(String gender, String letter, String sort, int limit) {
        List<NameItem> source;
        if ("M".equalsIgnoreCase(gender)) {
            source = boyNames;
        } else if ("F".equalsIgnoreCase(gender)) {
            source = girlNames;
        } else {
            source = allNames;
        }

        boolean hasLetterFilter = (letter != null && !letter.isEmpty() && !"ALL".equalsIgnoreCase(letter) && !"*".equals(letter));
        String prefix = hasLetterFilter ? letter.toLowerCase() : "";

        List<NameItem> filtered = new ArrayList<>();
        for (NameItem item : source) {
            if (hasLetterFilter) {
                if (item.name == null || !item.name.toLowerCase().startsWith(prefix)) {
                    continue;
                }
            }
            filtered.add(item);
        }

        // Sort
        if ("uncommon".equalsIgnoreCase(sort)) {
            Collections.sort(filtered, new Comparator<NameItem>() {
                @Override
                public int compare(NameItem a, NameItem b) {
                    if (a.rank != b.rank) {
                        return Integer.compare(b.rank, a.rank); // Higher rank number = less common
                    }
                    return a.name.compareToIgnoreCase(b.name);
                }
            });
        } else if ("popular".equalsIgnoreCase(sort)) {
            Collections.sort(filtered, new Comparator<NameItem>() {
                @Override
                public int compare(NameItem a, NameItem b) {
                    if (a.rank != b.rank) {
                        return Integer.compare(a.rank, b.rank); // Lower rank number = more popular
                    }
                    return a.name.compareToIgnoreCase(b.name);
                }
            });
        } else if ("namesZtoA".equalsIgnoreCase(sort)) {
            Collections.sort(filtered, new Comparator<NameItem>() {
                @Override
                public int compare(NameItem a, NameItem b) {
                    return b.name.compareToIgnoreCase(a.name);
                }
            });
        } else { // namesAtoZ or default
            Collections.sort(filtered, new Comparator<NameItem>() {
                @Override
                public int compare(NameItem a, NameItem b) {
                    return a.name.compareToIgnoreCase(b.name);
                }
            });
        }

        if (limit > 0 && limit < filtered.size()) {
            return new ArrayList<>(filtered.subList(0, limit));
        }
        return filtered;
    }

    public List<NameItem> searchNames(String query, int limit) {
        if (query == null) return Collections.emptyList();
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return Collections.emptyList();
        if (limit <= 0) limit = 50;

        List<NameItem> prefixMatches = new ArrayList<>();
        List<NameItem> containsMatches = new ArrayList<>();

        for (NameItem item : allNames) {
            if (item.name == null) continue;
            String lower = item.name.toLowerCase();
            if (lower.startsWith(q)) {
                prefixMatches.add(item);
                if (prefixMatches.size() >= limit) break;
            } else if (lower.contains(q)) {
                if (containsMatches.size() < limit) {
                    containsMatches.add(item);
                }
            }
        }

        List<NameItem> result = new ArrayList<>(prefixMatches);
        for (NameItem item : containsMatches) {
            if (result.size() >= limit) break;
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }
}
