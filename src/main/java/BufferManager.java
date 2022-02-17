import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class BufferManager {
    private final Buffer buffer;
    private int lastPlaceOfRequest;

    public BufferManager(Buffer buffer) {

        this.buffer = buffer;
    }

    public void addRequestToBuffer(Request request) {
        synchronized (buffer) {
            List<Request> requestList = buffer.buffer;
            int sizeRequestList = requestList.size();
            int i = lastPlaceOfRequest;
            while (sizeRequestList > i) {
                if (requestList.get(i) == null) {
                    buffer.push(i, request);
                    if (i + 1 != buffer.buffer.size()) {
                        lastPlaceOfRequest = i + 1;
                    } else {
                        lastPlaceOfRequest = 0;

                    }

                    return;
                }
                i++;
            }
            i = 0;
            while (lastPlaceOfRequest > i) {
                if (requestList.get(i) == null) {
                    buffer.push(i, request);
                    if (i + 1 != buffer.buffer.size()) {
                        lastPlaceOfRequest = i + 1;
                    } else {
                        lastPlaceOfRequest = 0;

                    }
                    return;
                }
                i++;
            }

            rightSort(requestList);
            buffer.push(buffer.buffer.indexOf(requestList.get(0)), request);
        }
    }

    public int getLastPlaceOfRequest() {
        return lastPlaceOfRequest;
    }

    private List<Request> rightSort(List<Request> buffer) {
        int sizeWithOutNulls = 0;
        for (Request request : buffer) {
            if (request == null) {
                continue;
            }
            sizeWithOutNulls++;
        }
        List<Request> tempVect = new ArrayList<>(Collections.nCopies(sizeWithOutNulls, null));
        int counter = 0;
        for (int i = 0; i < buffer.size(); i++) {
            if (buffer.get(i) != null) {
                tempVect.set(counter, buffer.get(i));
                counter++;
            }
        }

        bubbleSortArrivalTime(tempVect);
        return tempVect;
    }

    private void bubbleSortArrivalTime(List<Request> arr) {
        int n = arr.size();
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (arr.get(j).getArrivalTimeInSystem() < arr.get(j + 1).getArrivalTimeInSystem()) {
                    Request temp = arr.get(j);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, temp);
                }
    }
}
