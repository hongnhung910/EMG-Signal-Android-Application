package emgsignal.v3;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ListFilesActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    ArrayList<String> myList;
    ListView listview;
    ArrayList<String> line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_files);
        listview = findViewById(R.id.list);
        myList = new ArrayList<>();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data");
                    if (dir.exists()) {
                        Log.d("path", dir.toString());
                        File list[] = dir.listFiles();
                        for (int i = 0; i < list.length; i++) {
                            myList.add(list[i].getName());
                        }
                        ArrayAdapter arrayAdapter = new ArrayAdapter(ListFilesActivity.this, android.R.layout.simple_list_item_1, myList);
                        listview.setAdapter(arrayAdapter);
                    }
                } else {
                    requestPermission(); // Code for permission
                }
            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data");
                if (dir.exists()) {
                    Log.d("path", dir.toString());
                    File list[] = dir.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        myList.add(list[i].getName());
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ListFilesActivity.this, android.R.layout.simple_list_item_1, myList);
                    listview.setAdapter(arrayAdapter);
                }
            }
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/EMG_Data/"+ myList.get(position));
                Toast.makeText(ListFilesActivity.this,file + "",Toast.LENGTH_LONG).show();
                StringBuilder text = new StringBuilder();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    line = new ArrayList<>();
                    int i=0;
                    while (br.readLine() != null) {
                        text.append(br.readLine());
                        text.append("\n");
                        line.add(i,br.readLine());
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(ListFilesActivity.this,LoadDataActivity.class);
                intent.putExtra("Data",text.toString());
                intent.putExtra("Namefile",myList.get(position)+"");
                intent.putStringArrayListExtra("Array",line);
                startActivity(intent);
            }
        });
    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ListFilesActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ListFilesActivity.this,  android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(ListFilesActivity.this,"Write External Storage permission allows us to read  files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(ListFilesActivity.this, new String[]
                    {android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }
}

