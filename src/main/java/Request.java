public class Request {
    private int idRequest;
    private Integer sourceNumber;

    private Long arrivalTimeInSystem;

    public int getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(int idRequest) {
        this.idRequest = idRequest;
    }

    public Integer getSourceNumber() {
        return sourceNumber;
    }

    public void setSourceNumber(Integer sourceNumber) {
        this.sourceNumber = sourceNumber;
    }

    public void setArrivalTimeInSystem(Long time) {
        this.arrivalTimeInSystem = time;
    }

    public Long getArrivalTimeInSystem() {
        return this.arrivalTimeInSystem;
    }

    @Override
    public String toString() {
        return "Request{" +
                "idRequest=" + idRequest +

                ", arrivalTimeInSystem='" + arrivalTimeInSystem + '\'' +
                '}';
    }
}
