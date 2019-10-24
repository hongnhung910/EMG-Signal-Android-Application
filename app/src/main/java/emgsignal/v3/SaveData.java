package emgsignal.v3;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SaveData {
    public void save(final ArrayList<Double> dataSave) {

        String filename = nameFile()+".txt";
        OutputStreamWriter writer = null;

        File sdCard = Environment.getExternalStorageDirectory();
        if (sdCard.exists()) {
            File directory = new File(sdCard.getAbsolutePath() + "/EMG_Data");

            if (!directory.exists()) {
                directory.mkdirs();
                Log.i("making", "Creating Directory: " + directory);
            }

            Log.i("made", "Directory found: " + directory);

            File newFile = new File(directory, filename);
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

        }
    }
    public String nameFile(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd_HH'h'mm'm'ss's'");
        String Date = simpledateformat.format(calendar.getTime());
        return Date;
    }
}
