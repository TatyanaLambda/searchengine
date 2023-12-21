package searchengine.dto.statistics;

import lombok.Value;
import searchengine.model.Status;

import java.time.LocalDateTime;
import java.util.Date;

@Value
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private Status status;
    private LocalDateTime statusTime;
    private String error;
    private long pages;
    private long lemmas;
}
