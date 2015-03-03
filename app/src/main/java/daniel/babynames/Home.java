package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseACL;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class Home extends Activity
{

String genderString;
String letterString;
String nameString;
String sortString;
int limitInt;
int screenSize;

@Override
protected void onCreate(Bundle b) {
     requestWindowFeature(Window.FEATURE_NO_TITLE);

     super.onCreate(b);
     setContentView(R.layout.home);

     updateMyState();
     getAlphabet();
     setAlphabetSelection();
     createSortsSpinner();
     setSortSpinnerSelection();

     EditText limit = (EditText)findViewById(R.id.limit);
     limit.setImeActionLabel("set", KeyEvent.KEYCODE_ENTER);
     limit.setText(limitInt+"");

     updateScreen();

     Display display = getWindowManager().getDefaultDisplay();
     Point size = new Point();
     display.getSize(size);
     int width = size.x;
     int height = size.y;
     if (width<height) {
          screenSize = width;
     } else {
          screenSize = height;
     }
}

@Override
public boolean dispatchKeyEvent(KeyEvent event) {
     Log.i("key pressed", String.valueOf(event.getKeyCode()));
     updateScreen();
     return super.dispatchKeyEvent(event);
}

AlphebetArrayAdapter alphabetAdapter;
List<String> letters = new ArrayList<String>();
int alphabetSelection;

private void getAlphabet() {
     letters.clear();
     letters.add("All");
     letters.add("A");
     letters.add("B");
     letters.add("C");
     letters.add("D");
     letters.add("E");
     letters.add("F");
     letters.add("G");
     letters.add("H");
     letters.add("I");
     letters.add("J");
     letters.add("K");
     letters.add("L");
     letters.add("M");
     letters.add("N");
     letters.add("O");
     letters.add("P");
     letters.add("Q");
     letters.add("R");
     letters.add("S");
     letters.add("T");
     letters.add("U");
     letters.add("V");
     letters.add("W");
     letters.add("X");
     letters.add("Y");
     letters.add("Z");
     String[] lettersArray = letters.toArray(new String[letters.size()]);
     alphabetAdapter = new AlphebetArrayAdapter(this, lettersArray, alphabetSelection);
     final ListView alphabet = (ListView) findViewById(R.id.alphabetListView);
     alphabet.setAdapter(alphabetAdapter);
     alphabet.setOnItemClickListener(new AdapterView.OnItemClickListener()
     {
          @Override
          public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id)
          {
               alphabetSelection = position;
               TextView clickedItem = (TextView) viewClicked;
               if (clickedItem.getText().toString().equals("All")) {
                    letterString = "";
               } else {
                    letterString = clickedItem.getText().toString();
               }
               updateScreen();
               alphabetAdapter.notifyDataSetChanged();
               clickedItem.setSelected(true);
               alphabet.setSelection(position);
               alphabet.setSelectionFromTop(position, (screenSize / 2) - 70);
          }
     });
}

List<String> sorts = new ArrayList<String>();
String sortType;
SortsArrayAdapter sortsAdapter;
int sortSpinnerSelection;

public void createSortsSpinner() {
     sorts.clear();
     sorts.add("popular");
     sorts.add("uncommon");
     sorts.add("namesAtoZ");
     sorts.add("namesZtoA");
     sorts.add("newest");
     sorts.add("oldest");
     String[] sortsArray = sorts.toArray(new String[sorts.size()]);
     Spinner sortsSpinner = (Spinner) findViewById(R.id.sorts_spinner);
     sortsAdapter = new SortsArrayAdapter(this, sortsArray);
     sortsAdapter.setDropDownViewResource(R.layout.sort_cell);
     sortsSpinner.setAdapter(sortsAdapter);
     setSortSpinnerSelection();
     sortsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               sortSpinnerSelection = position;
               final ParseUser currentUser = new ParseUser().getCurrentUser();
               sortType = parent.getItemAtPosition(position).toString();
               currentUser.put("sort", sortType);
               currentUser.setACL(new ParseACL(currentUser));
               currentUser.saveInBackground();
          }
          public void onNothingSelected(AdapterView<?> parent) {}
     });
}

public void setSortSpinnerSelection() {
     sortSpinnerSelection = 0;
     for (int i = 0; i < sorts.size(); i++) {
          if (sortString.equals(sorts.get(i))) {
               sortSpinnerSelection = i;
          }
     }
     Spinner sortsSpinner = (Spinner) findViewById(R.id.sorts_spinner);
     sortsSpinner.setSelection(sortSpinnerSelection, true);
}

public void setAlphabetSelection() {
     alphabetSelection = 0;
     for (int i = 0; i < letters.size(); i++) {
          if (letterString.equals(letters.get(i))) {
               alphabetSelection = i;
          }
     }

     ListView alphabetListView = (ListView) findViewById(R.id.alphabetListView);
     alphabetListView.setSelection(alphabetSelection);
     int alphabetSelectionPosition = alphabetListView.getSelectedItemPosition();
     alphabetListView.smoothScrollToPosition(alphabetSelectionPosition);
     alphabetAdapter.notifyDataSetChanged();
}

public void boys(View v) {
     genderString = "M";
     updateScreen();
}

public void girls(View v) {
     genderString = "F";
     updateScreen();
}

public void all(View v) {
     genderString = "All";
     updateScreen();
}

public void account(View v) {
     lastPage();
     updateScreen();
     Intent i = new Intent(this, Account.class);
     finish();
     startActivity(i);
}

public void myList(View v) {
     lastPage();
     updateScreen();
     Intent i = new Intent(this, MyNames.class);
     finish();
     startActivity(i);
}

public void names(View v) {
     lastPage();
     updateScreen();
     Intent i = new Intent(this, daniel.babynames.NewNames.class);
     finish();
     startActivity(i);
}

public void getLimit() {
     EditText limit = (EditText) findViewById(R.id.limit);
     int limitNumber;
     if (limit.getText().toString().equals("")) {
          limitNumber = 1;
     } else {
          limitNumber = (Integer.parseInt(limit.getText().toString()));
     }
     if (limitNumber < 2) {
          limitInt = 1;
     } else if (limitNumber > 1000) {
          limitInt = 1000;
     } else {
          limitInt = limitNumber;
     }
}

public void updateScreen() {
     View bg = findViewById(R.id.bg);
     Button maleButton = (Button)findViewById(R.id.boys);
     Button femaleButton = (Button)findViewById(R.id.girls);
     Button allButton = (Button)findViewById(R.id.all);
     TextView homePage = (TextView)findViewById(R.id.homePage);
     getLimit();
     String space = " ";
     if (letterString.equals("")) {
          space = "";
     }
     if (genderString.equals("M")) {
          bg.setBackgroundColor(getResources().getColor(R.color.boy));
          maleButton.setBackgroundColor(getResources().getColor(R.color.black));
          maleButton.setTextColor(getResources().getColor(R.color.boy));
          femaleButton.setBackgroundColor(getResources().getColor(R.color.girl));
          femaleButton.setTextColor(getResources().getColor(R.color.black));
          allButton.setBackgroundColor(getResources().getColor(R.color.white));
          allButton.setTextColor(getResources().getColor(R.color.black));
          homePage.setText(limitInt + " Boy " + letterString + space + "Names");
     } else if (genderString.equals("F")) {
          bg.setBackgroundColor(getResources().getColor(R.color.girl));
          maleButton.setBackgroundColor(getResources().getColor(R.color.boy));
          maleButton.setTextColor(getResources().getColor(R.color.black));
          femaleButton.setBackgroundColor(getResources().getColor(R.color.black));
          femaleButton.setTextColor(getResources().getColor(R.color.girl));
          allButton.setBackgroundColor(getResources().getColor(R.color.white));
          allButton.setTextColor(getResources().getColor(R.color.black));
          homePage.setText(limitInt + " Girl " + letterString + space + "Names");
     } else {
          bg.setBackgroundColor(getResources().getColor(R.color.white));
          maleButton.setBackgroundColor(getResources().getColor(R.color.boy));
          maleButton.setTextColor(getResources().getColor(R.color.black));
          femaleButton.setBackgroundColor(getResources().getColor(R.color.girl));
          femaleButton.setTextColor(getResources().getColor(R.color.black));
          allButton.setBackgroundColor(getResources().getColor(R.color.black));
          allButton.setTextColor(getResources().getColor(R.color.white));
          homePage.setText("All " + letterString + space + "Names (" + limitInt +  ")");
     }
     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("gender", genderString);
     saveState.put("letter", letterString);
     saveState.put("name", nameString);
     saveState.put("limit", limitInt);
     saveState.setACL(new ParseACL(ParseUser.getCurrentUser()));
     saveState.saveInBackground();
}

public void updateMyState() {
     ParseUser currentUser = ParseUser.getCurrentUser();
     if (currentUser != null) {
          genderString = currentUser.getString("gender");
          letterString = currentUser.getString("letter");
          nameString = currentUser.getString("name");
          sortString = currentUser.getString("sort");
          limitInt = currentUser.getInt("limit");
     }
}

public void lastPage() {
     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("lastPage", "Home");
}

}
