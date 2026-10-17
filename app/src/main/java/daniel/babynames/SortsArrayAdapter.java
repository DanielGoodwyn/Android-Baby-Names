package daniel.babynames;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.widget.TextViewCompat;

public class SortsArrayAdapter extends ArrayAdapter<String>
{
private final Context context;
private final String[] sorts;

public SortsArrayAdapter(Context context, String[] sorts)
{
     super(context, R.layout.sort_cell, sorts);
     this.context = context;
     this.sorts = sorts;
}

public static String getDisplayName(String sortKey) {
    if (sortKey == null) return "Popular";
    switch (sortKey) {
        case "popular": return "Popular";
        case "uncommon": return "Uncommon";
        case "namesAtoZ": return "A - Z";
        case "namesZtoA": return "Z - A";
        case "newest": return "Newest";
        case "oldest": return "Oldest";
        default: return sortKey;
    }
}

@Override
public View getView(int position, View convertView, ViewGroup parent)
{
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     View rowView = inflater.inflate(R.layout.sort_cell, parent, false);
     TextView textView = (TextView) rowView.findViewById(R.id.sort_cell_text);
     if (textView != null) {
          textView.setText(getDisplayName(sorts[position]));
          textView.setTextColor(getContext().getResources().getColor(R.color.text_primary));
          TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView, 10, 16, 1, TypedValue.COMPLEX_UNIT_SP);
          textView.setPadding(4, 4, 4, 4);
     }
     rowView.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
     return rowView;
}

@Override
public View getDropDownView(int position, View convertView, ViewGroup parent) {
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     View rowView = inflater.inflate(R.layout.sort_cell, parent, false);
     TextView textView = (TextView) rowView.findViewById(R.id.sort_cell_text);
     if (textView != null) {
          textView.setText(getDisplayName(sorts[position]));
          textView.setTextColor(getContext().getResources().getColor(R.color.text_primary));
          TextViewCompat.setAutoSizeTextTypeWithDefaults(textView, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
          textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
          textView.setPadding(16, 20, 16, 20);
     }
     rowView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
     return rowView;
}
}
