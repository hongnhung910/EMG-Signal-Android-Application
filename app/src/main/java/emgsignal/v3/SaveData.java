package emgsignal.v3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SaveData extends AppCompatActivity {
    private static final String LOG_TAG_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";
    private String LOG_TAG_CHECK_SAVING = "Checking saving";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    public void save(final ArrayList<Double> dataSave, Context context) {

        try {
            Log.i(LOG_TAG_CHECK_SAVING, "save: trying");
            if (ExternalStorageUtil.isExternalStorageMounted()) {
                Log.i(LOG_TAG_CHECK_SAVING, "save: in if");
                // Check whether this app has write external storage permission or not.
                int writeExternalStoragePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // If do not grant write external storage permission.
                if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    // Request user to grant write external storage permission.
                    ActivityCompat.requestPermissions(SaveData.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
                } else {

                    File sdCard = Environment.getExternalStorageDirectory();
                    if (sdCard.exists()) {
                        File publicDcimDirPath = new File(sdCard.getAbsolutePath() + "/EMG_Data");

                        if (!publicDcimDirPath.exists()) {
                            publicDcimDirPath.mkdirs();
                            Log.i("making", "Creating Directory: " + publicDcimDirPath);
                        }

                        File newFile = new File(publicDcimDirPath, nameFile()+".txt");

                        OutputStreamWriter writer = null;
                        try {
                            writer = new OutputStreamWriter(new FileOutputStream(newFile));
                            for (int i = 0; i < dataSave.size(); i++) {
                                Log.i("writer", "Writing to file");
                                writer.write((dataSave.get(i) + "\n"));
                            }
                            writer.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                            }
                        }

                        Toast.makeText(context, "Save to public external storage success. File Path " + newFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                }

            }}
        catch (Exception ex)
        {
            Log.e(LOG_TAG_EXTERNAL_STORAGE, ex.getMessage(), ex);

            //Toast.makeText(getApplicationContext(), "Save to public external storage failed. Error message is " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public String nameFile(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd_HH'h'mm'm'ss's'");
        String Date = simpledateformat.format(calendar.getTime());
        return Date;
    }
}
