package com.example.albert.rawit;

import android.app.AlertDialog;
import android.app.Dialog;

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
import com.loopj.android.http.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    Dialog fruitDialog,weightDialog,loadingDialog,waterDialog;
    Button btnEat,btnDrink,btnLogout;
//    ImageView ivAbu,ivBiru;
    ProgressBar progressBar;
    TextView tvWaterIntake,tvWaterRequired,tvTarget;
    EditText etWeight,etMl;
    String fruitSelected="Jeruk";
    String fruitNdbno="09200";
    double weightSelected=0,mlSelected=0;
    double waterIntake=0;
    double waterRequired=0;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference rootRef,profileRef;
    private FirebaseUser user;
    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener((mAuthListener));
        collectData();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        };
        btnEat=(Button)findViewById(R.id.btn_eat);
        btnDrink=(Button)findViewById(R.id.btn_drink);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        btnLogout=(Button)findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
            }
        });
        tvWaterIntake=(TextView)findViewById(R.id.tv_water_intake);
        tvWaterIntake.setText(Double.toString(waterIntake));
        tvWaterRequired=(TextView)findViewById(R.id.tv_water_required);
        tvWaterRequired.setText(Double.toString(waterRequired));
        tvTarget=(TextView)findViewById(R.id.tv_target);
        fruitDialog=new Dialog(this);
        weightDialog=new Dialog(this);
        loadingDialog=new Dialog(this);
        waterDialog=new Dialog(this);
        btnEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fruitDialog.setContentView(R.layout.fruits);
                fruitDialog.getWindow();
                fruitDialog.show();
                //Toast.makeText(getApplicationContext(),"Eat",Toast.LENGTH_SHORT).show();
                Button btnCancel,btnNext;
                TextView txtHeader;
                btnCancel=(Button)fruitDialog.findViewById(R.id.btn_cancel);
                btnNext=(Button)fruitDialog.findViewById(R.id.btn_next);
                txtHeader=(TextView)fruitDialog.findViewById(R.id.txt_Header);
                txtHeader.setText("Pick a Fruit");
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fruitDialog.dismiss();
                    }
                });
                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fruitDialog.dismiss();
                        if(fruitSelected=="Orange"){
                            weightDialog.setContentView(R.layout.orange_weight);
                        }else if(fruitSelected=="Watermelon"){
                            weightDialog.setContentView(R.layout.watermelon_weight);
                        }else if(fruitSelected=="Pineapple"){
                            weightDialog.setContentView(R.layout.pineapple_weight);
                        }else if(fruitSelected=="Papaya"){
                            weightDialog.setContentView(R.layout.papaya_weight);
                        }else if(fruitSelected=="Starfruit"){
                            weightDialog.setContentView(R.layout.starfruit_weight);
                        }

                        weightDialog.getWindow();
                        weightDialog.show();
                        //Toast.makeText(getApplicationContext(),"Weight",Toast.LENGTH_SHORT).show();
                        Button btnCancel,btnFinish;
                        TextView txtHeader;
                        btnCancel=(Button)weightDialog.findViewById(R.id.btn_cancel);
                        btnFinish=(Button)weightDialog.findViewById(R.id.btn_finish);
                        txtHeader=(TextView)weightDialog.findViewById(R.id.txt_Header);
                        etWeight=(EditText)weightDialog.findViewById(R.id.et_weight);
                        txtHeader.setText("How many gram you eat?");
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                weightDialog.dismiss();
                            }
                        });
                        btnFinish.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(etWeight.getText().toString().matches("")){
                                    //pilih di radio button
                                }else{
                                    weightSelected=Double.parseDouble(etWeight.getText().toString());
                                }
                                weightDialog.dismiss();
                                loadingDialog.setContentView(R.layout.loading);
                                loadingDialog.show();
                                AsyncHttpClient client=new AsyncHttpClient();
                                client.get("https://api.nal.usda.gov/ndb/V2/reports?ndbno="+fruitNdbno+"&type=f&format=json&api_key=9mmcwZMDKBZIQGWhx97v0jWfWPzmS5prWuXwdC4d", new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        //Log.i("hasilresponse",String.valueOf(response));
                                        double refuse=0,value=0;
                                        try{
                                            JSONArray arrFood=response.getJSONArray("foods");
                                            for (int i =0 ; i<arrFood.length();i++) {
                                                JSONObject objFood = (JSONObject) arrFood.getJSONObject(i);
                                                //Log.i("hasilobjfood",String.valueOf(objFood));
                                                JSONObject food=objFood.getJSONObject("food");
                                                //Log.i("hasilfood",String.valueOf(food));
                                                JSONObject desc=food.getJSONObject("desc");
                                                //Log.i("hasildesc",String.valueOf(desc));
                                                //Log.i("hasilrefuse",String.valueOf(desc.getString("r").substring(0,2)));
                                               // Log.i("hasilrefuse",String.valueOf(desc.getString("r").length()));
                                                if(desc.getString("r").length()==2){
                                                    refuse=Double.parseDouble(desc.getString("r").substring(0,1));
                                                }else if(desc.getString("r").length()==3){
                                                    refuse=Double.parseDouble(desc.getString("r").substring(0,2));
                                                }
                                                JSONArray arrNutrient=food.getJSONArray("nutrients");
                                                for (int j=0;j<1;j++){
                                                    JSONObject objNutrient = (JSONObject) arrNutrient.getJSONObject(i);
                                                    //Log.i("hasilobjnutrient",String.valueOf(objNutrient));
                                                    //Log.i("hasilvalue",String.valueOf(objNutrient.getDouble("value")));
                                                    value=objNutrient.getDouble("value");
                                                    //Log.i("hasilvalue",String.valueOf(value));
                                                }
                                            }
                                            Log.i("hasil "+fruitSelected,String.valueOf(refuse+" "+value));
                                            double result=(weightSelected/100)*value*(refuse/100);
                                            Log.i("hasil "+fruitSelected,String.format("%.2f",result));
                                            Toast.makeText(getApplicationContext(),"You eat "+String.format("%.0f",weightSelected)+" gram of "+fruitSelected+".\n" +
                                                    "So, you receive "+String.format("%.2f",result)+" ml water.",Toast.LENGTH_LONG).show();
                                            tvWaterIntake.setText(String.format("%.2f",waterIntake+=result));
                                        }catch (JSONException e){
                                            Log.i("hasil","Ketangkep");
                                            e.printStackTrace();
                                        }
                                        double prog=(waterIntake/waterRequired)*100;
                                        int progress=(int) prog;
                                        if(waterIntake<waterRequired){
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                progressBar.setProgress(progress,true);
                                            }
                                        }else{
                                            tvTarget.setVisibility(View.VISIBLE);
                                        }

                                    }

                                    @Override
                                    public void onFinish() {
                                        super.onFinish();
                                        loadingDialog.dismiss();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        super.onFailure(statusCode, headers, responseString, throwable);
                                        AlertDialog.Builder builder;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            builder = new AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Dialog_Alert);
                                        } else {
                                            builder = new AlertDialog.Builder(getApplicationContext());
                                        }
                                        builder.setTitle("Failed")
                                                .setMessage("Failed to get data from server, please check your internet connection")
                                                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });
        btnDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                waterDialog.setContentView(R.layout.water);
                waterDialog.getWindow();
                waterDialog.show();
                Button btnCancel,btnFinish;
                TextView txtHeader;
                btnCancel=(Button)waterDialog.findViewById(R.id.btn_cancel);
                btnFinish=(Button)waterDialog.findViewById(R.id.btn_finish);
                txtHeader=(TextView)waterDialog.findViewById(R.id.txt_Header);
                etMl=(EditText)waterDialog.findViewById(R.id.et_ml);
                txtHeader.setText("How many mililiter you drink?");
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        waterDialog.dismiss();
                    }
                });
                btnFinish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        waterDialog.dismiss();
                        loadingDialog.setContentView(R.layout.loading);
                        loadingDialog.getWindow();
                        loadingDialog.show();
                        if(etMl.getText().toString().matches("")){
                            //pilih di radio button
                        }else{
                            mlSelected=Double.parseDouble(etMl.getText().toString());
                        }
                        Toast.makeText(getApplicationContext(),"You drink "+String.format("%.0f",mlSelected)+" ml water",Toast.LENGTH_LONG).show();
                        tvWaterIntake.setText(String.format("%.2f",waterIntake+=mlSelected));
                        double prog=(waterIntake/waterRequired)*100;
                        int progress=(int) prog;
                        if(waterIntake<waterRequired){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressBar.setProgress(progress,true);
                            }
                        }else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                progressBar.setProgress(progress,true);
                            }
                            tvTarget.setVisibility(View.VISIBLE);
                        }
                        loadingDialog.dismiss();
                    }
                });
            }
        });
    }
    private void collectData(){
        user = mAuth.getCurrentUser();
        if (user!=null){
            profileRef=FirebaseDatabase.getInstance().getReference(user.getUid());
            profileRef.child("FirstName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("profile", String.valueOf(dataSnapshot.getValue(String.class)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            profileRef.child("LastName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("profile", String.valueOf(dataSnapshot.getValue(String.class)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            profileRef.child("WaterRequired").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("profile", String.valueOf(dataSnapshot.getValue(Double.class)));
                    waterRequired=dataSnapshot.getValue(Double.class);
                    tvWaterRequired.setText(String.format("%.2f",waterRequired));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        //toast=Toast.makeText(getApplicationContext(),Integer.toString(view.getId()),Toast.LENGTH_LONG);
        //toast=Toast.makeText(getApplicationContext(),fruitSelected,Toast.LENGTH_LONG);
        //toast.show();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_jeruk:
                if (checked) {
                    fruitSelected = "Orange";
                    fruitNdbno = "09200";
                }
                    break;
            case R.id.radio_semangka:
                if (checked) {
                    fruitSelected = "Watermelon";
                    fruitNdbno = "09326";
                }
                    break;
            case R.id.radio_nanas:
                if (checked) {
                    fruitSelected = "Pineapple";
                    fruitNdbno="09266";
                }
                break;
            case R.id.radio_pepaya:
                if (checked) {
                    fruitSelected = "Papaya";
                    fruitNdbno="09226";
                }
                break;
            case R.id.radio_belimbing:
                if (checked) {
                    fruitSelected = "Starfruit";
                    fruitNdbno = "09060";
                }
                break;
            case R.id.radio_25Gram:
                if (checked)
                    weightSelected=25;
                break;
            case R.id.radio_26Gram:
                if (checked)
                    weightSelected=26;
                break;
            case R.id.radio_33Gram:
                if (checked)
                    weightSelected=33;
                break;
            case R.id.radio_50Gram:
                if (checked)
                    weightSelected=50;
                break;
            case R.id.radio_52Gram:
                if (checked)
                    weightSelected=52;
                break;
            case R.id.radio_66Gram:
                if (checked)
                    weightSelected=66;
                break;
            case R.id.radio_75Gram:
                if (checked)
                    weightSelected=75;
                break;
            case R.id.radio_78Gram:
                if (checked)
                    weightSelected=78;
                break;
            case R.id.radio_90Gram:
                if (checked)
                    weightSelected=90;
                break;
            case R.id.radio_99Gram:
                if (checked)
                    weightSelected=99;
                break;
            case R.id.radio_100Gram:
                if (checked)
                    weightSelected=100;
                break;
            case R.id.radio_150Gram:
                if (checked)
                    weightSelected=150;
                break;
            case R.id.radio_180Gram:
                if (checked)
                    weightSelected=180;
                break;
            case R.id.radio_270Gram:
                if (checked)
                    weightSelected=270;
                break;
            case R.id.radio_100ml:
                if (checked)
                    mlSelected=100;
                break;
            case R.id.radio_300ml:
                if (checked)
                    mlSelected=300;
                break;
            case R.id.radio_600ml:
                if (checked)
                    mlSelected=600;
                break;
        }
    }
}
