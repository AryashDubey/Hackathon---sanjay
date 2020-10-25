package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button Camera;
    Button Gallery;
    ImageView imageview;
    ListView listView;
    private ImageClassifier classifier;

    public void askCamerapermit(){
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},1);
        }else{
            camera();
        }
    }
    public void askStoragePermit(){
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
        }else{
            galler();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_DENIED) {
                camera();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }else if(requestCode==2){
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_DENIED) {
                galler();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
    }

    public void camera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
    }
    public void galler(){
        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,2);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            classifier = new ImageClassifier(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageview = findViewById(R.id.imageView);
        Camera =findViewById(R.id.button);
        Gallery = findViewById(R.id.button2);
        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askCamerapermit();
            }
        });
        Gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askStoragePermit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode==1) && resultCode ==RESULT_OK &&data!=null){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageview.setImageBitmap(bitmap);

            classifier.recognizeimage(bitmap,0);
            List<ImageClassifier.Recognition> predicitons = classifier.recognizeimage(
                    bitmap, 0);
            final List<String> predicitonsList = new ArrayList<>();
            for (ImageClassifier.Recognition recog : predicitons) {
                predicitonsList.add(recog.getName() + "  ::::::::::  " + recog.getConfidence());
            }
            ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                    this, R.layout.support_simple_spinner_dropdown_item, predicitonsList);
            listView.setAdapter(predictionsAdapter);
        }
        else if(requestCode==2&& resultCode ==RESULT_OK &&data!=null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                classifier.recognizeimage(bitmap,0);
                List<ImageClassifier.Recognition> predicitons = classifier.recognizeimage(
                        bitmap, 0);
                final List<String> predicitonsList = new ArrayList<>();
                for (ImageClassifier.Recognition recog : predicitons) {
                    predicitonsList.add(recog.getName() + "  ::::::::::  " + recog.getConfidence());
                }
                ArrayAdapter<String> predictionsAdapter = new ArrayAdapter<>(
                        this, R.layout.support_simple_spinner_dropdown_item, predicitonsList);
                listView.setAdapter(predictionsAdapter);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}