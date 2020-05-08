package emgsignal.v3.SavedDataProcessing;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SaveData extends AppCompatActivity {

    public void save(final ArrayList<Double> dataSave, String username,  String sensor ,
                     String testee_info , String sensor_res , String environment, String notes ) {

                    File sdCard = Environment.getExternalStorageDirectory();
                    if (sdCard.exists()) {
                        File publicDcimDirPath = new File(sdCard.getAbsolutePath() + "/EMG_Data/" + username);

                        if (!publicDcimDirPath.exists()) {
                            publicDcimDirPath.mkdirs();
                            Log.i("making", "Creating Directory: " + publicDcimDirPath);
                        }
                        //get name of user
                        int index = username.indexOf('-');
                        String substring = username.substring(0,index).replaceAll("\\s+", "");
                        String nameFile = getDate();
                        nameFile = nameFile +"_"+ substring + "_" +  notes;
                        File newFile = new File(publicDcimDirPath, nameFile+".txt");

                        OutputStreamWriter writer = null;
                        try {
                            writer = new OutputStreamWriter(new FileOutputStream(newFile));
                            writer.write("Tesste: " + username + ", " + testee_info + "\n"
                                    + "Sensor: " + sensor + ", " + sensor_res + "\n"
                                    + environment + "\n"
                                    +"----------------------------- \n");
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
                    }
                }
    public String getDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd_HH'h'mm'm'");
        String Date = simpledateformat.format(calendar.getTime());
        return Date;
    }
}
