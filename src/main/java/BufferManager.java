import java.util.List;

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
            buffer.push(lastPlaceOfRequest, request);
        }
    }

    public int getLastPlaceOfRequest() {
        return lastPlaceOfRequest;
    }
}
