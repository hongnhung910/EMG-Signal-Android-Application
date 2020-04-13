package emgsignal.v3.SignalProcessing;

import emgsignal.v3.SignalProcessing.Complex;

public class FFT {

    // compute the FFT of x[], assuming its length n is a power of 2
    public static Complex[] fft(Double[] x) {
        int n = x.length;

        // base case
        if (n == 1)
            return new Complex[] { new Complex(x[0], 0) };

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        // compute FFT of even terms
        Double[] even = new Double[n/2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] evenFFT = fft(even);

        // compute FFT of odd terms
        Double[] odd = new Double[n/2]; // reuse the array (to avoid n log n space)
        for (int k = 0; k < n / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] oddFFT = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = evenFFT[k].plus(wk.times(oddFFT[k]));
            y[k + n / 2] = evenFFT[k].minus(wk.times(oddFFT[k]));
        }
        return y;
    }
}
