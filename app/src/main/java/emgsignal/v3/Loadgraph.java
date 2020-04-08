package emgsignal.v3;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Loadgraph extends AppCompatActivity {
    private LineGraphSeries<DataPoint> fftSeries, timeSeries, dB_fftSeries;
    private static final double Fs = 1000;
    public Double[] voltage, absFFT;
    Integer[] domain;
    int length;
    String TAG = "SAVE_DATA";
    TabHost tabHost;
    GraphView time_graph, frequency_graph;
    RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_view);

        //Get intent - data from realtime signal saved
        {
            final Intent getData = getIntent();
            length = getData.getIntExtra("Length", 1);
            double[] timedata = getData.getDoubleArrayExtra("TimeData");
            int[] domainLabels = getData.getIntArrayExtra("DomainLabels");
            voltage = new Double[length];
            domain = new Integer[length];
            for (int i = 0; i < length; i++) {
                voltage[i] = Double.valueOf(timedata[i]);
                domain[i] = Integer.valueOf(domainLabels[i]);
            }
        }

        //Set up tabHost
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        time_graph = findViewById(R.id.time_chart);
        frequency_graph = findViewById(R.id.frequency_chart);

        setGraph(time_graph);
        setGraph(frequency_graph);
        //create Time series
        {
            timeSeries = new LineGraphSeries<>();
            timeSeries.setColor(Color.RED);
            timeSeries.setThickness(2);
            for(int k=0; k< voltage.length ; k++) {
                timeSeries.appendData(new DataPoint(k , voltage[k]), true, voltage.length);
            }
            time_graph.addSeries(timeSeries);
            time_graph.getViewport().setMaxX(voltage.length/3);
            time_graph.getViewport().setMaxY(4000);
        }

        //CreateFFT Series
        frequency_graph.getViewport().setMaxX(Fs/3);
        frequency_graph.getViewport().setMaxY(30);
        fftSeries = new LineGraphSeries<>();
        fftSeries.setColor(Color.RED);
        fftSeries.setThickness(2);
        dB_fftSeries = new LineGraphSeries<>();
        dB_fftSeries.setColor(Color.BLUE);
        dB_fftSeries.setThickness(2);
        Double[] ff = Helper.appendZeros(voltage);
        Complex[] fft = FFT.fft(ff);
        absFFT = new Double[fft.length];

        for(int k = 0 ; k < fft.length ; k++){
            //normalized FFT signal by dividing for the length of the signal
            absFFT[k] = Complex.abs(fft[k])/fft.length;
        }
        for(int k1=0; k1< absFFT.length ; k1++){
            fftSeries.appendData(new DataPoint(k1*Fs/absFFT.length,absFFT[k1]),true,absFFT.length);
        }
        for(int k1=0; k1< absFFT.length ; k1++){
            dB_fftSeries.appendData(new DataPoint(k1*Fs/absFFT.length,20*Math.log(absFFT[k1])/Math.log(10)),true,absFFT.length);
        }
        frequency_graph.addSeries(fftSeries);
        //>>>>

        radioGroup = findViewById(R.id.group_radio);
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup
                        .OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group,
                                                 int checkedId)
                    {
                        // Get the selected Radio Button
                        int selected = radioGroup.getCheckedRadioButtonId();
                        switch (selected){
                            case R.id.rd_fft_unit:
                                frequency_graph.removeAllSeries();
                                frequency_graph.getViewport().setMaxX(Fs/3);
                                frequency_graph.getViewport().setMaxY(30);
                                frequency_graph.getViewport().setMinY(0);
                                frequency_graph.addSeries(fftSeries);
                                break;
                            case R.id.rd_db_unit:
                                frequency_graph.removeAllSeries();
                                frequency_graph.getViewport().setMinY(-60);
                                frequency_graph.getViewport().setMaxY(60);
                                frequency_graph.addSeries(dB_fftSeries);
                                break;
                        }
                    }
                });

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("Time domain");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Time domain");
        tabHost.addTab(spec);
        tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#2763a3"));
        TextView tv = tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).findViewById(android.R.id.title);
        tv.setTextColor(Color.WHITE);

        //Tab 2
        spec = tabHost.newTabSpec("Frequency domain");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Frequency domain");
        tabHost.addTab(spec);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int tab = tabHost.getCurrentTab();
                for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                    // When tab is not selected
                    tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#444444"));
                    TextView tv = tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
                    tv.setTextColor(Color.BLACK);
                }
                // When tab is selected
                tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#2763a3"));
                TextView tv =  tabHost.getTabWidget().getChildAt(tab).findViewById(android.R.id.title);
                tv.setTextColor(Color.WHITE);
            }
        });

    }


    public void setGraph(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        //scrollable
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollableY(true);

    }

    public void onRadioButtonClicked(View view) {
    }
}