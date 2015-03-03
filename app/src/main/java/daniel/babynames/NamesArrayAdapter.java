package daniel.babynames;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class NamesArrayAdapter extends ArrayAdapter<String>
{
private final Context context;
private final String[] names;
private final String[] genders;
private final int selectedItem;
private final Button nameButton;

public NamesArrayAdapter(Context context, String[] names, String[] genders, int selectedItem, Button nameButton)
{
     super(context, R.layout.name_cell, names);
     this.context = context;
     this.names = names;
     this.genders = genders;
     this.selectedItem = selectedItem;
     this.nameButton = nameButton;
}

@Override
public View getView(int position, View convertView, ViewGroup parent)
{
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     View rowView = inflater.inflate(R.layout.name_cell, parent, false);
     TextView textView = (TextView) rowView.findViewById(R.id.name_cell);
     textView.setText(names[position]);
     if (genders[position].equals("F")) {
          rowView.setBackgroundColor(getContext().getResources().getColor(R.color.girl));
     } else if (genders[position].equals("M"))
     {
          rowView.setBackgroundColor(getContext().getResources().getColor(R.color.boy));
     } else {
          rowView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
     }
     if (rowView.isSelected() || position == selectedItem) {
          if (genders[position].equals("F")) {
               rowView.setBackgroundColor(getContext().getResources().getColor(R.color.girls));
          } else if (genders[position].equals("M"))
          {
               rowView.setBackgroundColor(getContext().getResources().getColor(R.color.boys));
          } else
          {
               rowView.setBackgroundColor(getContext().getResources().getColor(R.color.gray));
          }
     }
     return rowView;
}
}
