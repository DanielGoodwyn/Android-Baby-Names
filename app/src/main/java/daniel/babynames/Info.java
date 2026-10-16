package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class Info extends Activity
{

String lastPage;
String currentAppUser;
String currentAppUserId;
String genderString;
String letterString;
String nameString;
int limitInt;

@Override
protected void onCreate(Bundle b) {
     requestWindowFeature(Window.FEATURE_NO_TITLE);

     super.onCreate(b);
     setContentView(R.layout.info);

     TextView name = (TextView)findViewById(R.id.name);
     if (FirebaseAuth.getInstance().getCurrentUser() != null) {
          currentAppUser = AppPrefs.getString(this, "username", "");
          currentAppUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
          lastPage = AppPrefs.getString(this, "lastPage", "Home");
          genderString = AppPrefs.getString(this, "gender", "M");
          letterString = AppPrefs.getString(this, "letter", "A");
          nameString = AppPrefs.getString(this, "name", "Adam");
          limitInt = AppPrefs.getInt(this, "limit", 25);
          name.setText(nameString);
          setGenderColor();
          if (lastPage.equals("MyNames")) {
               Button save = (Button)findViewById(R.id.save);
               save.setAlpha(0);
          } else if (lastPage.equals("NewNames")) {
               Button delete = (Button) findViewById(R.id.delete);
               delete.setAlpha(0);
          }
          WebView webView = (WebView) findViewById(R.id.webView);
          webView.loadUrl("https://www.behindthename.com/name/" + nameString.toLowerCase());
     } else {
          lastPage = "Home";
     }
}

public void back(View v) {
     lastPage();
     Intent i;
     if (lastPage.equals("MyNames")) {
          i = new Intent(this, daniel.babynames.MyNames.class);
     } else if (lastPage.equals("NewNames")) {
          i = new Intent(this, daniel.babynames.NewNames.class);
     } else {
          i = new Intent(this, daniel.babynames.Home.class);
     }
     startActivity(i);
}

public void myList(View v) {
     lastPage();
     Intent i = new Intent(this, MyNames.class);
     finish();
     startActivity(i);
}

public void save(View v) {
     DatabaseQuery<DatabaseObject> q = DatabaseQuery.getQuery("names");
     q.whereEqualTo("name", nameString);
     q.setLimit(1);
     q.findInBackground(new DatabaseQuery.FindCallback<DatabaseObject>() {
          public void done(List<DatabaseObject> objectList, Exception e) {
               TextView save = (TextView)findViewById(R.id.save);
               TextView delete = (TextView)findViewById(R.id.delete);
               TextView name = (TextView)findViewById(R.id.name);
               if (e == null) {
                    if (objectList.size()==0) {
                         DatabaseObject object = new DatabaseObject("names");
                         object.put("userId", currentAppUserId);
                         object.put("name", nameString);
                         object.put("gender", genderString);
                         object.saveInBackground();
                    }
                    save.setAlpha(0);
                    delete.setAlpha(1);
                    name.setAlpha(1);
               } else {
                    save.setText("Fail");
                    save.setTextColor(getResources().getColor(R.color.no));
               }
          }
     });
}

public void delete(View v) {
     DatabaseQuery<DatabaseObject> query = DatabaseQuery.getQuery("names");
     query.whereEqualTo("name", nameString);
     query.findInBackground(new DatabaseQuery.FindCallback<DatabaseObject>() {
          public void done(List<DatabaseObject> objectList, Exception e) {
               for (int i = 0; i < objectList.size(); i++) {
                    TextView save = (TextView)findViewById(R.id.save);
                    TextView delete = (TextView)findViewById(R.id.delete);
                    TextView name = (TextView)findViewById(R.id.name);
                    try {
                         objectList.get(i).deleteInBackground();
                         save.setAlpha(1);
                         delete.setAlpha(0);
                         name.setAlpha((float) .5);
                    } catch (Exception exception) {
                         delete.setText("Fail.");
                    }
               }
          }
     });
}

public void setGenderColor() {
     View bg = findViewById(R.id.bg);
     if (genderString.equals("F")) {
          bg.setBackgroundColor(getResources().getColor(R.color.girl));
     } else {
          bg.setBackgroundColor(getResources().getColor(R.color.boy));
     }
}

public void lastPage() {
     AppPrefs.saveString(this, "lastPage", "Info");
}

}
