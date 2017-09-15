package com.unalfaruk.sanatgalerisi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main2Activity extends AppCompatActivity {

    ImageView imgEser;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    EditText txtEserAdi;
    TextView lblEserAdi;
    static SQLiteDatabase veritabani;
    Bitmap veritabaniFotograf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imgEser= (ImageView) findViewById(R.id.imgEser);
        txtEserAdi= (EditText) findViewById(R.id.txtEserIsmi);
        lblEserAdi= (TextView) findViewById(R.id.lblEserAdi);
        Button btnKaydet = (Button) findViewById(R.id.btnKaydet);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equalsIgnoreCase("new")){
            imgEser.setBackgroundColor(getApplicationContext().getResources().getColor(android.R.color.holo_red_dark));
            btnKaydet.setVisibility(View.VISIBLE);
            lblEserAdi.setVisibility(View.INVISIBLE);
            txtEserAdi.setVisibility(View.VISIBLE);
            txtEserAdi.setText("");
        }else{
            int pozisyon=intent.getIntExtra("pozisyon",0);
            txtEserAdi.setVisibility(View.INVISIBLE);
            lblEserAdi.setText(MainActivity.eserAdlari.get(pozisyon));
            lblEserAdi.setVisibility(View.VISIBLE);
            imgEser.setImageBitmap(MainActivity.eserFotograflari.get(pozisyon));
            btnKaydet.setVisibility(View.INVISIBLE);
        }

    }

    public void kaydet(View view){
        String isim = txtEserAdi.getText().toString();

        //Veritabanına fotoğraf kaydetmek için byte dizisine çeviriyoruz.
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        veritabaniFotograf.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{
            veritabani=this.openOrCreateDatabase("SanatEserleri",MODE_PRIVATE, null);
            veritabani.execSQL("CREATE TABLE IF NOT EXISTS eserler (isim VARCHAR, fotograf BLOB)");

            String sqlSorgusu = "INSERT INTO eserler (isim,fotograf) VALUES (?,?)";

            SQLiteStatement statement=veritabani.compileStatement(sqlSorgusu);
            statement.bindString(1,isim);
            statement.bindBlob(2,byteArray);
            statement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent=new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void fotografSec(View view){
        dispatchTakePictureIntent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==2){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                /*Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture,1);*/
                dispatchTakePictureIntent();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_TAKE_PHOTO && resultCode==RESULT_OK && data!=null){

            setPic();
            Log.w("Yol: ",mCurrentPhotoPath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.w("Yol: ",mCurrentPhotoPath);
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imgEser.getWidth();
        int targetH = imgEser.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;


        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        veritabaniFotograf=bitmap;
        imgEser.setImageBitmap(bitmap);
    }
}
