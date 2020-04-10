package emgsignal.v3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Add_Sensor_Activity extends AppCompatActivity {
    private EditText et_type, et_resMid, et_resEnd, et_resRef;
    private String sensorID;
    private Button btnAdd;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
        btnAdd = findViewById(R.id.btn_addSensor);
        et_type = findViewById(R.id.typeSensor);
        et_resMid = findViewById(R.id.res_m);
        et_resEnd = findViewById(R.id.res_e);
        et_resRef = findViewById(R.id.res_r);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorID = getDate();
                dbManager = new DBManager(Add_Sensor_Activity.this);
                dbManager.addSensor(new SensorFormat(
                        et_type.getText().toString().trim(),
                        et_resMid.getText().toString().trim(),
                        et_resEnd.getText().toString().trim(),
                        et_resRef.getText().toString().trim(),
                        sensorID));
                EmptyField();
                Toast.makeText(Add_Sensor_Activity.this,"Sensor created successfully: ID " + sensorID, Toast.LENGTH_LONG).show();
            }
        });

    }
    public String getDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("HHmmss_ddMMyyyy");
        String Date = simpledateformat.format(calendar.getTime());
        return Date;
    }
    public void EmptyField(){
        et_type.setText("");
        et_resMid.setText("");
        et_resEnd.setText("");
        et_resRef.setText("");
    }
}
