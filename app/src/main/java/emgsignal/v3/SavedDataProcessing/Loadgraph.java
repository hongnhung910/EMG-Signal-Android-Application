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
    public double[] data_removeDC, rawData;
    private int lengthdata;
    String TAG = "DATA PROCESSING";
    TabHost tabHost;
    GraphView time_graph, frequency_graph;
    RadioGroup radioGroup;
    List<Double> list = new ArrayList<>();
    double maxTimeSignal , minTimeSignal , maxFFTSignal , minFFTSignal , maxDBSignal , minDBSignal;
    int countTime = 3, countXTime = 0;
    int countFrequency = 3, countXFrequency = 0;
    Button yOutTimeSignal , yInTimeSignal , xOutTimeSignal , xInTimeSignal;
    Button yOutFSignal , yInFSignal , xOutFSignal , xInFSignal;
    TextView yScaleText , xScaleText;
    TextView yScaleFText , xScaleFText;
    private Detrend removeDC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_view);

        //set intial scale times
        yScaleText = findViewById(R.id.ytimes);
        yScaleText.setText(Integer.toString(countTime));

        xScaleText = findViewById(R.id.xtimes);
        xScaleText.setText(Integer.toString(countXTime));

        yScaleFText = findViewById(R.id.yFtimes);
        yScaleFText.setText(Integer.toString(countFrequency));

        xScaleFText = findViewById(R.id.xFtimes);
        xScaleFText.setText(Integer.toString(countXFrequency));

        //Get intent - data from realtime signal saved
        final Intent getData = getIntent();
        lengthdata = getData.getIntExtra("Length", 1);
        double[] timedata = getData.getDoubleArrayExtra("TimeData");

        //Detrend Signal
        rawData = new double[lengthdata];
        data_removeDC = new double[lengthdata];
        amplitude = new Double[lengthdata];
        removeDC = new Detrend();
        for (int i = 0; i < lengthdata; i++) {
            rawData[i] = Double.valueOf(timedata[i]);
        }
        data_removeDC = removeDC.detrend(rawData);
        for (int i = 0; i < lengthdata; i++) {
            amplitude[i] = Double.valueOf(data_removeDC[i]);
        }

        //Set up tabHost
        tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        time_graph = findViewById(R.id.time_chart);
        frequency_graph = findViewById(R.id.frequency_chart);
        //min,max time signal
        minTimeSignal = getMin(amplitude);
        Log.i(TAG, "MIN value of amplitude: " + minTimeSignal);
        maxTimeSignal = getMax(amplitude);
        Log.i(TAG, "MAX value of amplitude: " + maxTimeSignal);
        //>>>
        //draw time graph
        drawTimeGraph(time_graph , minTimeSignal - (3-countTime)*100 , maxTimeSignal + (3-countTime)*100 , lengthdata/3 - countXTime*2500);
        //button for Y axis

        yOutTimeSignal = findViewById(R.id.yout);
        yOutTimeSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countTime > 0) {
                    countTime--;
                    yScaleText.setText(Integer.toString(countTime));
                    drawTimeGraph(time_graph , minTimeSignal - (3-countTime)*100 , maxTimeSignal + (3-countTime)*100 , lengthdata/3 - countXTime*2500);
                }
            }
        });
        yInTimeSignal = findViewById(R.id.yin);
        yInTimeSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countTime < 3) {
                    countTime++;
                    yScaleText.setText(Integer.toString(countTime));
                    drawTimeGraph(time_graph , minTimeSignal - (3-countTime)*100 , maxTimeSignal + (3-countTime)*100 , lengthdata/3 - countXTime*2500);
                }
            }
        });

        //button for X axis

        xOutTimeSignal = findViewById(R.id.xout);
        xOutTimeSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countXTime > 0) {
                    countXTime--;
                    xScaleText.setText(Integer.toString(countXTime));
                    drawTimeGraph(time_graph , minTimeSignal - (3-countTime)*100 , maxTimeSignal + (3-countTime)*100 , lengthdata/3 - countXTime*2500);
                }
            }
        });
        xInTimeSignal = findViewById(R.id.xin);
        xInTimeSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countXTime < 3) {
                    countXTime++;
                    xScaleText.setText(Integer.toString(countXTime));
                    drawTimeGraph(time_graph , minTimeSignal - (3-countTime)*100 , maxTimeSignal + (3-countTime)*100 , lengthdata/3 - countXTime*2500);
                }
            }
        });
        //create Time series
        {
            timeSeries = new LineGraphSeries<>();
            timeSeries.setColor(Color.RED);
            timeSeries.setThickness(2);
            for(int k=0; k< lengthdata ; k++) {
                timeSeries.appendData(new DataPoint(k , amplitude[k]), true, lengthdata);
            }
            time_graph.addSeries(timeSeries);
            time_graph.getViewport().setMaxX(lengthdata/3);
            //time_graph.getViewport().setMaxY(4000);
        }

        //CreateFFT Series
        frequency_graph.getViewport().setMaxX(Fs/3);
        frequency_graph.getViewport().setMaxY(30);
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
                                drawFrequencyGraph(frequency_graph , absFFT , minFFTSignal - (3 - countFrequency)*10 , 10 + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                                break;
                            case R.id.rd_db_unit:
                                drawFrequencyGraph(frequency_graph , dB_FFT , minDBSignal - (3 - countFrequency)*10 , maxDBSignal + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                                break;
                        }
                    }
                });
        //button for Y Frequency axis
        yOutFSignal = findViewById(R.id.yFout);
        yOutFSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countFrequency > 0) {
                    countFrequency--;
                    yScaleFText.setText(Integer.toString(countFrequency));
                    if(radioGroup.getCheckedRadioButtonId() == R.id.rd_fft_unit){
                        drawFrequencyGraph(frequency_graph , absFFT , minFFTSignal - (3 - countFrequency)*10 , 10 + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }else{
                        drawFrequencyGraph(frequency_graph , dB_FFT , minDBSignal - (3 - countFrequency)*10 , maxDBSignal + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }
                }
            }
        });
        yInFSignal = findViewById(R.id.yFin);
        yInFSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countFrequency < 3) {
                    countFrequency++;
                    yScaleFText.setText(Integer.toString(countFrequency));
                    if(radioGroup.getCheckedRadioButtonId() == R.id.rd_fft_unit){
                        drawFrequencyGraph(frequency_graph , absFFT , minFFTSignal - (3 - countFrequency)*10 , 10 + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }else{
                        drawFrequencyGraph(frequency_graph , dB_FFT , minDBSignal - (3 - countFrequency)*10 , maxDBSignal + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }
                }
            }
        });

        //button for X Frequency axis

        xOutFSignal = findViewById(R.id.xFout);
        xOutFSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countXFrequency > 0) {
                    countXFrequency--;
                    xScaleFText.setText(Integer.toString(countXFrequency));
                    if(radioGroup.getCheckedRadioButtonId() == R.id.rd_fft_unit){
                        drawFrequencyGraph(frequency_graph , absFFT , minFFTSignal - (3 - countFrequency)*10 , 10 + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }else{
                        drawFrequencyGraph(frequency_graph , dB_FFT , minDBSignal - (3 - countFrequency)*10 , maxDBSignal + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }
                }
            }
        });
        xInFSignal = findViewById(R.id.xFin);
        xInFSignal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countXFrequency < 3) {
                    countXFrequency++;
                    xScaleFText.setText(Integer.toString(countXFrequency));
                    if(radioGroup.getCheckedRadioButtonId() == R.id.rd_fft_unit){
                        drawFrequencyGraph(frequency_graph , absFFT , minFFTSignal - (3 - countFrequency)*10 , 10 + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }else{
                        drawFrequencyGraph(frequency_graph , dB_FFT , minDBSignal - (3 - countFrequency)*10 , maxDBSignal + ( 3- countFrequency)*10 , Fs/3 - countXFrequency*100);
                    }
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
        graph.removeAllSeries();
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScrollable(true);
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
        }
    }
    public void drawFrequencyGraph(GraphView graph , Double[] data , double minY , double maxY , double maxX){
        graph.removeAllSeries();
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setScrollable(true);

        if(radioGroup.getCheckedRadioButtonId() == R.id.rd_fft_unit){
            fftSeries = new LineGraphSeries<>();
            fftSeries.setColor(Color.RED);
            fftSeries.setThickness(2);

            for(int k1=0; k1< data.length ; k1++){
                fftSeries.appendData(new DataPoint(k1*Fs/data.length,data[k1]),true,data.length);
            }
            frequency_graph.addSeries(fftSeries);
            frequency_graph.getViewport().setMaxX(maxX);
            frequency_graph.getViewport().setMaxY(maxY);
            frequency_graph.getViewport().setMinY(minY);
        }else {
            dB_fftSeries = new LineGraphSeries<>();
            dB_fftSeries.setColor(Color.BLUE);
            dB_fftSeries.setThickness(2);

            for(int k1=0; k1< data.length ; k1++){
                dB_fftSeries.appendData(new DataPoint(k1*Fs/data.length,data[k1]),true,data.length);
            }
            frequency_graph.addSeries(dB_fftSeries);
            frequency_graph.getViewport().setMaxX(maxX);
            frequency_graph.getViewport().setMaxY(maxY);
            frequency_graph.getViewport().setMinY(minY);
        }
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
            }
        }
        return min;
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