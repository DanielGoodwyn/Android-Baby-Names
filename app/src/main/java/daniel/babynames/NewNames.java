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

public class NewNames extends Activity
{

String currentAppUser;
String currentAppUserId;
String genderString;
String currentGenderString;
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
     setContentView(R.layout.new_names);

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
     name.setText("Loading...");
     name.setEnabled(false);
     ParseQuery<ParseObject> q = ParseQuery.getQuery("name");
     if (gender!="All"&&gender!=null) {
          q.whereEqualTo("gender", gender);
     }
     if (sortString.equals("namesAtoZ")) {
          q.orderByAscending("name");
     } else if (sortString.equals("namesZtoA")) {
          q.orderByDescending("name");
     } else if (sortString.equals("uncommon")) {
          q.orderByDescending("rank");
     } else {
          q.orderByAscending("rank");
     }
     q.whereMatches("name", letterString + ".*");
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
               pickRandomName();
               name.setEnabled(true);
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

     LVNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
               TextView nameCell = (TextView)view.findViewById(R.id.name_cell);
               nameCell.setSelected(true);
               String nameCellText = nameCell.getText().toString();
               Button name = (Button) findViewById(R.id.name);
               name.setText(nameCellText);

               selectedItem = position;
               nameString = names.get(position);
               currentGenderString = genders.get(position);

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

               Button save = (Button)findViewById(R.id.save);
               save.setTextColor(getResources().getColor(R.color.black));
               if (currentGenderString.equals('F')) {
                    save.setBackgroundColor(getResources().getColor(R.color.girl));
               } else if (currentGenderString.equals('M')) {
                    save.setBackgroundColor(getResources().getColor(R.color.boy));
               }
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

     Button save = (Button)findViewById(R.id.save);
     save.setTextColor(getResources().getColor(R.color.black));
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

public void myList(View v) {
     lastPage();
     updateScreen();
     Intent i = new Intent(this, MyNames.class);
     finish();
     startActivity(i);
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

public void save(View v) {
     ParseQuery<ParseObject> q = ParseQuery.getQuery("names");
     q.whereEqualTo("name", nameString);
     q.setLimit(1);
     q.findInBackground(new FindCallback<ParseObject>() {
          public void done(List<ParseObject> objectList, ParseException e) {
               Button save = (Button)findViewById(R.id.save);
               if (e == null) {
                    if (objectList.size()==0) {
                         ParseObject object = new ParseObject("names");
                         object.put("userId", currentAppUserId);
                         object.put("name", nameString);
                         object.put("gender", currentGenderString);
                         object.saveInBackground();
                         save.setTextColor(getResources().getColor(R.color.yes));
                    } else {
                         save.setTextColor(getResources().getColor(R.color.no));
                    }
               } else {
                    save.setText("Fail");
                    save.setTextColor(getResources().getColor(R.color.no));
               }
          }
     });

     Button save = (Button)findViewById(R.id.save);
     save.setTextColor(getResources().getColor(R.color.black));
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
     currentGenderString = genders.get(randomNumber);
     updateScreen();
     setMyState();

     selectedItem = randomNumber;
     populateNamesListView();
     ListView LVNames = (ListView) findViewById(R.id.namesListView);
     LVNames.setSelection(randomNumber);
     LVNames.setSelectionFromTop(randomNumber, (screenSize / 2) - 70);

     Button save = (Button)findViewById(R.id.save);
     save.setTextColor(getResources().getColor(R.color.black));
}

public int rand(int a, int b)
{
     return((int)((b-a+1)*Math.random() + a));
}

public void lastPage() {
     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("lastPage", "NewNames");
}

}
