package de.giovio.touchcounter;

public class DataPoint {
    private long time;

    DataPoint() {
        this.time = System.nanoTime();
    }

    long getTime() {
        return time;
    }
}
