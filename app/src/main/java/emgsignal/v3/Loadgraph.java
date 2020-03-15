package emgsignal.v3;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

public class Loadgraph extends AppCompatActivity {

    private XYPlot plot;
    private Redrawer redrawer;


    public Double[] voltage;
    Integer[] domain;
    int length;
    String TAG = "SAVE_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadgraph);
        plot = findViewById(R.id.plot);
        Log.i(TAG, "onCreate: starting to draw graph...................");
        final Intent getData = getIntent();
        length = getData.getIntExtra("Length",1);
        Log.i(TAG, "onCreate: Size : " + length +"...................");


        double[] timedata = getData.getDoubleArrayExtra("TimeData");
        int[] domainLabels = getData.getIntArrayExtra("DomainLabels");

        voltage = new Double[length];
        domain = new Integer[length];
        for (int i=0; i<length; i++) {
            voltage[i] = Double.valueOf(timedata[i]);
            domain[i] = Integer.valueOf(domainLabels[i]);
        }


        //plot graph
        // turn array data into XY Series

        //plot.setDomainBoundaries(0,length, BoundaryMode.FIXED);
        //plot.setDomainStep(StepMode.SUBDIVIDE, 5);


        EMGModel emgSeries = new EMGModel(200, voltage);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("0"));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("0"));


        // add a new series' to the xyplot:
        MyFadeFormatter formatter =new MyFadeFormatter(30000);
        formatter.setLegendIconEnabled(false);
        plot.addSeries(emgSeries, formatter);
        plot.setRangeBoundaries(0, 5000, BoundaryMode.GROW);
        plot.setDomainBoundaries(0, voltage.length, BoundaryMode.FIXED);

        // reduce the number of range labels
        plot.setLinesPerRangeLabel(3);

        // set a redraw rate of 30hz and start immediately:
        redrawer = new Redrawer(plot, 1000, true);

        plot.setTitle("Raw signal - Time Domain");

    }

    public static class MyFadeFormatter extends AdvancedLineAndPointRenderer.Formatter {

        private int trailSize;

        MyFadeFormatter(int trailSize) {
            this.trailSize = trailSize;
        }

        @Override
        public Paint getLinePaint(int thisIndex, int latestIndex, int seriesSize) {
            // offset from the latest index:
            int offset = 0;
            /*if(thisIndex > latestIndex) {
                offset = latestIndex + (seriesSize - thisIndex);
            } else {
                offset =  latestIndex - thisIndex;
            }*/

            float scale = 255f / trailSize;
            int alpha = (int) (255 - (offset * scale));
            getLinePaint().setAlpha(alpha > 0 ? alpha : 0);
            return getLinePaint();
        }
    }


    public static class EMGModel implements XYSeries {
        private final Number[] data;
        private final long delayMs;
        private final int blipInteral;
        private final Thread thread;
        private boolean keepRunning;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        /**
         * @param updateFreqHz Frequency at which new samples are added to the model
         */
        EMGModel(int updateFreqHz, Double voltage[]) {

            final int size = voltage.length;
            data = new Number[size];
            for (int i = 0; i< size; i++)
            {
                data[i] = voltage[i];
            }

            // translate hz into delay (ms):
            delayMs = 1000 / updateFreqHz;

            // add 7 "blips" into the signal:
            blipInteral = size / 7;

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (keepRunning) {
                            if (latestIndex >= size) {
                                keepRunning = false;
                            }
                            if(latestIndex < size - 1) {
                                // null out the point immediately following i, to disable
                                // connecting i and i+1 with a line:
                                data[latestIndex +1] = null;
                            }

                            if(rendererRef.get() != null) {
                                rendererRef.get().setLatestIndex(latestIndex);
                                Thread.sleep(delayMs);
                            } else {
                                keepRunning = false;
                            }
                            latestIndex++;
                        }
                    } catch (InterruptedException e) {
                        keepRunning = false;
                    }
                }
            });
        }



        @Override
        public int size() {
            return data.length;
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data[index];
        }

        @Override
        public String getTitle() {
            return "Signal";
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        redrawer.finish();
    }

}
