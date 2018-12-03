package com.example.albert.rawit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseAuth mAuth;
    private Dialog registerDialog,registerDialog2;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    int year,month,day;
    DatabaseReference rootRef,profileRef;
    Dialog loadingDialog;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button mEmailSignUpButton=(Button) findViewById(R.id.email_sign_up_button);
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        loadingDialog.show();
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            loadingDialog.dismiss();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private void attemptRegister(){
        registerDialog=new Dialog(this);
        registerDialog.setContentView(R.layout.register1);
        registerDialog2=new Dialog(this);
        registerDialog2.setContentView(R.layout.register2);
        registerDialog.getWindow();
        registerDialog.show();
        Button btnNext;
        final EditText etEmail,etPassword,etRePassword;
        btnNext=(Button)registerDialog.findViewById(R.id.btn_next);
        etEmail=(EditText)registerDialog.findViewById(R.id.email);
        etPassword=(EditText)registerDialog.findViewById(R.id.password);
        etRePassword=(EditText)registerDialog.findViewById(R.id.retypePassword);
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.show();
                etEmail.setError(null);
                etPassword.setError(null);
                etRePassword.setError(null);
                final String email=etEmail.getText().toString();
                final String password=etPassword.getText().toString();
                String rePassword=etRePassword.getText().toString();
                boolean cancel = false;
                View focusView = null;

                // Validation
                if (TextUtils.isEmpty(email)) {
                    etEmail.setError(getString(R.string.error_field_required));
                    focusView = etEmail;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    etEmail.setError(getString(R.string.error_invalid_email));
                    focusView = etEmail;
                    cancel = true;
                }
                if (TextUtils.isEmpty(password)) {
                    etPassword.setError(getString(R.string.error_field_required));
                    focusView = etPassword;
                    cancel = true;
                }else if(!isPasswordValid(password)){
                    etPassword.setError(getString(R.string.error_invalid_password));
                    focusView = etPassword;
                    cancel = true;
                }
                if(!rePassword.equals(password)){
                    etRePassword.setError(getString(R.string.error_match_password));
                    focusView = etRePassword;
                    cancel = true;
                }
                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                    loadingDialog.dismiss();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    try {
                        // Simulate network access.
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.getMessage();
                    }
                    loadingDialog.dismiss();
                    registerDialog.dismiss();
                    registerDialog2.getWindow();
                    registerDialog2.show();
                    final Spinner spinnerGender=(Spinner)registerDialog2.findViewById(R.id.spinner_gender);
                    final Spinner spinnerAct=(Spinner)registerDialog2.findViewById(R.id.spinner_act);
                    final EditText etFirstName,etLastName,etWeight,etHeight;
                    final TextView tvDOB,tvWarningDOB;
                    tvDOB=(TextView) registerDialog2.findViewById(R.id.tvDOB);
                    tvWarningDOB=(TextView)registerDialog2.findViewById(R.id.tvWarningDOB);
                    tvDOB.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Calendar calendar=Calendar.getInstance();
                            int year =calendar.get(Calendar.YEAR);
                            int month =calendar.get(Calendar.MONTH);
                            int day =calendar.get(Calendar.DAY_OF_MONTH);

                            DatePickerDialog dialog=new DatePickerDialog(
                                    LoginActivity.this,
                                    AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                                    mDateSetListener,
                                    year,month,day);
                            dialog.getWindow();
                            dialog.show();
                        }
                    });
                    mDateSetListener=new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            year=i;
                            month=i1+1;
                            day=i2;
                            tvDOB.setText(day+"/"+month+"/"+year);
                        }
                    };
                    etFirstName=(EditText)registerDialog2.findViewById(R.id.firstName);
                    etLastName=(EditText)registerDialog2.findViewById(R.id.lastName);
                    etWeight=(EditText)registerDialog2.findViewById(R.id.weight);
                    etHeight=(EditText)registerDialog2.findViewById(R.id.height);
                    Button btnFinish=(Button)registerDialog2.findViewById(R.id.btn_finish);
                    btnFinish.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loadingDialog.show();
                            tvWarningDOB.setVisibility(View.GONE);
                            tvDOB.setError(null);
                            final String firstName=etFirstName.getText().toString();
                            final String lastName=etLastName.getText().toString();
                            final String gender=spinnerGender.getSelectedItem().toString();
                            final String act=spinnerAct.getSelectedItem().toString();
                            int weight=0,height=0,age=0;
                            double rumus1=0,rumus2=0,amb=0;
                            boolean cancel2=false;
                            View focusView2 = null;
                            //Validation
                            if(TextUtils.isEmpty(firstName)){
                                etFirstName.setError(getString(R.string.error_field_required));
                                focusView2 = etFirstName;
                                cancel2 = true;
                            }
                            if(TextUtils.isEmpty(etWeight.getText().toString())){
                                etWeight.setError(getString(R.string.error_field_required));
                                focusView2 = etWeight;
                                cancel2 = true;
                            }else{
                                weight=Integer.parseInt(etWeight.getText().toString());

                            }
                            if(TextUtils.isEmpty(etHeight.getText().toString())){
                                etHeight.setError(getString(R.string.error_field_required));
                                focusView2 = etHeight;
                                cancel2 = true;
                            }else{
                                height=Integer.parseInt(etHeight.getText().toString());
                            }
                            if (year==0&&month==0&&day==0){
                                tvDOB.setError(getString(R.string.error_field_required));
                                tvWarningDOB.setVisibility(View.VISIBLE);
                                focusView2 = tvDOB;
                                cancel2 = true;
                            }else{
                                age=getAge(year,month,day);
                            }

                            if (cancel2){
                                focusView2.requestFocus();
                                loadingDialog.dismiss();
                            }else{
                                final double iwl=(15*weight)/24;
                                if (age<17){
                                    if (weight<10){
                                        rumus1=100*weight;
                                    }else if(weight>20){
                                        rumus1=1500+((weight-20)*20);
                                    }else{
                                        rumus1=1000+((weight-10)*50);
                                    }
                                }else{
                                    rumus1=50*weight;
                                }
                                if(gender.equals("Male")){
                                    amb=66.5+(13.7*weight)+(5*height)-(6.8*age);
                                    if(act.equals("High")){
                                        rumus2=amb*2.1;
                                    }else if(act.equals("Moderate")){
                                        rumus2=amb*1.76;
                                    }else{
                                        rumus2=amb*1.56;
                                    }
                                }else if(gender.equals("Female")){
                                    amb=65.5+(9.6*weight)+(1.8*height)-(4.7*age);
                                    if(act.equals("High")){
                                        rumus2=amb*2;
                                    }else if(act.equals("Moderate")){
                                        rumus2=amb*1.7;
                                    }else{
                                        rumus2=amb*1.55;
                                    }
                                }
                                final double waterRequired=(rumus1+rumus2)/2;
                                final int ageCopy=age;
                                final int weightCopy=weight;
                                final int heightCopy=height;
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    user = mAuth.getCurrentUser();
                                                    rootRef = FirebaseDatabase.getInstance().getReference(user.getUid());
                                                    profileRef = rootRef.child("FirstName");
                                                    profileRef.setValue(firstName);
                                                    profileRef = rootRef.child("LastName");
                                                    profileRef.setValue(lastName);
                                                    profileRef = rootRef.child("Gender");
                                                    profileRef.setValue(gender);
                                                    profileRef = rootRef.child("Age");
                                                    profileRef.setValue(ageCopy);
                                                    profileRef = rootRef.child("Weight");
                                                    profileRef.setValue(weightCopy);
                                                    profileRef = rootRef.child("Height");
                                                    profileRef.setValue(heightCopy);
                                                    profileRef = rootRef.child("Activity");
                                                    profileRef.setValue(act);
                                                    profileRef = rootRef.child("IWL");
                                                    profileRef.setValue(iwl);
                                                    profileRef = rootRef.child("WaterIntake");
                                                    profileRef.setValue(0);
                                                    profileRef = rootRef.child("WaterRequired");
                                                    profileRef.setValue(waterRequired);
                                                    profileRef = rootRef.child("Status");
                                                    profileRef.setValue("Normal");
                                                    Log.i("signup", "createUserWithEmail:success "+user.getUid());
                                                    Toast.makeText(getApplicationContext(),"Authentication Success",Toast.LENGTH_SHORT).show();
                                                    registerDialog2.dismiss();
                                                    loadingDialog.dismiss();
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    Log.w("signup", "createUserWithEmail:failure", task.getException());
                                                    Toast.makeText(getApplicationContext(), "Authentication failed.",Toast.LENGTH_SHORT).show();
                                                    loadingDialog.dismiss();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
    private int getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);

        return ageInt;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            mAuth.signInWithEmailAndPassword(mEmail,mPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Log.i("signin", "createUserWithEmail:success "+user);
                                Toast.makeText(getApplicationContext(),"Auth Success "+user,Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("signin", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                loadingDialog.dismiss();
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            loadingDialog.dismiss();
            showProgress(false);
        }
    }
}

