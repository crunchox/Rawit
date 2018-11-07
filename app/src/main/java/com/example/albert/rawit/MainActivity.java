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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    Dialog fruitDialog,weightDialog;
    Button btnEat,btnDrink;
    String fruitSelected="jeruk";
    String fruitNdbno="09200";
    int weightSelected=50;
    String[] fruitList=new String[]{"Jeruk","Semangka","Nanas","Pepaya","Belimbing"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnEat=(Button)findViewById(R.id.btn_eat);
        btnDrink=(Button)findViewById(R.id.btn_drink);
        fruitDialog=new Dialog(this);
        weightDialog=new Dialog(this);
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
                                AsyncHttpClient client=new AsyncHttpClient();
                                client.get("https://api.nal.usda.gov/ndb/V2/reports?ndbno="+fruitNdbno+"&type=f&format=json&api_key=9mmcwZMDKBZIQGWhx97v0jWfWPzmS5prWuXwdC4d", new JsonHttpResponseHandler(){
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        Toast.makeText(getApplicationContext(),"on Success",Toast.LENGTH_SHORT).show();
                                        try{
                                            String data=response.getString("foods");
                                            Log.i("hasilnya",data);

                                        }catch (JSONException e){
                                            e.printStackTrace();
                                        }
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
                    fruitSelected = "jeruk";
                    fruitNdbno = "09200";
                }
                    break;
            case R.id.radio_semangka:
                if (checked) {
                    fruitSelected = "semangka";
                    fruitNdbno = "09326";
                }
                    break;
            case R.id.radio_nanas:
                if (checked) {
                    fruitSelected = "nanas";
                    fruitNdbno="09266";
                }
                break;
            case R.id.radio_pepaya:
                if (checked) {
                    fruitSelected = "pepaya";
                    fruitNdbno="09226";
                }
                break;
            case R.id.radio_belimbing:
                if (checked) {
                    fruitSelected = "belimbing";
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
