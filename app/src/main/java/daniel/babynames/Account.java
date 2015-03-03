package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class Account extends Activity {

String genderString;
String letterString;
String nameString;
int limitInt;
String fbString;

@Override
protected void onCreate(Bundle b) {
     requestWindowFeature(Window.FEATURE_NO_TITLE);

     super.onCreate(b);
     setContentView(R.layout.account);

     ParseUser currentUser = ParseUser.getCurrentUser();
     TextView usernameTV = (TextView)findViewById(R.id.username);
     TextView emailTV = (TextView)findViewById(R.id.email);
     TextView user = (TextView)findViewById(R.id.user);
     if (currentUser != null) {
          usernameTV.setText(currentUser.getUsername());
          emailTV.setText(currentUser.getEmail());
          user.setText(currentUser.getUsername());
          genderString = currentUser.getString("gender");
          letterString = currentUser.getString("letter");
          nameString = currentUser.getString("name");
          limitInt = currentUser.getInt("limit");

     } else {
          user.setText("no user");
          user.setTextColor(getResources().getColor(R.color.no));
     }

     setfbString();
     updateScreen();
}

@Override
public boolean dispatchKeyEvent(KeyEvent event) {
     updateScreen();
     return super.dispatchKeyEvent(event);
}

public void signup(View v) {
     signup();
}

public void signup() {
     final TextView label = (TextView)findViewById(R.id.user);
     EditText username = (EditText)findViewById(R.id.username);
     EditText email = (EditText)findViewById(R.id.email);
     EditText password = (EditText)findViewById(R.id.password);
     final ParseUser user = new ParseUser();
     user.setUsername(username.getText().toString().toLowerCase());
     user.setEmail(email.getText().toString().toLowerCase());
     user.setPassword(password.getText().toString());
     user.put("gender", "M");
     user.put("letter", "A");
     user.put("name", "Adam");
     user.put("limit", 25);
     user.put("sort", "popular");
     user.signUpInBackground(new SignUpCallback() {
          public void done(ParseException e) {
               if (e == null) {
                    label.setText(user.getUsername());
                    label.setTextColor(getResources().getColor(R.color.yes));
                    goHome();
               } else {
                    label.setText("Signup Fail");
                    label.setTextColor(getResources().getColor(R.color.no));
                    login();
               }
          }
     });
     updateScreen();
}

public void login(View v) {
     login();
}

public void login() {
     final TextView label = (TextView)findViewById(R.id.user);
     EditText username = (EditText)findViewById(R.id.username);
     EditText password = (EditText)findViewById(R.id.password);
     ParseUser.logInInBackground(username.getText().toString().toLowerCase(), password.getText().toString(), new LogInCallback()
     {
          public void done(ParseUser user, ParseException e)
          {
               if (user != null) {
                    label.setText(user.getUsername().substring(0,1).toUpperCase() + user.getUsername().substring(1).toLowerCase());
                    genderString = user.getString("gender");
                    letterString = user.getString("letter");
                    nameString = user.getString("name");
                    limitInt = user.getInt("limit");
                    label.setTextColor(getResources().getColor(R.color.yes));
                    goHome();
               } else {
                    label.setText("Login Fail");
                    label.setTextColor(getResources().getColor(R.color.no));

                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {
                         goHome();
                    }
                    signup();
               }
          }
     });
     updateScreen();
}

public void logout(View v) {
     ParseUser.logOut();
     final TextView label = (TextView)findViewById(R.id.user);
     label.setTextColor(getResources().getColor(R.color.yes));
     updateScreen();
     label.setText("Logged Out");
}

public void setfbString() {
     fbString = "logged out";
}

public void updateScreen() {
     TextView user = (TextView)findViewById(R.id.user);
     EditText username = (EditText)findViewById(R.id.username);
     EditText password = (EditText)findViewById(R.id.password);
     EditText email = (EditText)findViewById(R.id.email);
     Button login = (Button)findViewById(R.id.login);
     Button logout = (Button)findViewById(R.id.logout);
     Button signup = (Button)findViewById(R.id.signup);
     Button fb = (Button)findViewById(R.id.fb);
     ParseUser currentUser = ParseUser.getCurrentUser();
     user.setTextColor(getResources().getColor(R.color.black));
     if (currentUser != null) {
          login.setAlpha(1);
          login.setEnabled(true);
          signup.setAlpha(0);
          signup.setEnabled(false);
          logout.setAlpha(1);
          logout.setEnabled(true);
          if (fbString.equals("unlinked")) {
               fb.setText("Link with FB");
          }
          if (fbString.equals("linked")) {
               fb.setText("Unink from FB");
          }
          user.setText("My Account");
     } else {
          if (username.getText().toString().isEmpty()) {
               user.setText("Type your name below.");
               email.setAlpha(0);
               email.setEnabled(false);
          } else {
               if (password.getText().toString().isEmpty()) {
                    user.setText("Type your password below.");
               } else {
                    email.setAlpha(1);
                    email.setEnabled(true);
                    if (email.getText().toString().isEmpty()) {
                         user.setText("If you're new, enter your email.");
                    } else {
                         user.setText("Rock 'n' Roll!");
                    }
               }
          }
          logout.setAlpha(0);
          logout.setEnabled(false);
          fb.setText("Login with FB");
          fbString = "logged out";
          if (username.getText().length() > 0 && password.getText().length() > 0) {
               login.setAlpha(1);
               login.setEnabled(true);
          } else {
               login.setAlpha(0);
               login.setEnabled(false);
          }
          if (username.getText().length() > 0 && password.getText().length() > 0 && email.getText().length() > 0) {
               signup.setAlpha(1);
               signup.setEnabled(true);
          } else {
               signup.setAlpha(0);
               signup.setEnabled(false);
          }
     }
}

public void goHome() {
     lastPage();
     Intent i = new Intent(this, daniel.babynames.Home.class);
     startActivity(i);
}

public void lastPage() {
     ParseUser saveState = new ParseUser().getCurrentUser();
     saveState.put("lastPage", "Account");
}

}
