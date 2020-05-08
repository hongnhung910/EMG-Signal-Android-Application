/*Show data files saved in external storage*/

package emgsignal.v3.SavedDataProcessing;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import emgsignal.v3.R;
import emgsignal.v3.SignalProcessing.Detrend;


public class ListFilesActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    ArrayList<String> myList;
    ListView listView;
    ArrayList<String> ArrayData = new ArrayList<>();
    String nameFolder;
    private ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_datafile);
        Intent getNameFolder = getIntent();
        nameFolder = getNameFolder.getStringExtra("NameFolder");
        listView = findViewById(R.id.list_dataFile);
        myList = new ArrayList<>();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data/"+nameFolder);
                    if (dir.exists()) {
                        Log.d("path", dir.toString());
                        File[] list = dir.listFiles();
                        Arrays.sort(list, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                        for (int i = 0; i < list.length; i++) {
                            myList.add(list[i].getName());
                        }
                        ArrayAdapter arrayAdapter = new ArrayAdapter(ListFilesActivity.this, android.R.layout.simple_list_item_1, myList);
                        listView.setAdapter(arrayAdapter);
                    }
                } else {
                    requestPermission(); // Code for permission
                }
            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data/"+nameFolder);

                if (dir.exists()) {
                    Log.d("path", dir.toString());
                    File list[] = dir.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        myList.add(list[i].getName());
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ListFilesActivity.this, android.R.layout.simple_list_item_1, myList);
                    listView.setAdapter(arrayAdapter);
                }
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/EMG_Data/"+nameFolder+"/"+ myList.get(position));
                ArrayData = ReadFile(file);
                Detrend removeDC = new Detrend();
                int lengthData;
                    if (ArrayData.size()>30000) lengthData = 30000;
                        else lengthData = ArrayData.size();
                Log.i("CHECKING LONG", "Length of data array " + lengthData);
                double[] timedata = new double[lengthData];
                double[] data_removeDC = new double[lengthData];
                for (int i=0;i<lengthData;i++) {
                    if (isNumberic(ArrayData.get(i))){
                        timedata[i] = (Double.valueOf(ArrayData.get(i)))/101;
                    } else timedata[i] = 0;
                    Log.i("CHECKING VALUE", "data " + timedata[i]);
                }
                data_removeDC = removeDC.detrend(timedata);
                for (int i=0; i<4; i++) {
                    data_removeDC[i] = 0;
                }

                Log.i("CHECKING LONG", "long data " + timedata.length);
                Intent intent = new Intent(ListFilesActivity.this,Loadgraph.class);
                intent.putExtra("Namefile",myList.get(position)+"");
                intent.putExtra("TimeData",data_removeDC);
                intent.putExtra("Length",timedata.length);
                startActivity(intent);
            }
        });

        //long click delete file
        adapter = new ArrayAdapter(ListFilesActivity.this , android.R.layout.simple_list_item_1 , myList);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final int which_item = position;
                final String item = parent.getItemAtPosition(position).toString() ;
                //get name of the file want to delete

                new AlertDialog.Builder(ListFilesActivity.this)
                        .setTitle("Do you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/EMG_Data/" + nameFolder + "/" + myList.get(position);
                                Log.d("pathmother", path);
                                File file = new File(path);
                                boolean deleted = false;
                                if(file.exists()){
                                    Log.d("ReadFile: ", "file's found");
                                    deleted = file.delete();
                                }else{
                                    Log.d("ReadFileException: ", "cannot find the file");
                                }

                                if(deleted == true) {
                                    myList.remove(which_item);
                                    adapter.notifyDataSetChanged();
                                    Toast toastSuccess = Toast.makeText(getApplicationContext() , item + " is deleted" , Toast.LENGTH_LONG);
                                    toastSuccess.show();
                                }else{
                                    Toast toastFailed = Toast.makeText(getApplicationContext() , item + " is not deleted, failed" , Toast.LENGTH_LONG);
                                    toastFailed.show();
                                }
                            }
                        })
                        .setNegativeButton("No" , null)
                        .show();
                return true;
            }
        });
    }
    private ArrayList<String> ReadFile(File file) {

        String line = null;
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
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
    private boolean isNumberic(String s) {
        boolean numeric = true;

        try {
            Double num = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            numeric = false;
        }
        return numeric;
    }

}

