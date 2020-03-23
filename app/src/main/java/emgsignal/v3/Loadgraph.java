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
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Loadgraph extends AppCompatActivity {
    private LineGraphSeries<DataPoint> fftSeries, timeSeries;
    private static final double Fs = 1000;
    public Double[] voltage;
    Integer[] domain;
    int length;
    String TAG = "SAVE_DATA";
    TabHost tabHost;
    GraphView time_graph, frequency_graph;

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
        createTimeseries();
        createFFTseries();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("Time domain");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Time domain");
        tabHost.addTab(spec);

        //Tab 2
        spec = tabHost.newTabSpec("Frequency domain");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Frequency domain");
        tabHost.addTab(spec);

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

    public void createTimeseries(){
        timeSeries = new LineGraphSeries<DataPoint>();
        timeSeries.setColor(Color.RED);
        timeSeries.setThickness(2);
        for(int k=0; k< voltage.length ; k++) {
            timeSeries.appendData(new DataPoint(k , voltage[k]), true, voltage.length);
        }
        time_graph.addSeries(timeSeries);
        time_graph.getViewport().setMaxX(voltage.length/3);
        time_graph.getViewport().setMaxY(4000);
    }

    public void createFFTseries(){
        Double[] ff = Helper.appendZeros(voltage);
        for(int k0 = 0 ; k0 < ff.length ; k0++){
            ff[k0] = ff[k0];
        }
        Complex[] fft = FFT.fft(ff);
        Double[] absFFT = new Double[fft.length];

        for(int k = 0 ; k < fft.length ; k++){
            //normalized FFT signal by dividing for the length of the signal
            absFFT[k] = Complex.abs(fft[k])/fft.length;
        }

        fftSeries = new LineGraphSeries<DataPoint>();
        fftSeries.setColor(Color.RED);
        fftSeries.setThickness(2);
        for(int k1=0; k1< absFFT.length ; k1++){
            fftSeries.appendData(new DataPoint(k1*Fs/absFFT.length,absFFT[k1]),true,absFFT.length);
        }

        frequency_graph.addSeries(fftSeries);
        frequency_graph.setTitleColor(Color.BLUE);
        frequency_graph.getViewport().setMaxX(Fs/3);
        frequency_graph.getViewport().setMaxY(30);
    }

}