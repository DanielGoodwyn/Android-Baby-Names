package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

     ParseUser currentUser = ParseUser.getCurrentUser();
     TextView name = (TextView)findViewById(R.id.name);
     if (currentUser != null) {
          currentAppUser = currentUser.getUsername();
          currentAppUserId = currentUser.getObjectId();
          lastPage = currentUser.getString("lastPage");
          genderString = currentUser.getString("gender");
          letterString = currentUser.getString("letter");
          nameString = currentUser.getString("name");
          limitInt = currentUser.getInt("limit");
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
          webView.loadUrl("http://www.behindthename.com/name/" + nameString.toLowerCase());
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
     ParseQuery<ParseObject> q = ParseQuery.getQuery("names");
     q.whereEqualTo("name", nameString);
     q.setLimit(1);
     q.findInBackground(new FindCallback<ParseObject>() {
          public void done(List<ParseObject> objectList, ParseException e) {
               TextView save = (TextView)findViewById(R.id.save);
               TextView delete = (TextView)findViewById(R.id.delete);
               TextView name = (TextView)findViewById(R.id.name);
               if (e == null) {
                    if (objectList.size()==0) {
                         ParseObject object = new ParseObject("names");
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
     ParseQuery<ParseObject> query = ParseQuery.getQuery("names");
     query.whereEqualTo("name", nameString);
     query.findInBackground(new FindCallback<ParseObject>() {
          public void done(List<ParseObject> objectList, ParseException e) {
               for (int i = 0; i < objectList.size(); i++) {
                    TextView save = (TextView)findViewById(R.id.save);
                    TextView delete = (TextView)findViewById(R.id.delete);
                    TextView name = (TextView)findViewById(R.id.name);
                    try {
                         objectList.get(i).delete();
                         save.setAlpha(1);
                         delete.setAlpha(0);
                         name.setAlpha((float) .5);
                    } catch (ParseException exception) {
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
     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("lastPage", "Info");
}

}
