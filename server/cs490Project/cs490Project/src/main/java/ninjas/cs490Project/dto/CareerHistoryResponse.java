package ninjas.cs490Project.dto;

import ninjas.cs490Project.entity.CareerHistory;
import java.util.List;

public class CareerHistoryResponse {
    private String historyId;
    private String status;
    private List<CareerHistory> histories;

    public CareerHistoryResponse(String historyId, String status) {
        this.historyId = historyId;
        this.status = status;
    }

    public CareerHistoryResponse(String historyId, String status, List<CareerHistory> histories) {
        this.historyId = historyId;
        this.status = status;
        this.histories = histories;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CareerHistory> getHistories() {
        return histories;
    }

    public void setHistories(List<CareerHistory> histories) {
        this.histories = histories;
    }
}
