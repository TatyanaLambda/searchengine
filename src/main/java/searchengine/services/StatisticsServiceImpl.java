package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SitePage;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Status;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    private TotalStatistics getTotal() {
        Long sites = siteRepository.count();
        Long pages = pageRepository.count();
        Long lemmas = lemmaRepository.count();
        return new TotalStatistics(sites, pages, lemmas, true);
    }

    private DetailedStatisticsItem getDetailed(SitePage sitePage) {
        String url = sitePage.getUrl();
        String name = sitePage.getName();
        Status status = sitePage.getStatus();
        LocalDateTime statusTime = sitePage.getStatusTime();
        String error = sitePage.getLastError();
        long pages = pageRepository.countBySiteId(sitePage);
        long lemmas = lemmaRepository.countBySiteId(sitePage);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

    private List<DetailedStatisticsItem> getDetailedList() {
        List<SitePage> sitePageList = siteRepository.findAll();
        List<DetailedStatisticsItem> result = new ArrayList<>();
        for (SitePage sitePage : sitePageList) {
            DetailedStatisticsItem item = getDetailed(sitePage);
            result.add(item);
        }
        return result;
    }


    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = getTotal();
        List<DetailedStatisticsItem> list = getDetailedList();
        return new StatisticsResponse(true, new StatisticsData(total, list));
    }
}