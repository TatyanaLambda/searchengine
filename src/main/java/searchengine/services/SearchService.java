package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.statistics.Response;
import searchengine.dto.statistics.SearchData;

import java.util.List;

public interface SearchService {
    ResponseEntity<?> search(String request, String site, int offset, int limit);


}
