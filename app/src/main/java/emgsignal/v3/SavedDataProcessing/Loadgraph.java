package emgsignal.v3.SavedDataProcessing;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import emgsignal.v3.R;
import emgsignal.v3.SignalProcessing.Complex;
import emgsignal.v3.SignalProcessing.Detrend;
import emgsignal.v3.SignalProcessing.FFT;
import emgsignal.v3.SignalProcessing.Helper;

public class Loadgraph extends AppCompatActivity {
    private LineGraphSeries<DataPoint> fftSeries, timeSeries, dB_fftSeries;
    private static final double Fs = 1000;
    public Double[]  amplitude, absFFT , dB_FFT;
    private int lengthdata;
    String TAG = "DATA PROCESSING";
    TabHost tabHost;
    GraphView time_graph, frequency_graph;
    RadioGroup radioGroup;
    double maxTimeSignal , minTimeSignal , maxFFTSignal , minFFTSignal , maxDBSignal , minDBSignal;
    Button btn_scaleTimeSignal, btn_scaleFFTsignal;
    double maxYTimeGraph = 10;
    double maxYFFTGraph = 0.01;
    int rd_check = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_view);

        //Get intent - data from realtime signal saved
        final Intent getData = getIntent();
        lengthdata = getData.getIntExtra("Length", 1);
        double[] timedata = getData.getDoubleArrayExtra("TimeData");
        Log.i("CHECKING LONG", "long data " + lengthdata);

        amplitude = new Double[lengthdata];
        for (int i = 0; i < lengthdata; i++) {
            amplitude[i] = Double.valueOf(timedata[i]);
        }

        //Set up tabHost
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        time_graph = findViewById(R.id.time_chart);
        frequency_graph = findViewById(R.id.frequency_chart);

        //create Time series
        timeSeries = new LineGraphSeries<>();
        timeSeries.setColor(Color.RED);
        timeSeries.setThickness(2);
        for(int k=0; k< lengthdata ; k++) {
            timeSeries.appendData(new DataPoint(k , amplitude[k]), true, lengthdata);
        }
        time_graph.addSeries(timeSeries);
        time_graph.getViewport().setMaxX(lengthdata/3);

        //min,max time signal
        minTimeSignal = getMin(amplitude);
        Log.i(TAG, "MIN value of amplitude: " + minTimeSignal);
        maxTimeSignal = getMax(amplitude);
        Log.i(TAG, "MAX value of amplitude: " + maxTimeSignal);

        //draw time graph
        drawTimeGraph(time_graph , -maxYTimeGraph , maxYTimeGraph , lengthdata/3);

        btn_scaleTimeSignal = findViewById(R.id.btn_scaleTimeSignal);
        btn_scaleTimeSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_btn = btn_scaleTimeSignal.getText().toString().trim();
                if (text_btn.equals("Fit Amplitude")) {
                    drawTimeGraph(time_graph , minTimeSignal , maxTimeSignal , lengthdata/3);
                    btn_scaleTimeSignal.setText("Zoom out");
                } else {
                    drawTimeGraph(time_graph , -maxYTimeGraph, maxYTimeGraph , lengthdata/3);
                    btn_scaleTimeSignal.setText(R.string.fit_amplitude);
                }

            }
        });

        //CreateFFT Series
        fftSeries = new LineGraphSeries<>();
        fftSeries.setColor(Color.RED);
        fftSeries.setThickness(2);
        dB_fftSeries = new LineGraphSeries<>();
        dB_fftSeries.setColor(Color.BLUE);
        dB_fftSeries.setThickness(2);

        Double[] ff = Helper.appendZeros(amplitude);
        Complex[] fft = FFT.fft(ff);
        absFFT = new Double[fft.length];
        dB_FFT = new Double[fft.length];
        for(int k = 0 ; k < fft.length ; k++){
            //normalized FFT signal by dividing for the length of the signal
            absFFT[k] = Complex.abs(fft[k])/fft.length;
        }
        for(int k1 = 0 ; k1 < absFFT.length ; k1++){
            dB_FFT[k1] = 20*Math.log(absFFT[k1])/Math.log(10);
        }
        //min, max frequency
        minFFTSignal = getMin(absFFT);
        maxFFTSignal = getMax(absFFT);
        minDBSignal = getMin(dB_FFT);
        maxDBSignal = getMax(dB_FFT);

        for(int k1=0; k1< absFFT.length ; k1++){
            fftSeries.appendData(new DataPoint(k1*Fs/absFFT.length,absFFT[k1]),true,absFFT.length);
        }
        for(int k1=0; k1< absFFT.length ; k1++){
            dB_fftSeries.appendData(new DataPoint(k1*Fs/absFFT.length,dB_FFT[k1]),true,absFFT.length);
        }

        drawFrequencyGraph(frequency_graph , fftSeries , 0 , maxYFFTGraph , 500);
        //>>>>

        radioGroup = findViewById(R.id.group_radio);
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup
                        .OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        // Get the selected Radio Button
                        int selected = radioGroup.getCheckedRadioButtonId();
                        switch (selected){
                            case R.id.rd_fft_unit:
                                rd_check = 0;
                                btn_scaleFFTsignal.setText(R.string.fit_amplitude);
                                drawFrequencyGraph(frequency_graph , fftSeries , 0 , maxYFFTGraph , 500);
                                break;
                            case R.id.rd_db_unit:
                                rd_check = 1;
                                btn_scaleFFTsignal.setText(R.string.fit_amplitude);
                                drawFrequencyGraph(frequency_graph , dB_fftSeries , -150, -10, 500);
                                break;
                        }
                    }
                });

        btn_scaleFFTsignal = findViewById(R.id.btn_scaleFFTSignal);
        btn_scaleFFTsignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_btn = btn_scaleFFTsignal.getText().toString().trim();
                if (text_btn.equals("Fit Amplitude")) {
                    if (rd_check == 0) {
                        drawFrequencyGraph(frequency_graph , fftSeries , minFFTSignal , maxFFTSignal , 500);
                    } else
                        drawFrequencyGraph(frequency_graph , dB_fftSeries , minDBSignal, maxDBSignal, 500);
                    btn_scaleFFTsignal.setText("Zoom out");
                } else {
                    if (rd_check == 0) {
                        drawFrequencyGraph(frequency_graph , fftSeries , 0 , maxYFFTGraph , 500);
                    } else
                        drawFrequencyGraph(frequency_graph , dB_fftSeries , -150, -10, 500);
                    btn_scaleFFTsignal.setText(R.string.fit_amplitude);
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

    public void drawTimeGraph(GraphView graph , double minY , double maxY , double maxX){
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        {
            timeSeries = new LineGraphSeries<>();
            timeSeries.setColor(Color.RED);
            timeSeries.setThickness(2);
            for(int k=0; k< lengthdata ; k++) {
                timeSeries.appendData(new DataPoint(k , amplitude[k]), true, lengthdata);
            }
            graph.addSeries(timeSeries);
            graph.getViewport().setMaxX(maxX);
            graph.getViewport().setMinY(minY);
            graph.getViewport().setMaxY(maxY);
            graph.getGridLabelRenderer().setNumVerticalLabels(10);
        }
    }
    public void drawFrequencyGraph(GraphView graph , LineGraphSeries graphSeries , double minY , double maxY , double maxX){
        graph.removeAllSeries();
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        //graph.getGridLabelRenderer().setNumHorizontalLabels(10);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);
        graph.getViewport().setMaxX(maxX);
        graph.getViewport().setMaxY(maxY);
        graph.getViewport().setMinY(minY);
        graph.addSeries(graphSeries);

    }
    public double getMax(Double[] arr){
        double max = arr[0];
        for(int counter = 0 ; counter < arr.length ; counter++){
            if(arr[counter] > max){
                max = arr[counter];
            }
        }
        return max;
    }
    public double getMin(Double[] arr){
        double min = arr[0];
        for(int counter = 0 ; counter < arr.length ; counter++){
            if(arr[counter] < min){
                min = arr[counter];
                Log.i(TAG, "getMin: position = " + counter);
            }
        }
        return min;
    }


    public void onRadioButtonClicked(View view) {
    }
}