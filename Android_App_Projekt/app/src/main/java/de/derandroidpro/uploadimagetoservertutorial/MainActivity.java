package de.derandroidpro.uploadimagetoservertutorial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final String uploadUrlString = "http://derandroidpro.esy.es/upload_file_to_server_tutorial/UploadReceiver.php";

    Button btn_choose, btn_upload;
    ImageView imageView;
    TextView tv_link;

    final int PICK_IMAGE_REQ_CODE = 12;
    final int EXTERNAL_STORAGE_PERMISSION_REQ_CODE = 14;

    Uri imageUri;

    ProgressDialog uploadDialog;

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
                if(imageUri!= null && internetAvailable()) {
                    uploadDialog = new ProgressDialog(MainActivity.this);
                    uploadDialog.setTitle("Bild wird hochgeladen...");
                    uploadDialog.setMessage("Bitte warten.");
                    uploadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    uploadDialog.show();
                    new UploadImageAsyncTask().execute(null);
                }
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
            tv_link.setText(null);
        }
    }

    private class UploadImageAsyncTask extends AsyncTask{

        String serverResponse;

        @Override
        protected Object doInBackground(Object[] params) {

            String boundary = "---boundary"+System.currentTimeMillis();
            String firstLineBoundary = "--"+boundary+"\r\n";
            String contentDisposition = "Content-Disposition: form-data;name=\"fileupload1\";filename=\"imagefile.jpg\"\r\n";
            String newLine = "\r\n";
            String lastLineBoundary = "--"+boundary+"--\r\n";

            try {
                InputStream imageInputStream = getContentResolver().openInputStream(imageUri);
                int uploadSize = (firstLineBoundary+contentDisposition+newLine+newLine+lastLineBoundary).getBytes().length + imageInputStream.available();
                uploadDialog.setMax(uploadSize);


                URL uploadUrl = new URL(uploadUrlString);
                HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setFixedLengthStreamingMode(uploadSize);

                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(firstLineBoundary);
                dataOutputStream.writeBytes(contentDisposition);
                dataOutputStream.writeBytes(newLine);

                int byteCounter = 0;

                byte[] buffer = new byte[1024];
                int read;
                while ((read = imageInputStream.read(buffer)) != -1){
                    dataOutputStream.write(buffer, 0, read);
                    byteCounter+=1024;
                    uploadDialog.setProgress(byteCounter);
                }

                dataOutputStream.writeBytes(newLine);
                dataOutputStream.writeBytes(lastLineBoundary);
                dataOutputStream.flush();
                dataOutputStream.close();

                serverResponse = getTextFromInputStream(connection.getInputStream());
                connection.getInputStream().close();
                connection.disconnect();


            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Es ist ein Fehler aufgetreten! Bitte erneut versuchen!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Es ist ein Fehler aufgetreten! Bitte erneut versuchen!", Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            imageView.setImageDrawable(null);
            btn_upload.setVisibility(View.INVISIBLE);
            tv_link.setText(serverResponse);
            uploadDialog.dismiss();
            super.onPostExecute(o);
        }
    }

    public String getTextFromInputStream(InputStream is){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String aktuelleZeile;
        try {
            while ((aktuelleZeile = reader.readLine()) != null){
                stringBuilder.append(aktuelleZeile);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString().trim();
    }

    private boolean internetAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


}
