package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyNames extends Activity
{

String currentAppUser;
String currentAppUserId;
String genderString;
String letterString;
String nameString;
String sortString;
int limitInt;
List<String> names = new ArrayList<String>();
List<String> genders = new ArrayList<String>();
int screenSize;

@Override
protected void onCreate(Bundle b)
{
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     super.onCreate(b);
     setContentView(R.layout.my_names);

     ParseUser currentUser = ParseUser.getCurrentUser();
     currentAppUser = currentUser.getUsername();
     currentAppUserId = currentUser.getObjectId();
     nameString = "Loading..";
     updateMyState();
     updateScreen();
     getNames(genderString);

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

private void getNames(String gender) {
     names.clear();
     genders.clear();
     final Button name = (Button)findViewById(R.id.name);
     final Button random = (Button)findViewById(R.id.random);
     name.setText("Loading...");
     name.setEnabled(false);
     random.setEnabled(false);
     ParseQuery<ParseObject> q = ParseQuery.getQuery("names");
     q.whereEqualTo("userId", currentAppUserId);
     if (gender!="All"&&gender!=null) {
          q.whereEqualTo("gender", gender);
     }
     q.whereMatches("name", letterString + ".*");
     if (sortString.equals("newest")) {
          q.orderByDescending("createdAt");
     } else if (sortString.equals("oldest")) {
          q.orderByAscending("createdAt");
     } else if (sortString.equals("namesZtoA")) {
          q.orderByDescending("name");
     } else {
          q.orderByAscending("name");
     }
     q.setLimit(limitInt);
     q.findInBackground(new FindCallback<ParseObject>() {
          public void done(List<ParseObject> objectList, ParseException e) {
               if (e == null) {
                    for (int i = 0;i<objectList.size();i++) {
                         names.add(i, objectList.get(i).getString("name"));
                         genders.add(i, objectList.get(i).getString("gender"));
                    }
               } else {
                    Log.d("object", "Error: " + e.getMessage());
               }
               if (names.size()>0) {
                    name.setEnabled(true);
                    random.setEnabled(true);
                    pickRandomName();
               } else {
                    name.setEnabled(false);
                    random.setEnabled(false);
                    if (genderString.equals("F")) {
                         name.setText("No girl " + letterString +  " names saved yet.");
                    } else if (genderString.equals("M")) {
                         name.setText("No boy " + letterString +  " names saved yet.");
                    } else {
                         name.setText("No " + letterString +  " names saved yet.");
                    }
               }
          }
     });
}

NamesArrayAdapter namesAdapter;
int selectedItem;

public void populateNamesListView()
{
     String[] namesArray = names.toArray(new String[names.size()]);
     String[] gendersArray = genders.toArray(new String[genders.size()]);

     Button nameButton = (Button) findViewById(R.id.name);

     namesAdapter = new NamesArrayAdapter(this, namesArray, gendersArray, selectedItem, nameButton);
     final ListView LVNames = (ListView) findViewById(R.id.namesListView);
     LVNames.setAdapter(namesAdapter);

     LVNames.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
               TextView nameCell = (TextView)view.findViewById(R.id.name_cell);
               nameCell.setSelected(true);
               String nameCellText = nameCell.getText().toString();
               Button name = (Button) findViewById(R.id.name);
               name.setText(nameCellText);

               selectedItem = position;
               nameString = names.get(position);
               genderString = genders.get(position);

               updateScreen();

               namesAdapter.notifyDataSetChanged();

               populateNamesListView();

               ListView LVNames = (ListView) findViewById(R.id.namesListView);
               LVNames.setSelection(position);
               LVNames.setSelectionFromTop(position, (screenSize/2)-70);

               ParseObject object = new ParseObject("_user");
               object.put("userId", currentAppUserId);
               object.put("name", nameString);
               object.put("gender", genderString);
               object.saveInBackground();

               Button delete = (Button)findViewById(R.id.delete);
               delete.setTextColor(getResources().getColor(R.color.black));
          }
     });
}

public void updateScreen() {
     Button name = (Button)findViewById(R.id.name);
     name.setText(nameString);

     View bg = findViewById(R.id.bg);
     Button maleButton = (Button)findViewById(R.id.boys);
     Button femaleButton = (Button)findViewById(R.id.girls);
     Button allButton = (Button)findViewById(R.id.all);
     if (genderString.equals("M")) {
          bg.setBackgroundColor(getResources().getColor(R.color.boy));
          maleButton.setBackgroundColor(getResources().getColor(R.color.black));
          maleButton.setTextColor(getResources().getColor(R.color.boy));
          femaleButton.setBackgroundColor(getResources().getColor(R.color.girl));
          femaleButton.setTextColor(getResources().getColor(R.color.black));
          allButton.setBackgroundColor(getResources().getColor(R.color.white));
          allButton.setTextColor(getResources().getColor(R.color.black));
     } else if (genderString.equals("F")) {
          bg.setBackgroundColor(getResources().getColor(R.color.girl));
          maleButton.setBackgroundColor(getResources().getColor(R.color.boy));
          maleButton.setTextColor(getResources().getColor(R.color.black));
          femaleButton.setBackgroundColor(getResources().getColor(R.color.black));
          femaleButton.setTextColor(getResources().getColor(R.color.girl));
          allButton.setBackgroundColor(getResources().getColor(R.color.white));
          allButton.setTextColor(getResources().getColor(R.color.black));
     } else {
          bg.setBackgroundColor(getResources().getColor(R.color.white));
          maleButton.setBackgroundColor(getResources().getColor(R.color.boy));
          maleButton.setTextColor(getResources().getColor(R.color.black));
          femaleButton.setBackgroundColor(getResources().getColor(R.color.girl));
          femaleButton.setTextColor(getResources().getColor(R.color.black));
          allButton.setBackgroundColor(getResources().getColor(R.color.black));
          allButton.setTextColor(getResources().getColor(R.color.white));
     }

     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("lastPage", "MyNames");
     saveState.put("gender", genderString);
     saveState.put("letter", letterString);
     saveState.put("name", nameString);
     saveState.put("limit", limitInt);
     saveState.setACL(new ParseACL(ParseUser.getCurrentUser()));
     saveState.saveInBackground();

     Button delete = (Button)findViewById(R.id.delete);
     delete.setTextColor(getResources().getColor(R.color.black));
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

public void name(View v) {
     lastPage();
     Intent i = new Intent(this, daniel.babynames.Info.class);
     startActivity(i);
}

public void back(View v) {
     lastPage();
     Intent i = new Intent(this, daniel.babynames.Home.class);
     startActivity(i);
}

public void delete(View v) {
     Button delete = (Button)findViewById(R.id.delete);
     delete.setTextColor(getResources().getColor(R.color.no));
     ParseQuery<ParseObject> query = ParseQuery.getQuery("names");
     query.whereEqualTo("name", nameString);
     query.findInBackground(new FindCallback<ParseObject>() {
          public void done(List<ParseObject> objectList, ParseException e) {
               for (int i = 0; i < objectList.size(); i++) {
                    try {
                         objectList.get(i).delete();
                    } catch (ParseException exception) {
                         Button name = (Button)findViewById(R.id.name);
                         name.setText("Fail.");
                    }
                    getNames(genderString);
                    namesAdapter.notifyDataSetChanged();
               }
          }
     });
}

public void random(View v) {
     pickRandomName();
}

public void boys(View v) {
     if (genderString!="M") {
          getNames("M");
          genderString = "M";
          setMyState();
     }
}

public void all(View v) {
     if (genderString!="All") {
          getNames("All");
          genderString = "All";
          setMyState();
     }
}

public void girls(View v) {
     if (genderString!="F") {
          getNames("F");
          genderString = "F";
          setMyState();
     }
}

public void setMyState() {
     ParseObject object = new ParseObject("_user");
     object.put("user", currentAppUser);
     object.put("name", nameString);
     object.put("letter", letterString);
     object.put("gender", genderString);
     object.saveInBackground();
}

public void pickRandomName() {
     int randomNumber = rand(0,names.size()-1);
     nameString = names.get(randomNumber);
     updateScreen();
     setMyState();

     selectedItem = randomNumber;
     populateNamesListView();
     ListView LVNames = (ListView) findViewById(R.id.namesListView);
     LVNames.setSelection(randomNumber);
     LVNames.setSelectionFromTop(randomNumber, (screenSize / 2) - 70);

     Button delete = (Button)findViewById(R.id.delete);
     delete.setTextColor(getResources().getColor(R.color.black));
}

public int rand(int a, int b)
{
     return((int)((b-a+1)*Math.random() + a));
}

public void lastPage() {
     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("lastPage", "MyNames");
}

}
