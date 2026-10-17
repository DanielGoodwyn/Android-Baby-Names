package daniel.babynames;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Account extends Activity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private static final String WEB_CLIENT_ID = "486710219401-6r138uibjue9mira4mloep1e52hdcp7j.apps.googleusercontent.com";

    private GoogleSignInClient mGoogleSignInClient;

    String genderString;
    String letterString;
    String nameString;
    int limitInt;

    @Override
    protected void onCreate(Bundle b) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(b);
        setContentView(R.layout.account);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        EditText emailET = findViewById(R.id.email);
        EditText passwordET = findViewById(R.id.password);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                updateScreen();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        emailET.addTextChangedListener(watcher);
        passwordET.addTextChangedListener(watcher);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        TextView user = findViewById(R.id.user);
        if (currentUser != null) {
            emailET.setText(currentUser.getEmail());
            genderString = AppPrefs.getString(this, "gender", "All");
            letterString = AppPrefs.getString(this, "letter", "");
            nameString = AppPrefs.getString(this, "name", "Adam");
            limitInt = AppPrefs.getInt(this, "limit", 25);
        } else {
            user.setText("Type your email and password below.");
            user.setTextColor(getResources().getColor(R.color.text_primary));
        }
        updateScreen();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        updateScreen();
        return super.dispatchKeyEvent(event);
    }

    public void begin(View v) {
        begin();
    }

    public void begin() {
        final TextView label = findViewById(R.id.user);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        final String emailStr = email.getText().toString().trim().toLowerCase();
        final String passwordStr = password.getText().toString();

        if (emailStr.isEmpty() || passwordStr.isEmpty()) {
            label.setText("Please enter both email and password.");
            label.setTextColor(getResources().getColor(R.color.no));
            return;
        }

        label.setText("Checking account...");
        label.setTextColor(getResources().getColor(R.color.text_primary));

        // Attempt sign in first
        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AppPrefs.loadFromFirebase(Account.this, () -> {
                            String loadedUser = AppPrefs.getString(Account.this, "username", "");
                            if (loadedUser.length() > 0) {
                                label.setText(loadedUser.substring(0, 1).toUpperCase() + loadedUser.substring(1).toLowerCase());
                            } else {
                                label.setText(emailStr.split("@")[0]);
                            }
                            label.setTextColor(getResources().getColor(R.color.yes));
                            goHome();
                        });
                    } else {
                        // User might not exist -> attempt sign up
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailStr, passwordStr)
                                .addOnCompleteListener(signupTask -> {
                                    if (signupTask.isSuccessful()) {
                                        String usernameStr = emailStr.split("@")[0];
                                        AppPrefs.saveString(Account.this, "username", usernameStr);
                                        AppPrefs.saveString(Account.this, "gender", "All");
                                        AppPrefs.saveString(Account.this, "letter", "");
                                        AppPrefs.saveString(Account.this, "name", "Adam");
                                        AppPrefs.saveInt(Account.this, "limit", 25);
                                        AppPrefs.saveString(Account.this, "sort", "popular");

                                        label.setText("Welcome " + usernameStr);
                                        label.setTextColor(getResources().getColor(R.color.yes));
                                        goHome();
                                    } else {
                                        Exception createEx = signupTask.getException();
                                        if (createEx instanceof FirebaseAuthUserCollisionException) {
                                            // Email already in use -> password was incorrect
                                            label.setText("Incorrect password. Tap Forgot Password to reset.");
                                            label.setTextColor(getResources().getColor(R.color.no));
                                        } else {
                                            String msg = (createEx != null && createEx.getLocalizedMessage() != null)
                                                    ? createEx.getLocalizedMessage()
                                                    : "Authentication error.";
                                            label.setText(msg);
                                            label.setTextColor(getResources().getColor(R.color.no));
                                        }
                                    }
                                });
                    }
                });
    }

    public void beginWithGoogle(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    TextView label = findViewById(R.id.user);
                    label.setText("Google Sign-In failed to get token.");
                    label.setTextColor(getResources().getColor(R.color.no));
                }
            } catch (ApiException e) {
                TextView label = findViewById(R.id.user);
                label.setText("Google Sign-In failed (" + e.getStatusCode() + ")");
                label.setTextColor(getResources().getColor(R.color.no));
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        final TextView label = findViewById(R.id.user);
        label.setText("Signing in with Google...");
        label.setTextColor(getResources().getColor(R.color.text_primary));

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String email = user != null && user.getEmail() != null ? user.getEmail() : "";
                        final String displayName = user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()
                                ? user.getDisplayName()
                                : (email.contains("@") ? email.split("@")[0] : "User");

                        AppPrefs.loadFromFirebase(Account.this, () -> {
                            String existingUser = AppPrefs.getString(Account.this, "username", "");
                            if (existingUser == null || existingUser.isEmpty()) {
                                AppPrefs.saveString(Account.this, "username", displayName);
                                AppPrefs.saveString(Account.this, "gender", "All");
                                AppPrefs.saveString(Account.this, "letter", "");
                                AppPrefs.saveString(Account.this, "name", "Adam");
                                AppPrefs.saveInt(Account.this, "limit", 25);
                                AppPrefs.saveString(Account.this, "sort", "popular");
                            }
                            label.setText("Welcome " + displayName);
                            label.setTextColor(getResources().getColor(R.color.yes));
                            goHome();
                        });
                    } else {
                        String msg = task.getException() != null ? task.getException().getLocalizedMessage() : "Google sign-in error";
                        label.setText(msg);
                        label.setTextColor(getResources().getColor(R.color.no));
                    }
                });
    }

    public void logout(View v) {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut();
        }
        AppPrefs.logout(this);
        final TextView label = findViewById(R.id.user);
        label.setTextColor(getResources().getColor(R.color.yes));
        label.setText("Logged Out");
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        email.setText("");
        password.setText("");
        updateScreen();
    }

    public void resetPassword(View v) {
        EditText email = findViewById(R.id.email);
        String emailStr = email.getText().toString().trim();
        final TextView label = findViewById(R.id.user);
        if (emailStr.isEmpty()) {
            label.setText("Enter email to reset.");
            label.setTextColor(getResources().getColor(R.color.no));
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailStr)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        label.setText("Reset email sent.");
                        label.setTextColor(getResources().getColor(R.color.yes));
                    } else {
                        label.setText("Reset fail: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        label.setTextColor(getResources().getColor(R.color.no));
                    }
                });
    }

    public void updateScreen() {
        TextView user = findViewById(R.id.user);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);
        View forgotPassword = findViewById(R.id.forgotPassword);
        Button beginBtn = findViewById(R.id.begin);
        Button googleBeginBtn = findViewById(R.id.googleBegin);
        Button logoutBtn = findViewById(R.id.logout);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            user.setTextColor(getResources().getColor(R.color.text_primary));
            user.setText("Logged in as:\n" + (currentUser.getEmail() != null ? currentUser.getEmail() : "User"));
            email.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            forgotPassword.setVisibility(View.GONE);
            beginBtn.setVisibility(View.GONE);
            googleBeginBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.VISIBLE);
        } else {
            email.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            forgotPassword.setVisibility(View.VISIBLE);
            beginBtn.setVisibility(View.VISIBLE);
            googleBeginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);

            String e = email.getText().toString().trim();
            String p = password.getText().toString();

            if (e.isEmpty() && p.isEmpty()) {
                user.setText("Type your email and password below.");
                user.setTextColor(getResources().getColor(R.color.text_primary));
            }

            if (!e.isEmpty() && !p.isEmpty()) {
                beginBtn.setEnabled(true);
                beginBtn.setAlpha(1.0f);
            } else {
                beginBtn.setEnabled(false);
                beginBtn.setAlpha(0.4f);
            }
        }
    }

    public void goHome() {
        lastPage();
        Intent i = new Intent(this, daniel.babynames.Home.class);
        startActivity(i);
    }

    public void lastPage() {
        AppPrefs.saveString(this, "lastPage", "Account");
    }
}
