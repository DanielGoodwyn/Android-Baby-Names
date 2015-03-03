package daniel.babynames;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AlphebetArrayAdapter extends ArrayAdapter<String>
{
private final Context context;
private final String[] lettersArray;
private final int alphabetSelection;

public AlphebetArrayAdapter(Context context, String[] lettersArray, int alphabetSelection)
{
     super(context, R.layout.letter, lettersArray);
     this.context = context;
     this.lettersArray = lettersArray;
     this.alphabetSelection = alphabetSelection;
}

@Override
public View getView(int position, View convertView, ViewGroup parent)
{
     LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     View rowView = inflater.inflate(R.layout.name_cell, parent, false);
     TextView textView = (TextView) rowView.findViewById(R.id.name_cell);
     textView.setText(lettersArray[position]);
     return rowView;
}
}
