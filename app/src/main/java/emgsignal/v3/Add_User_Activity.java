package emgsignal.v3;

import android.app.DatePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Add_User_Activity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private EditText testee_name, testee_height, testee_weight, testee_res, testee_birthday;
    private String testee_id;
    private Button btn_addUser;
    private DBManager dbManager;
    private UserFormat userFormat;
    SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        testee_name = findViewById(R.id.testee_name);
        testee_height = findViewById(R.id.testee_height);
        testee_weight = findViewById(R.id.testee_weight);
        testee_res = findViewById(R.id.testee_R);
        btn_addUser = findViewById(R.id.btn_addUser);
        testee_birthday = findViewById(R.id.testee_birthday);
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        testee_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        btn_addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbManager = new DBManager(Add_User_Activity.this);
                testee_id = testee_birthday.getText().toString().replace("/", "");
                dbManager.addUser(new UserFormat(
                        testee_name.getText().toString().trim(),
                        testee_birthday.getText().toString().trim(),
                        testee_height.getText().toString().trim(),
                        testee_weight.getText().toString().trim(),
                        testee_res.getText().toString().trim(),
                        testee_id)
                );
                EmptyField();
                Toast.makeText(Add_User_Activity.this,"User created successfully: ID = " + testee_id, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDatePickerDialog(){
        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog( this ,android.R.style.Theme_Holo_Dialog, this,
                newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        datePickerDialog.show();
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar newDate = Calendar.getInstance();
        newDate.set(year, month, day);
        testee_birthday.setText(dateFormatter.format(newDate.getTime()));
    }
    private void EmptyField(){
        testee_name.setText("");
        testee_birthday.setText("");
        testee_height.setText("");
        testee_weight.setText("");
        testee_res.setText("");
    }
}
