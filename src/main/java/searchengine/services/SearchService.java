package searchengine.services;

import searchengine.dto.statistics.SearchData;

import java.util.List;

public interface SearchService {
    List<SearchData> siteSearch(String query, String site, int offset, int limit);
    List<SearchData> allSiteSearch(String query, int offset, int limit);
}
