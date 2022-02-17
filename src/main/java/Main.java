import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws InterruptedException, IOException {
        boolean stepByStepMode = false;
        final int COUNT_DEVICES = Constants.COUNT_DEVICES;
        final int COUNT_SOURCES = Constants.COUNT_SOURCES;
        final int SIMULATION_TIME = Constants.SIMULATION_TIME;
        Report report = new Report("RealSheet.xls", COUNT_SOURCES, COUNT_DEVICES);
        Object stepByStep = new Object();
        final Object bufferNotEmptyNotifier = new Object();
        Buffer buffer = new Buffer(bufferNotEmptyNotifier, report);
        BufferManager bufferManager = new BufferManager(buffer);
        Scanner input = new Scanner(System.in);


        List<Thread> sourcesList = new ArrayList<>(COUNT_SOURCES);
        for (int i = 0; i < COUNT_SOURCES; i++) {
            sourcesList.add(new Thread(new Generator(bufferManager, report, stepByStep)));
        }

        ArrayList<Devices> devicesList = new ArrayList<>(COUNT_DEVICES);
        List<Thread> threadsList = new ArrayList<>(COUNT_DEVICES);

        for (int i = 0; i < COUNT_DEVICES; i++) {
            Devices device = new Devices(report, stepByStep);
            devicesList.add(device);
            threadsList.add(new Thread(device));
        }

        DeviceManager deviceManager = new DeviceManager(buffer, devicesList, bufferNotEmptyNotifier, report, stepByStep, bufferManager);
        Thread deviceManagerThread = new Thread(deviceManager);

        report.setDeviceManager(deviceManager);
        try {
            sourcesList.forEach(Thread::start);
            Thread.sleep(1);
            threadsList.forEach(Thread::start);
            Thread.sleep(1);
            deviceManagerThread.start();

            if (stepByStepMode) {
                long timeUserInputTime = 0;
                long startTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - startTime < SIMULATION_TIME + timeUserInputTime) {
                    synchronized (stepByStep) {
                        stepByStep.notify();
                    }
                    long startTimerUserInput = System.currentTimeMillis();
                    Thread.sleep(10);
                    System.out.println(report.printConsoleStepByStepReport());
                    input.nextLine();
                    timeUserInputTime += System.currentTimeMillis() - startTimerUserInput;
                }
            } else {
                Thread thread = new Thread(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (stepByStep) {
                            stepByStep.notify();
                        }
                    }
                });
                thread.start();
                Thread.sleep(SIMULATION_TIME);
                thread.interrupt();
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sourcesList.forEach(Thread::interrupt);
        threadsList.forEach(Thread::interrupt);
        deviceManagerThread.interrupt();

        try {
            for (Thread sourceThread : sourcesList) {
                sourceThread.join();
            }
            for (Thread deviceThread : threadsList) {
                deviceThread.join();
            }
            deviceManagerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupt");
        }


        try {
            report.createReport();
            report.createTotalReport();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
