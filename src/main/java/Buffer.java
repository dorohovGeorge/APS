import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class Buffer {
    private int MAX_BUFFER_SIZE = Constants.MAX_BUFFER_SIZE;
    private Report report;
    final Vector<Request> buffer;
    private volatile int counter;
    private final Object bufferNotEmptyNotifier;

    public Buffer(Object bufferNotEmptyNotifier, Report report) {
        this.bufferNotEmptyNotifier = bufferNotEmptyNotifier;
        buffer = new Vector<>(Collections.nCopies(MAX_BUFFER_SIZE, null));
        this.report = report;
    }

    public Request get(int i) {
        Request request = buffer.get(i);
        buffer.set(i, null);
        counter--;
        return request;
    }

    public void push(int i, Request request) {
        Request oldRequest = buffer.get(i);


        if (oldRequest == null) {
            buffer.set(i, request);
            ++counter;
            synchronized (bufferNotEmptyNotifier) {
                bufferNotEmptyNotifier.notify();
            }
        } else {
            Vector<Request> tempVect = new Vector<Request>(Collections.nCopies(buffer.size(), null));
            Collections.copy(tempVect, buffer);
            tempVect.sort(Comparator.comparing(Request::getArrivalTimeInSystem).reversed());
            Request lastReceived = tempVect.get(0);
            int indexLastReceived = buffer.indexOf(lastReceived);
            buffer.set(indexLastReceived, request);
            report.increaseTimeRequestInBuffer(oldRequest.getSourceNumber(), System.currentTimeMillis() - oldRequest.getArrivalTimeInSystem());
            System.out.println("Request " + oldRequest.getIdRequest() + " canceled");
            report.increaseCanceledSourceRequestCount(oldRequest.getSourceNumber());
        }
    }

    public synchronized boolean isEmpty() {
        return counter == 0;
    }


}
