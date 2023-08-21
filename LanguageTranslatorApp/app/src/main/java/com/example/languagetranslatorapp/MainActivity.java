package com.example.languagetranslatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner,toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTv;
    String[]  fromLanguages = {"From","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Czech","Welsh",
    "Hindi","Urdu","Marathi"};
    String[]  toLanguages = {"To","English","Afrikaans","Arabic","Belarusian","Bulgarian","Bengali","Catalan","Czech","Welsh",
            "Hindi","Urdu","Marathi"};

    private static  final int REQUEST_PERMISSION_CODE =1;
    int languagesCode, fromLanguageCode, toLanguageCode = 0;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEditSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTv = findViewById(R.id.idTVTranslatedTV);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent , View view, int position , long l) {
                  fromLanguageCode = getLanguageCode(fromLanguages[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);


        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent , View view,int position , long l) {
                toLanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


          ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
          toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          toSpinner.setAdapter(toAdapter);

          translateBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  translatedTv.setText("");
                  if (sourceEdt.getText().toString().isEmpty()){
                      Toast.makeText(MainActivity.this,"Please enter the text to translate",Toast.LENGTH_SHORT);
                  }
                  else if (fromLanguageCode==0) {
                      Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT);

                  } else if (toLanguageCode==0) {
                      Toast.makeText(MainActivity.this,"Please select the language for translation ",Toast.LENGTH_SHORT);

                  }
                  else {
                      translateText(fromLanguageCode,toLanguageCode,sourceEdt.getText().toString());
                  }

              }
          });


          micIV.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Intent i =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                  i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                  i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                  try {

                      startActivityForResult(i,REQUEST_PERMISSION_CODE);
                  }
                  catch (Exception e) {

                      e.printStackTrace();
                      Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT);

                  }
              }
          });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_PERMISSION_CODE)
        {
            if (resultCode==RESULT_OK && data!=null) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }

    private void  translateText(int fromLanguageCode, int toLanguageCode, String source)
    {

        translatedTv.setText("Downloading model...");
        FirebaseTranslatorOptions options = new  FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions =  new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTv.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTv.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Failed to translate : "+ e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed to download ",Toast.LENGTH_SHORT).show();
            }
        });



    }
    public int getLanguageCode(String language)
    {
        int languageCode = 0;
        switch (language) {


            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;

            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Calatan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;

            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;

            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                break;
            default:
                languageCode=0;


        }
        return languageCode;



    }



}



