package emgsignal.v3;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import emgsignal.v3.R;

public class LoadDataActivity extends AppCompatActivity {


    private TextView show_data, nameFile;
    private Button graphing;
    private ArrayList<String> get_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);

        nameFile = findViewById(R.id.nameFile);
        show_data = findViewById(R.id.show_data);
        graphing = findViewById(R.id.graphing);

        final Intent getData = getIntent();
        nameFile.setText(getData.getStringExtra("Namefile"));
        show_data.setText(getData.getStringExtra("Data"));

        get_data = new ArrayList<>();
        get_data = getData.getStringArrayListExtra("Array");

        graphing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoadDataActivity.this,PlotDataActivity.class);
                intent.putStringArrayListExtra("Values",get_data);
                startActivity(intent);
            }
        });
    }
}
