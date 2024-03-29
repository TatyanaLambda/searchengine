package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;
import searchengine.model.SitePage;

@Repository
public interface SiteRepository extends JpaRepository<SitePage, Long> {
    SitePage findByUrl(String url);

}

