package emgsignal.v3;

public class Helper {
    public static boolean check(int n) {
        if(n <= 0)
            return false;
        while (n > 1){
            if(n%2 !=0)
                return false;
            n = n/2;
        }
        return true;
    }

    public static int sampleNumbers(int n) {
        double pow = Math.log(n) / Math.log(2);
        return (int) Math.pow(2, Math.ceil(pow));
    }

    public static Double[] appendZeros(Double[] a) {
        int n = a.length;
        if (check(n)) {
            return a;
        } else {
            int m = sampleNumbers(n);
            Double[] b = new Double[m];
            for (int i = 0; i < n; i++) {
                b[i] = a[i];
            }
            for (int j = n; j < m; j++) {
                b[j] = 0.0;
            }
            return b;
        }
    }

}