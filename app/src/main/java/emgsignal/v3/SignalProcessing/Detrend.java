//Function to Eliminate DC level
package emgsignal.v3.SignalProcessing;

import android.util.Log;

import org.apache.commons.math3.stat.regression.SimpleRegression;


public class Detrend {

    public double[] detrend(double[] x) {

        double[][] newInput = new double[x.length][2];
        for (int i = 0; i < x.length; i++) {
            newInput[i][1] = x[i];
            newInput[i][0] = (double)i;
        }

        SimpleRegression regression = new SimpleRegression();
        regression.addData(newInput);
        double slope = regression.getSlope();
        Log.i("DETREND", "Check Detrend Function: slope = " + slope);
        double intercept = regression.getIntercept();
        Log.i("DETREND", "Check Detrend Function: intercept = " + intercept);
        double[][] output = new double[newInput.length][2];
        double[] y = new double[x.length];
        int j = 0;
        for (int i = 0; i < newInput.length; i++) {
            //double diffed = input[i][1] - slope*input[i][0] - intercept;
            output[i][1] = newInput[i][1] - slope*newInput[i][0] - intercept;
            y[j++] = output[i][1];
            output[i][0] = newInput[i][0];
            Log.i("DETREND", "Check Detrend Function: input = " + newInput[i][1] +  "output = " + y[j-1]);
        }
        return y;
    }
}
