public class SourceReport {
    private final int sourceNumber;
    private int requestCount;
    private int processedRequestCount;
    private int canceledRequestCount;
    private long timeRequestInBuffer;
    private long timeOfWorkWithRequest;

    public SourceReport(int sourceNumber) {
        this.sourceNumber = sourceNumber;
        requestCount = 0;
        processedRequestCount = 0;
        canceledRequestCount = 0;
        timeRequestInBuffer = 0;
    }

    public synchronized void increaseRequestCount() {
        ++requestCount;
    }

    public synchronized void increaseProcessedRequestCount() {
        ++processedRequestCount;
    }

    public synchronized void increaseCanceledRequestCount() {
        ++canceledRequestCount;
    }

    public synchronized void increaseTimeRequestInBuffer(long time) {
        this.timeRequestInBuffer += time;
    }

    public synchronized void increaseTimeOfWorkWithRequest(long time) {
        this.timeOfWorkWithRequest += time;
    }

    public synchronized int getSourceNumber() {
        return sourceNumber;
    }

    public synchronized int getRequestCount() {
        return requestCount;
    }

    public synchronized int getProcessedRequestCount() {
        return processedRequestCount;
    }

    public synchronized int getCanceledRequestCount() {
        return canceledRequestCount;
    }

    public synchronized long getTimeRequestInBuffer() {
        return timeRequestInBuffer;
    }

    public synchronized long getTimeOfWorkWithRequest() {
        return timeOfWorkWithRequest;
    }

}
