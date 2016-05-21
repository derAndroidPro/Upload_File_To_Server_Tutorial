package de.derandroidpro.uploadimagetoservertutorial;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final String uploadUrlString = "http://derandroidpro.esy.es/upload_file_to_server_tutorial/UploadReceiver.php";

    Button btn_choose, btn_upload;
    ImageView imageView;
    TextView tv_link;

    final int PICK_IMAGE_REQ_CODE = 12;
    final int EXTERNAL_STORAGE_PERMISSION_REQ_CODE = 14;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_choose = (Button) findViewById(R.id.button_choose);
        btn_choose.setOnClickListener(this);
        btn_upload = (Button) findViewById(R.id.button_upload);
        btn_upload.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        tv_link = (TextView) findViewById(R.id.textView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_choose:{
                if(ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    pickImage();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},EXTERNAL_STORAGE_PERMISSION_REQ_CODE );
                }
                break;
            }
            case R.id.button_upload:{

                break;
            }
        }
    }

    public void pickImage(){
        Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickImageIntent.setType("image/*");
        startActivityForResult(pickImageIntent, PICK_IMAGE_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == EXTERNAL_STORAGE_PERMISSION_REQ_CODE && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            pickImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQ_CODE){
            imageView.setImageURI(data.getData());
            imageUri = data.getData();
            btn_upload.setVisibility(View.VISIBLE);
        }
    }
}
