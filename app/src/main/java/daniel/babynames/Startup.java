package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.parse.Parse;

public class Startup extends Activity
{

@Override
protected void onCreate(Bundle b)
{
     requestWindowFeature(Window.FEATURE_NO_TITLE);
     Parse.enableLocalDatastore(this);
     Parse.initialize(this, "bm6jT0Rj1lUCwutfFk1WUgXm4wLRpN1Jz4LKMLqw", "Y8IFJzyA64AhAUOh7TvOn7FXd3U6xcIhwY4pQWuz");
     super.onCreate(b);
     Intent i = new Intent(this, Account.class);
     startActivity(i);
}

}
