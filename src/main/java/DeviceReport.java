public class DeviceReport {
    private final int deviceNumber;
    private long busytime;
    private long downtime;

    public DeviceReport(int deviceNumber) {
        this.deviceNumber = deviceNumber;
        busytime = 0;
        downtime = 0;
    }

    public void increaseBusytime(long processingSpeed) {
        this.busytime += processingSpeed;
    }

    public void increaseDowntime(long downtime){
        this.downtime = downtime;
    }

    public synchronized int getDeviceNumber() {
        return deviceNumber;
    }

    public synchronized long getBusytime() {
        return busytime;
    }

    public synchronized long getDowntime() {
        return downtime;
    }

    public synchronized double getUseFactor() {
        return busytime == 0 && downtime == 0
                ? 0
                : (double) busytime/(busytime + downtime);
    }
}
