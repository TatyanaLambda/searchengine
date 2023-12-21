package searchengine.dto.statistics;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchData {
    private String site;
    private String siteName;
    private String url;
    private String title;
    private String snippet;
    private Float relevance;
    public SearchData(String site, String siteName, String url,
                            String title, String snippet, Float relevance) {
        this.site = site;
        this.siteName = siteName;
        this.url = url;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
