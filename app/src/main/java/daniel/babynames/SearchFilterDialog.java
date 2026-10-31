package daniel.babynames;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchFilterDialog extends Dialog {

    public interface OnLimitChangedListener {
        void onLimitChanged(int newLimit);
    }

    private final Context context;
    private final OnLimitChangedListener limitListener;
    private int currentLimit;
    private String currentUserId;

    private Button btn25;
    private Button btn50;
    private Button btn100;
    private Button btnAll;
    private EditText searchInput;
    private ListView resultsList;
    private TextView emptyText;

    private final List<NamesDatabase.NameItem> searchResults = new ArrayList<>();
    private final Set<String> favoriteNames = new HashSet<>();
    private ResultsAdapter adapter;

    public SearchFilterDialog(@NonNull Context context, int currentLimit, OnLimitChangedListener limitListener) {
        super(context);
        this.context = context;
        this.currentLimit = currentLimit > 0 ? currentLimit : 25;
        this.limitListener = limitListener;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search_filter);

        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        btn25 = findViewById(R.id.limit_25);
        btn50 = findViewById(R.id.limit_50);
        btn100 = findViewById(R.id.limit_100);
        btnAll = findViewById(R.id.limit_all);
        searchInput = findViewById(R.id.search_query_input);
        resultsList = findViewById(R.id.search_results_list);
        emptyText = findViewById(R.id.search_empty_text);

        Button closeBtn = findViewById(R.id.dialog_close_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setupLimitButtons();
        setupSearch();
        loadFavorites();
    }

    private void setupLimitButtons() {
        updateLimitButtonStyles();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.limit_25) {
                    currentLimit = 25;
                } else if (id == R.id.limit_50) {
                    currentLimit = 50;
                } else if (id == R.id.limit_100) {
                    currentLimit = 100;
                } else if (id == R.id.limit_all) {
                    currentLimit = 10000;
                }
                AppPrefs.saveInt(context, "limit", currentLimit);
                updateLimitButtonStyles();
                if (limitListener != null) {
                    limitListener.onLimitChanged(currentLimit);
                }
            }
        };

        btn25.setOnClickListener(clickListener);
        btn50.setOnClickListener(clickListener);
        btn100.setOnClickListener(clickListener);
        btnAll.setOnClickListener(clickListener);
    }

    private void updateLimitButtonStyles() {
        int activeBg = context.getResources().getColor(R.color.btn_begin_bg);
        int activeText = context.getResources().getColor(R.color.white);
        int defaultBg = context.getResources().getColor(R.color.button_bg);
        int defaultText = context.getResources().getColor(R.color.button_text);

        btn25.setBackgroundColor(currentLimit == 25 ? activeBg : defaultBg);
        btn25.setTextColor(currentLimit == 25 ? activeText : defaultText);

        btn50.setBackgroundColor(currentLimit == 50 ? activeBg : defaultBg);
        btn50.setTextColor(currentLimit == 50 ? activeText : defaultText);

        btn100.setBackgroundColor(currentLimit == 100 ? activeBg : defaultBg);
        btn100.setTextColor(currentLimit == 100 ? activeText : defaultText);

        btnAll.setBackgroundColor(currentLimit >= 1000 ? activeBg : defaultBg);
        btnAll.setTextColor(currentLimit >= 1000 ? activeText : defaultText);
    }

    private void setupSearch() {
        adapter = new ResultsAdapter();
        resultsList.setAdapter(adapter);

        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < searchResults.size()) {
                    NamesDatabase.NameItem item = searchResults.get(position);
                    Intent i = new Intent(context, Info.class);
                    i.putExtra("name", item.name);
                    i.putExtra("gender", item.gender);
                    context.startActivity(i);
                }
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        String trimmed = query != null ? query.trim() : "";
        if (trimmed.isEmpty()) {
            searchResults.clear();
            adapter.notifyDataSetChanged();
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Type a name above to search 31,830 official SSA names.\n\nSave any name directly to your Favorites.");
            return;
        }

        List<NamesDatabase.NameItem> matches = NamesDatabase.getInstance(context).searchNames(trimmed, 60);
        searchResults.clear();
        searchResults.addAll(matches);
        adapter.notifyDataSetChanged();

        if (matches.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("No matching names for \"" + trimmed + "\" in official 2023 SSA release.\n\nCustom names cannot be added.");
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private void loadFavorites() {
        if (currentUserId == null || currentUserId.isEmpty()) return;
        FirebaseFirestore.getInstance().collection("names")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        favoriteNames.clear();
                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                String n = doc.getString("name");
                                if (n != null) favoriteNames.add(n);
                            }
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private class ResultsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return searchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return searchResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
            }

            final NamesDatabase.NameItem item = searchResults.get(position);

            TextView nameView = convertView.findViewById(R.id.result_name);
            TextView badgeView = convertView.findViewById(R.id.result_gender_badge);
            TextView detailView = convertView.findViewById(R.id.result_detail);
            final ImageButton starBtn = convertView.findViewById(R.id.result_star_button);

            nameView.setText(item.name);

            boolean isBoy = "M".equalsIgnoreCase(item.gender);
            badgeView.setText(isBoy ? "BOY" : "GIRL");
            badgeView.setBackgroundColor(context.getResources().getColor(isBoy ? R.color.boy : R.color.girl));

            detailView.setText(String.format("#%,d • %,d babies", item.rank, item.count));

            final boolean isFavorite = favoriteNames.contains(item.name);
            if (isFavorite) {
                starBtn.setColorFilter(context.getResources().getColor(R.color.save));
            } else {
                starBtn.setColorFilter(context.getResources().getColor(R.color.gray));
            }

            starBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUserId == null) {
                        Toast.makeText(context, "Please log in to save favorites.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (favoriteNames.contains(item.name)) {
                        favoriteNames.remove(item.name);
                        notifyDataSetChanged();
                        // Delete from Firestore
                        FirebaseFirestore.getInstance().collection("names")
                                .whereEqualTo("userId", currentUserId)
                                .whereEqualTo("name", item.name)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                            queryDocumentSnapshots.getDocuments().get(0).getReference().delete();
                                        }
                                    }
                                });
                    } else {
                        favoriteNames.add(item.name);
                        notifyDataSetChanged();
                        // Add to Firestore
                        Map<String, Object> data = new HashMap<>();
                        data.put("userId", currentUserId);
                        data.put("name", item.name);
                        data.put("gender", item.gender);
                        FirebaseFirestore.getInstance().collection("names").add(data);
                        Toast.makeText(context, "Saved " + item.name + " to Favorites", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return convertView;
        }
    }
}
