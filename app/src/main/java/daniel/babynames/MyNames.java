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

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
     final Button name = (Button)findViewById(R.id.name);
     final Button random = (Button)findViewById(R.id.random);
     name.setText("Loading...");
     name.setEnabled(false);
     random.setEnabled(false);
     DatabaseQuery<DatabaseObject> q = DatabaseQuery.getQuery("names");
     q.whereEqualTo("userId", currentAppUserId);
     q.setLimit(1000);
     q.findInBackground(new DatabaseQuery.FindCallback<DatabaseObject>() {
          public void done(List<DatabaseObject> objectList, Exception e) {
               boolean isM = "M".equalsIgnoreCase(genderString);
               boolean isF = "F".equalsIgnoreCase(genderString);
               boolean hasLetter = (letterString != null && !letterString.isEmpty() && !letterString.equalsIgnoreCase("ALL") && !letterString.equals("*"));

               if (e == null && objectList != null) {
                    List<DatabaseObject> filtered = new ArrayList<>();
                    for (DatabaseObject obj : objectList) {
                         String objGender = obj.getString("gender");
                         String objName = obj.getString("name");
                         if (objName == null) continue;

                         boolean genderMatch = (!isM && !isF) || (isM && "M".equalsIgnoreCase(objGender)) || (isF && "F".equalsIgnoreCase(objGender));
                         boolean letterMatch = !hasLetter || objName.toUpperCase().startsWith(letterString.toUpperCase());

                         if (genderMatch && letterMatch) {
                              filtered.add(obj);
                         }
                    }

                    Collections.sort(filtered, new Comparator<DatabaseObject>() {
                         @Override
                         public int compare(DatabaseObject a, DatabaseObject b) {
                              if (sortString.equals("newest")) {
                                   Date da = a.getCreatedAt();
                                   Date db = b.getCreatedAt();
                                   if (da != null && db != null) return db.compareTo(da);
                                   return 0;
                              } else if (sortString.equals("oldest")) {
                                   Date da = a.getCreatedAt();
                                   Date db = b.getCreatedAt();
                                   if (da != null && db != null) return da.compareTo(db);
                                   return 0;
                              } else if (sortString.equals("namesZtoA")) {
                                   return b.getString("name").compareToIgnoreCase(a.getString("name"));
                              } else { // namesAtoZ or popular
                                   return a.getString("name").compareToIgnoreCase(b.getString("name"));
                              }
                         }
                    });

                    int limit = Math.min(limitInt, filtered.size());
                    for (int i = 0; i < limit; i++) {
                         names.add(filtered.get(i).getString("name"));
                         genders.add(filtered.get(i).getString("gender"));
                    }
               } else if (e != null) {
                    Log.d("object", "Error: " + e.getMessage());
               }

               if (names.size() > 0) {
                    name.setEnabled(true);
                    random.setEnabled(true);
                    pickRandomName();
               } else {
                    name.setEnabled(false);
                    random.setEnabled(false);
                    String displayLetter = hasLetter ? (letterString + " ") : "";
                    if ("F".equalsIgnoreCase(genderString)) {
                         name.setText("No girl " + displayLetter + "names saved yet.");
                    } else if ("M".equalsIgnoreCase(genderString)) {
                         name.setText("No boy " + displayLetter + "names saved yet.");
                    } else {
                         name.setText("No " + displayLetter + "names saved yet.");
                    }
                    if (namesAdapter != null) {
                         namesAdapter.notifyDataSetChanged();
                    }
                    populateNamesListView();
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
               genderString = (position < genders.size() && genders.get(position) != null) ? genders.get(position) : "M";

               updateScreen();

               namesAdapter.notifyDataSetChanged();

               populateNamesListView();

               ListView LVNames = (ListView) findViewById(R.id.namesListView);
               LVNames.setSelection(position);
               LVNames.setSelectionFromTop(position, (screenSize/2)-70);

               AppPrefs.saveString(MyNames.this, "name", nameString);
               AppPrefs.saveString(MyNames.this, "gender", genderString);

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
     if (genderString == null || (!genderString.equals("M") && !genderString.equals("F"))) {
          genderString = "All";
     }
     if (letterString == null) {
          letterString = "";
     }
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

     AppPrefs.saveString(this, "lastPage", "MyNames");
     AppPrefs.saveString(this, "gender", genderString);
     AppPrefs.saveString(this, "letter", letterString);
     AppPrefs.saveString(this, "name", nameString);
     AppPrefs.saveInt(this, "limit", limitInt);

     Button delete = (Button)findViewById(R.id.delete);
     delete.setTextColor(getResources().getColor(R.color.delete_text));
}

public void updateMyState() {
     genderString = AppPrefs.getString(this, "gender", "All");
     letterString = AppPrefs.getString(this, "letter", "");
     nameString = AppPrefs.getString(this, "name", "Adam");
     sortString = AppPrefs.getString(this, "sort", "popular");
     limitInt = AppPrefs.getInt(this, "limit", 25);
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
     DatabaseQuery<DatabaseObject> query = DatabaseQuery.getQuery("names");
     query.whereEqualTo("userId", currentAppUserId);
     query.findInBackground(new DatabaseQuery.FindCallback<DatabaseObject>() {
          public void done(List<DatabaseObject> objectList, Exception e) {
               if (objectList != null) {
                    for (int i = 0; i < objectList.size(); i++) {
                         DatabaseObject obj = objectList.get(i);
                         if (nameString != null && nameString.equalsIgnoreCase(obj.getString("name"))) {
                              try {
                                   obj.deleteInBackground();
                              } catch (Exception exception) {
                                   Button name = (Button)findViewById(R.id.name);
                                   name.setText("Fail.");
                              }
                         }
                    }
               }
               getNames(genderString);
               if (namesAdapter != null) {
                    namesAdapter.notifyDataSetChanged();
               }
          }
     });
}

public void random(View v) {
     pickRandomName();
}

public void boys(View v) {
     if (!"M".equals(genderString)) {
          genderString = "M";
          setMyState();
          updateScreen();
          getNames("M");
     }
}

public void all(View v) {
     if (!"All".equals(genderString)) {
          genderString = "All";
          setMyState();
          updateScreen();
          getNames("All");
     }
}

public void girls(View v) {
     if (!"F".equals(genderString)) {
          genderString = "F";
          setMyState();
          updateScreen();
          getNames("F");
     }
}

public void setMyState() {
     AppPrefs.saveString(this, "name", nameString);
     AppPrefs.saveString(this, "letter", letterString);
     AppPrefs.saveString(this, "gender", genderString);
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
     delete.setTextColor(getResources().getColor(R.color.delete_text));
}

public int rand(int a, int b)
{
     return((int)((b-a+1)*Math.random() + a));
}

public void lastPage() {
     AppPrefs.saveString(this, "lastPage", "MyNames");
}

}
