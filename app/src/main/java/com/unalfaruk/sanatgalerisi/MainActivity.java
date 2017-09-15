package com.unalfaruk.sanatgalerisi;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<Bitmap> eserFotograflari;
    static ArrayList<String> eserAdlari;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lstAnaListe= (ListView) findViewById(R.id.lstAnaListe);

        eserAdlari = new ArrayList<>();
        eserFotograflari = new ArrayList<>();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,eserAdlari);
        lstAnaListe.setAdapter(arrayAdapter);

        try{
            Main2Activity.veritabani= this.openOrCreateDatabase("SanatEserleri",MODE_PRIVATE,null);
            Main2Activity.veritabani.execSQL("CREATE TABLE IF NOT EXISTS eserler (isim VARCHAR, fotograf BLOB)");

            Cursor cursor = Main2Activity.veritabani.rawQuery("SELECT * FROM eserler",null);

            int isimIx= cursor.getColumnIndex("isim");
            int fotografIx=cursor.getColumnIndex("fotograf");

            cursor.moveToFirst();

            while (cursor != null){
                eserAdlari.add(cursor.getString(isimIx));

                byte[] byteArray = cursor.getBlob(fotografIx);
                Bitmap fotograf = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
                eserFotograflari.add(fotograf);

                cursor.moveToNext();

                arrayAdapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        lstAnaListe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),Main2Activity.class);
                intent.putExtra("info","eski");
                intent.putExtra("pozisyon",position);

                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.add_art){
            Intent intent=new Intent(getApplicationContext(),Main2Activity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


}
