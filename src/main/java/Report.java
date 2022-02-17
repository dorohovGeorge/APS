import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class Report {
    private String fileName;
    private int bufferSize = Constants.MAX_BUFFER_SIZE;
    Workbook reports;
    DeviceManager deviceManager;

    List<SourceReport> sourceReportList;
    List<DeviceReport> deviceReportList;

    private int totalRequests;
    private long totalRequestTimeInSystem;
    private long totalDeviceDownTime;
    private long totalDeviceBusyTime;
    private double chanceOfRejection;

    private double workloadOfSystem;
    private double averageTimeOfRequestInSystem;

    public Report(String reportFileName, int sourceReportSize, int deviceReportSize) {
        this.fileName = reportFileName;
        this.reports = new HSSFWorkbook();
        sourceReportList = new Vector<>(sourceReportSize);
        deviceReportList = new Vector<>(deviceReportSize);

        for (int i = 0; i < sourceReportSize; i++) {
            sourceReportList.add(new SourceReport(i));
        }

        for (int i = 0; i < deviceReportSize; i++) {
            deviceReportList.add(new DeviceReport(i));
        }
    }


    public synchronized String printConsoleStepByStepReport() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%10s", "Source"))
                .append(String.format("  %24s", "Generated request count"))
                .append(String.format("  %24s", "Rejected request count"))
                .append(String.format(" %2s", "\n"));

        for (SourceReport sourceReport : sourceReportList) {

            stringBuilder.append(String.format("%10s", "Source " + sourceReport.getSourceNumber()))
                    .append(String.format("  %24s", sourceReport.getRequestCount()))
                    .append(String.format("  %24s", sourceReport.getCanceledRequestCount()))
                    .append(String.format(" %2s", "\n"));
        }


        if (deviceManager != null) {
            stringBuilder.append(String.format("\n%10s", "Device"))
                    .append(String.format("  %7s", "Status"))
                    .append(String.format(" %2s", "\n"))
                    .append(String.format(" %2s", "|\n"));

            int i = 0;
            for (Boolean status : deviceManager.getDeviceStatuses()) {
                stringBuilder
                        .append(String.format("%10s", (deviceManager.getIndexDevicesPlace() == i ? "*" : "") + "Device " + i++))
                        .append(String.format("  %7s", status ? "Busy" : "Free"))
                        .append(String.format(" %2s", "|\n"));
            }
            stringBuilder.append("\nBuffer\n")
                    .append(deviceManager.printBuffer());
        }
        return stringBuilder.toString();
    }


    public synchronized void createReport() throws IOException {
        Sheet sheet = reports.createSheet("RealSheet");
        int rowCounter = 0;
        Row row = sheet.createRow(rowCounter++);
        List<Cell> cellList = new ArrayList<Cell>();
        for (int i = 0; i < 9; i++) {
            cellList.add(row.createCell(i));
        }
        cellList.get(0).setCellValue("Source");
        cellList.get(1).setCellValue("Generated request");
        cellList.get(2).setCellValue("Canceled request");
        cellList.get(3).setCellValue("Probability canceled");
        cellList.get(4).setCellValue("Time of request in buffer");
        cellList.get(5).setCellValue("Time of work with request");
        cellList.get(6).setCellValue("All time of request in system");
        cellList.get(7).setCellValue("Dispersion time in buffer");
        cellList.get(8).setCellValue("Dispersion time of work with request");

        for (SourceReport sourceReport : sourceReportList) {
            row = sheet.createRow(rowCounter++);
            cellList.clear();
            for (int i = 0; i < 7; i++) {
                cellList.add(row.createCell(i));
            }
            cellList.get(0).setCellValue(sourceReport.getSourceNumber());
            cellList.get(1).setCellValue(sourceReport.getRequestCount());
            cellList.get(2).setCellValue(sourceReport.getCanceledRequestCount());
            cellList.get(3).setCellValue(sourceReport.getProcessedRequestCount() + sourceReport.getCanceledRequestCount() == 0
                    ? 0 : (double) sourceReport.getCanceledRequestCount() / (sourceReport.getProcessedRequestCount() + sourceReport.getCanceledRequestCount()));
            cellList.get(4).setCellValue(sourceReport.getTimeRequestInBuffer());
            cellList.get(5).setCellValue(sourceReport.getTimeOfWorkWithRequest());
            cellList.get(6).setCellValue(sourceReport.getTimeOfWorkWithRequest() + sourceReport.getTimeRequestInBuffer());


        }
        row = sheet.createRow(rowCounter++);
        cellList.clear();
        for (int i = 0; i < 9; i++) {
            cellList.add(row.createCell(i));
        }

        double averageTimeInBuffer = 0;
        double averageTimeOfWorkWithRequest = 0;
        for (SourceReport sourceReport : sourceReportList) {
            averageTimeInBuffer = sourceReport.getTimeRequestInBuffer();
            averageTimeOfWorkWithRequest = sourceReport.getTimeOfWorkWithRequest();
        }
        averageTimeInBuffer /= sourceReportList.size();
        averageTimeOfWorkWithRequest /= sourceReportList.size();

        double sumTimeInBuffer = 0;
        double sumTimeOfWorkWithRequest = 0;
        for (SourceReport sourceReport : sourceReportList) {
            sumTimeInBuffer = Math.pow((sourceReport.getTimeRequestInBuffer() - averageTimeInBuffer), 2);
            sumTimeOfWorkWithRequest = Math.pow(sourceReport.getTimeOfWorkWithRequest() - averageTimeOfWorkWithRequest, 2);
        }
        cellList.get(0).setCellValue("Total");
        cellList.get(7).setCellValue(sourceReportList.size() <= 1
                ? 0
                : sumTimeInBuffer / (sourceReportList.size() - 1));
        cellList.get(8).setCellValue(sourceReportList.size() <= 1
                ? 0
                : sumTimeOfWorkWithRequest / (sourceReportList.size() - 1));
        row = sheet.createRow(rowCounter++);
        cellList.clear();
        for (int i = 0; i < 2; i++) {
            cellList.add(row.createCell(i));
        }
        cellList.get(0).setCellValue("Device");
        cellList.get(1).setCellValue("Use factor");

        for (DeviceReport report : deviceReportList) {
            row = sheet.createRow(rowCounter++);
            cellList.clear();
            for (int i = 0; i < 2; i++) {
                cellList.add(row.createCell(i));
            }
            cellList.get(0).setCellValue("Device " + report.getDeviceNumber());
            cellList.get(1).setCellValue(report.getUseFactor());
        }

        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }

        reports.write(new FileOutputStream(fileName));
    }

    public synchronized void createTotalReport() throws IOException {
        summarize();
        Sheet sheet = reports.createSheet("TotalSheet");
        int rowCounter = 0;
        Row row = sheet.createRow(rowCounter++);
        List<Cell> cellList = new ArrayList<Cell>();
        for (int i = 0; i < 8; i++) {
            cellList.add(row.createCell(i));
        }
        cellList.get(0).setCellValue("Source");
        cellList.get(1).setCellValue("Devices");
        cellList.get(2).setCellValue("Buffer size");
        cellList.get(3).setCellValue("Total count of request");
        cellList.get(4).setCellValue("Chance of rejected");
        cellList.get(5).setCellValue("Average time of request in system");
        cellList.get(6).setCellValue("Time of work system");
        cellList.get(7).setCellValue("Workload of system");


        row = sheet.createRow(rowCounter++);
        cellList.clear();
        for (int i = 0; i < 8; i++) {
            cellList.add(row.createCell(i));
        }

        cellList.get(0).setCellValue(sourceReportList.size());
        cellList.get(1).setCellValue(deviceReportList.size());
        cellList.get(2).setCellValue(bufferSize);
        cellList.get(3).setCellValue(totalRequests);
        cellList.get(4).setCellValue(chanceOfRejection);
        cellList.get(5).setCellValue(averageTimeOfRequestInSystem);
        cellList.get(6).setCellValue(totalDeviceBusyTime + totalDeviceDownTime);
        cellList.get(7).setCellValue(workloadOfSystem);

        reports.write(new FileOutputStream(fileName));
    }

    public void increaseCanceledSourceRequestCount(int sourceNumber) {
        sourceReportList.get(sourceNumber).increaseCanceledRequestCount();
    }

    public void increaseProcessedSourceRequestCount(int sourceNumber) {
        sourceReportList.get(sourceNumber).increaseProcessedRequestCount();
    }

    public void increaseTimeOfWorkWithRequest(int sourceNumber, long time) {
        sourceReportList.get(sourceNumber).increaseTimeOfWorkWithRequest(time);
    }

    public void increaseTimeRequestInBuffer(int sourceNumber, long time) {
        sourceReportList.get(sourceNumber).increaseTimeRequestInBuffer(time);
    }

    public void increaseGenerateSourceRequestCount(int sourceNumber) {
        sourceReportList.get(sourceNumber).increaseRequestCount();
    }

    public void increaseBusytimeInDevice(int deviceNumber, long time) {
        deviceReportList.get(deviceNumber).increaseBusytime(time);
    }

    public void increaseDowntimeInDevice(int deviceNumber, long time) {
        deviceReportList.get(deviceNumber).increaseDowntime(time);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    public void setDeviceManager(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    private synchronized void summarize() {
        AtomicInteger processedSourceRequestCount = new AtomicInteger();
        AtomicInteger canceledRequestCount = new AtomicInteger();

        sourceReportList.forEach(sourceReport -> {
            totalRequests += sourceReport.getRequestCount();
            processedSourceRequestCount.addAndGet(sourceReport.getProcessedRequestCount());
            canceledRequestCount.addAndGet(sourceReport.getCanceledRequestCount());
            totalRequestTimeInSystem = totalRequestTimeInSystem + sourceReport.getTimeOfWorkWithRequest() + sourceReport.getTimeRequestInBuffer();
        });

        chanceOfRejection = (double) canceledRequestCount.get() / (canceledRequestCount.get() + processedSourceRequestCount.get());

        deviceReportList.forEach(deviceReport -> {
            totalDeviceBusyTime += deviceReport.getBusytime();
            totalDeviceDownTime += deviceReport.getDowntime();
        });

        workloadOfSystem = (double) totalDeviceBusyTime / (totalDeviceDownTime + totalDeviceBusyTime);
        averageTimeOfRequestInSystem = (double) totalRequestTimeInSystem / totalRequests;
    }
}

