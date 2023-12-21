package searchengine.services;

public interface IndexingService {
    boolean indexingAllSites();
    boolean indexingByUrl(String url);
    boolean stopIndexing();
}
