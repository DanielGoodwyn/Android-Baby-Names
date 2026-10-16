package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Startup extends Activity
{

@Override
protected void onCreate(Bundle b)
{
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     super.onCreate(b);
     FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
     if (currentUser != null) {
          AppPrefs.loadFromFirebase(this, () -> {
               Intent i = new Intent(Startup.this, Home.class);
               startActivity(i);
               finish();
          });
     } else {
          Intent i = new Intent(this, Account.class);
          startActivity(i);
          finish();
     }
}

}
