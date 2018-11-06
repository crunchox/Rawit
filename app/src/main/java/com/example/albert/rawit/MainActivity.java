package com.example.albert.rawit;

import android.app.ActivityManager;
import android.app.Dialog;
import android.nfc.Tag;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Dialog fruitDialog,weightDialog;
    Button btnEat,btnDrink;
    String fruitSelected;
    int weightSelected;
    String[] fruitList=new String[]{"Jeruk","Semangka","Nanas","Pepaya","Belimbing"};
    Toast toast;
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
                        Button btnCancel,btnNext;
                        TextView txtHeader;
                        btnCancel=(Button)weightDialog.findViewById(R.id.btn_cancel);
                        btnNext=(Button)weightDialog.findViewById(R.id.btn_next);
                        txtHeader=(TextView)weightDialog.findViewById(R.id.txt_Header);
                        txtHeader.setText("Count a fruit");
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                weightDialog.dismiss();
                            }
                        });
                        btnNext.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                toast.makeText(view.getContext(),"fruit:",Toast.LENGTH_LONG);
                            }
                        });
                    }
                });

            }
        });
        btnDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast=Toast.makeText(getApplicationContext(),"Input Drink",Toast.LENGTH_SHORT);
                toast.show();
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
                if (checked)
                    fruitSelected="jeruk";
                    break;
            case R.id.radio_semangka:
                if (checked)
                    fruitSelected="semangka";
                    break;
            case R.id.radio_nanas:
                if (checked)
                    fruitSelected="nanas";
                break;
            case R.id.radio_pepaya:
                if (checked)
                    fruitSelected="pepaya";
                break;
            case R.id.radio_belimbing:
                if (checked)
                    fruitSelected="belimbing";
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
