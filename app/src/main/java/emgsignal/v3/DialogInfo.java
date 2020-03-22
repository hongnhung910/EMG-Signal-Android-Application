package emgsignal.v3;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.reflect.Array;

public class DialogInfo extends AppCompatActivity {
    private Button btn_next1, btn_next2, btn_save;
    private EditText et_testee_name, et_testee_height, et_testee_weight, et_testee_res,
                    et_sensor_name, et_res_m, et_res_r, et_res_e, et_humid, et_temp;
    private String testee_name, testee_height, testee_weight, testee_res, sensor_name, res_m, res_r, res_e, humid, temp;

    public void showdialog(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_info_testee);
        Window window = dialog.getWindow();

        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        btn_next1 = dialog.findViewById(R.id.btn_next1);
        et_testee_name = dialog.findViewById(R.id.testee_name);
        et_testee_height = dialog.findViewById(R.id.testee_height);
        et_testee_weight = dialog.findViewById(R.id.testee_weight);
        et_testee_res = dialog.findViewById(R.id.testee_R);

        testee_height = et_testee_height.getText().toString().trim();
        testee_weight = et_testee_weight.getText().toString().trim();
        testee_res = et_testee_res.getText().toString().trim();

        btn_next1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setContentView(R.layout.dialog_info_sensor);
                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                btn_next2 = dialog.findViewById(R.id.btn_next2);
                et_sensor_name = dialog.findViewById(R.id.sensor_name);
                et_res_e = dialog.findViewById(R.id.res_e);
                et_res_m = dialog.findViewById(R.id.res_m);
                et_res_r = dialog.findViewById(R.id.res_r);
                sensor_name = et_sensor_name.getText().toString().trim();
                res_e = et_res_e.getText().toString().trim();
                res_m = et_res_m.getText().toString().trim();
                res_r = et_res_r.getText().toString().trim();

                btn_next2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.setContentView(R.layout.dialog_info_weather);
                        Window window = dialog.getWindow();
                        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        btn_save = dialog.findViewById(R.id.btn_log_save);
                        et_humid = dialog.findViewById(R.id.humid);
                        et_temp = dialog.findViewById(R.id.temp);
                        humid = et_humid.getText().toString().trim();
                        temp = et_temp.getText().toString().trim();

                        btn_save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText (context, "Saved", Toast.LENGTH_SHORT).show ();
                                dialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }
}
