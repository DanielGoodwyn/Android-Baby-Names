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

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

     if (FirebaseAuth.getInstance().getCurrentUser() != null) {
          currentAppUser = AppPrefs.getString(this, "username", "");
          currentAppUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
     }
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
     final Button nameButton = (Button)findViewById(R.id.name);
     nameButton.setText("Loading...");
     nameButton.setEnabled(false);
     DatabaseQuery<DatabaseObject> q = DatabaseQuery.getQuery("Name");
     
     q.findInBackground(new DatabaseQuery.FindCallback<DatabaseObject>() {
          public void done(List<DatabaseObject> objectList, Exception e) {
               if (e == null) {
                    List<DatabaseObject> filtered = new ArrayList<>();
                    for (int i = 0; i < objectList.size(); i++) {
                         DatabaseObject obj = objectList.get(i);
                         String objGender = obj.getString("gender");
                         String objName = obj.getString("name");
                         if (objName == null) continue;
                         
                         boolean genderMatch = genderString.equals("All") || (objGender != null && objGender.equals(genderString));
                         boolean letterMatch = letterString == null || objName.toUpperCase().startsWith(letterString.toUpperCase());
                         
                         if (genderMatch && letterMatch) {
                              filtered.add(obj);
                         }
                    }
                    
                    Collections.sort(filtered, new Comparator<DatabaseObject>() {
                         public int compare(DatabaseObject a, DatabaseObject b) {
                              if (sortString.equals("uncommon")) {
                                   return b.getInt("rank") - a.getInt("rank");
                              } else if (sortString.equals("popular")) {
                                   return a.getInt("rank") - b.getInt("rank");
                              } else if (sortString.equals("namesZtoA")) {
                                   return b.getString("name").compareToIgnoreCase(a.getString("name"));
                              } else { // namesAtoZ
                                   return a.getString("name").compareToIgnoreCase(b.getString("name"));
                              }
                         }
                    });
                    
                    int limit = Math.min(limitInt, filtered.size());
                    for (int i = 0; i < limit; i++) {
                         names.add(filtered.get(i).getString("name"));
                         genders.add(filtered.get(i).getString("gender"));
                    }
               } else {
                    Log.d("object", "Error: " + e.getMessage());
               }
               pickRandomName();
               nameButton.setEnabled(true);
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
               currentGenderString = (position < genders.size() && genders.get(position) != null) ? genders.get(position) : "M";

               updateScreen();

               namesAdapter.notifyDataSetChanged();

               populateNamesListView();

               ListView LVNames = (ListView) findViewById(R.id.namesListView);
               LVNames.setSelection(position);
               LVNames.setSelectionFromTop(position, (screenSize/2)-70);

               AppPrefs.saveString(NewNames.this, "name", nameString);
               AppPrefs.saveString(NewNames.this, "gender", genderString);

               Button save = (Button)findViewById(R.id.save);
               save.setTextColor(getResources().getColor(R.color.black));
               if (currentGenderString != null && currentGenderString.equals("F")) {
                    save.setBackgroundColor(getResources().getColor(R.color.girl));
               } else if (currentGenderString != null && currentGenderString.equals("M")) {
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

     AppPrefs.saveString(this, "lastPage", "NewNames");
     AppPrefs.saveString(this, "gender", genderString);
     AppPrefs.saveString(this, "letter", letterString);
     AppPrefs.saveString(this, "name", nameString);
     AppPrefs.saveInt(this, "limit", limitInt);

     Button save = (Button)findViewById(R.id.save);
     save.setTextColor(getResources().getColor(R.color.black));
}

public void updateMyState() {
     genderString = AppPrefs.getString(this, "gender", "M");
     letterString = AppPrefs.getString(this, "letter", "A");
     nameString = AppPrefs.getString(this, "name", "Adam");
     sortString = AppPrefs.getString(this, "sort", "popular");
     limitInt = AppPrefs.getInt(this, "limit", 25);
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
     DatabaseQuery<DatabaseObject> q = DatabaseQuery.getQuery("names");
     q.whereEqualTo("name", nameString);
     q.setLimit(1);
     q.findInBackground(new DatabaseQuery.FindCallback<DatabaseObject>() {
          public void done(List<DatabaseObject> objectList, Exception e) {
               Button save = (Button)findViewById(R.id.save);
               if (e == null) {
                    if (objectList.size()==0) {
                         DatabaseObject object = new DatabaseObject("names");
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
     AppPrefs.saveString(this, "name", nameString);
     AppPrefs.saveString(this, "letter", letterString);
     AppPrefs.saveString(this, "gender", genderString);
}

public void pickRandomName() {
     if (names == null || names.size() == 0) {
          nameString = "No names found";
          currentGenderString = genderString;
          updateScreen();
          return;
     }
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
     AppPrefs.saveString(this, "lastPage", "NewNames");
}

}
