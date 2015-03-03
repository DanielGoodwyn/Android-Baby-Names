package daniel.babynames;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SortsArrayAdapter extends ArrayAdapter<String>
{
private final Context context;
private final String[] sorts;

public SortsArrayAdapter(Context context, String[] sorts)
{
     super(context, R.layout.name_cell, sorts);
     this.context = context;
     this.sorts = sorts;
}

@Override
public View getView(int position, View convertView, ViewGroup parent)
{
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     View rowView = inflater.inflate(R.layout.name_cell, parent, false);
     TextView textView = (TextView) rowView.findViewById(R.id.name_cell);
     textView.setText(sorts[position]);
     rowView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
     return rowView;
}
}
