package net.openhft.chronicle.wire.bytesmarshallable;

public class BenchStringMain {
    public static void main(String[] args) {
        PerfRegressionHolder main = new PerfRegressionHolder();
        main.doTest(main::benchString);
    }
}
