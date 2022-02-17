import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Generator implements Runnable {
    private static final AtomicInteger number = new AtomicInteger();
    private int numberSource = 0;
    private static int counter = 0;
    private final BufferManager bufferManager;
    private Report report;
    private final Object stepByStep;
    public Generator(BufferManager bufferManager, Report report, Object stepByStep) {
        this.bufferManager = bufferManager;
        this.report = report;
        this.numberSource = counter++;
        this.stepByStep = stepByStep;
    }

    public Request generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Request request = new Request();
        request.setIdRequest(number.incrementAndGet());
        request.setSourceNumber(this.numberSource);
        request.setArrivalTimeInSystem(System.currentTimeMillis());
        return request;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            synchronized (stepByStep) {
                try {
                    stepByStep.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }

            Request request = generate();
            report.increaseGenerateSourceRequestCount(numberSource);
            System.out.println("Generator:" + numberSource + " number request" + request.getIdRequest());
            bufferManager.addRequestToBuffer(request);
            try {
                Thread.sleep((long) (Math.exp(Math.random()) * Constants.MILLISECONDS_PER_SECOND));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
