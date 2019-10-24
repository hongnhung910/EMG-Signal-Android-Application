package emgsignal.v3;

public class IIR_Filter {
//    private double[] B_coeff = {0.145869755195673, -0.925285841831314, 2.69694235352519, -4.79411043461389, 5.75316833546117, -4.79411043461389, 2.69694235352519,
//            -0.925285841831312, 0.145869755195673};
//    private double[] A_coeff = {-6.62815676962616, 19.2980903872498, -32.2699397581598, 33.9302017305931, -22.9970371843728, 9.82924121450740, -2.42992391554021, 0.267524295418928};
    private double[] B_coeff = { 0.903314273613301, - 8.59527050895991,   37.2310661038477, - 96.6384690896745,   166.416151717975, - 198.633577257335,   166.416151717975, - 96.6384690896744,   37.2310661038477, - 8.59527050895991,   0.903314273613301 };
    private double[] A_coeff = { -9.32179761798290,   39.5586499989896, - 100.600682185204,   169.738997575319, - 198.515883809871,   162.973129243891, - 92.7406019617151,   35.0143114724585, - 7.92209165964128,   0.815976680024282 };

    private double[] B_coeff10Hz = { 8.98486146372335e-07,   3.59394458548934e-06,    5.39091687823401e-06,    3.59394458548934e-06,    8.98486146372335e-07 };
    private double[] A_coeff10Hz = { -3.83582554064735,  5.52081913662223, - 3.53353521946301,   0.848555999266476 };

    public double[] update_input_filter_array50Hz(double[] array, double newNumber) {
        for(int i = 10; i >= 1; i--) {
            array[i] = array[i-1];
        }

        array[0] = newNumber;
        return array;
    }

    public double[] update_output_filter_array50Hz(double[] array, double newNumber) {
        for(int i = 9; i >= 1; i--) {
            array[i] = array[i-1];
        }

        array[0] = newNumber;
        return array;
    }

    public double filter50Hz(double[] input, double[] priorOutputs) {
        double output = 0;

        for(int i = 0; i < 11; i++) {
            output += B_coeff[i]*input[i];
        }

        for(int j = 0; j < 10; j++) {
            output -= A_coeff[j]*priorOutputs[j];
        }

        return output;
    }
    // for 100Hz filter
    public double filter10Hz(double[] input, double[] priorOutputs) {
        double output = 0;

        for(int i = 0; i < 5; i++) {
            output += B_coeff10Hz[i]*input[i];
        }

        for(int j = 0; j < 4; j++) {
            output -= A_coeff10Hz[j]*priorOutputs[j];
        }

        return output;
    }
    // for input update parameter
    public double[] update_input_filter_array10Hz(double[] array, double newNumber) {
        for(int i = 4; i >= 1; i--) {
            array[i] = array[i-1];
        }

        array[0] = newNumber;
        return array;
    }
    public double[] update_output_filter_array10Hz(double[] array, double newNumber) {
        for(int i = 3; i >= 1; i--) {
            array[i] = array[i-1];
        }

        array[0] = newNumber;
        return array;
    }

}
