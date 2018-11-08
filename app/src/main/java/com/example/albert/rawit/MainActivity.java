package com.example.albert.rawit;

import android.app.ActivityManager;
import android.app.Dialog;
import android.nfc.Tag;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v7.app.AlertDialog;
import com.loopj.android.http.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    Dialog fruitDialog,weightDialog,loadingDialog;
    Button btnEat,btnDrink;
    TextView tvWaterIntake,tvWaterRequired;
    EditText etWeight;
    String fruitSelected="Jeruk";
    String fruitNdbno="09200";
    double weightSelected=0;
    double waterIntake=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnEat=(Button)findViewById(R.id.btn_eat);
        btnDrink=(Button)findViewById(R.id.btn_drink);
        tvWaterIntake=(TextView)findViewById(R.id.tv_water_intake);
        tvWaterIntake.setText(Double.toString(waterIntake));
        tvWaterRequired=(TextView)findViewById(R.id.tv_water_required);
        etWeight=(EditText)findViewById(R.id.et_weight);
        fruitDialog=new Dialog(this);
        weightDialog=new Dialog(this);
        loadingDialog=new Dialog(this);
        btnEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fruitDialog.setContentView(R.layout.fruits);
                fruitDialog.getWindow();
                fruitDialog.show();
                Toast.makeText(getApplicationContext(),"Eat",Toast.LENGTH_SHORT).show();
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
                        weightDialog.setContentView(R.layout.weight);
                        weightDialog.getWindow();
                        weightDialog.show();
                        Toast.makeText(getApplicationContext(),"Weight",Toast.LENGTH_SHORT).show();
                        Button btnCancel,btnFinish;
                        TextView txtHeader;
                        btnCancel=(Button)weightDialog.findViewById(R.id.btn_cancel);
                        btnFinish=(Button)weightDialog.findViewById(R.id.btn_finish);
                        txtHeader=(TextView)weightDialog.findViewById(R.id.txt_Header);
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
                                weightDialog.dismiss();
                                if(etWeight.getText().toString().matches("")){
                                    //pilih di radio button
                                }else{
                                    weightSelected=Double.parseDouble(etWeight.getText().toString());
                                }
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
                                            Log.i("hasil "+fruitSelected,String.valueOf(result));
                                            tvWaterIntake.setText(Double.toString(waterIntake+=result));
                                        }catch (JSONException e){
                                            Log.i("hasil","Ketangkep");
                                            e.printStackTrace();
                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                                Toast.makeText(getApplicationContext(),"Kamu makan buah "+fruitSelected+" dengan berat "+weightSelected+" gram",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

            }
        });
        btnDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Input Drink",Toast.LENGTH_SHORT).show();
            }
        });
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
                    fruitSelected = "Jeruk";
                    fruitNdbno = "09200";
                }
                    break;
            case R.id.radio_semangka:
                if (checked) {
                    fruitSelected = "Semangka";
                    fruitNdbno = "09326";
                }
                    break;
            case R.id.radio_nanas:
                if (checked) {
                    fruitSelected = "Nanas";
                    fruitNdbno="09266";
                }
                break;
            case R.id.radio_pepaya:
                if (checked) {
                    fruitSelected = "Pepaya";
                    fruitNdbno="09226";
                }
                break;
            case R.id.radio_belimbing:
                if (checked) {
                    fruitSelected = "Belimbing";
                    fruitNdbno = "09060";
                }
                break;
            case R.id.radio_50gram:
                if (checked)
                    weightSelected=50;
                break;
            case R.id.radio_100Gram:
                if (checked)
                    weightSelected=100;
                break;
            case R.id.radio_150Gram:
                if (checked)
                    weightSelected=150;
                break;
        }
    }
}
