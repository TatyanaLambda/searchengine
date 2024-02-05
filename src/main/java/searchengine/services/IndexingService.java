package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.statistics.Response;

public interface IndexingService {
    ResponseEntity<?> indexingAllSites();
    ResponseEntity<?> indexingByUrl(String url);
    ResponseEntity<?> stopIndexing();
}
