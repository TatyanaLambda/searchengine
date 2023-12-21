package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SitePage;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Long> {
    long countBySiteId(SitePage site);

    List<Lemma> findBySiteId(SitePage siteId);
    @Query(value = "SELECT l.* FROM lemmas l WHERE l.lemma IN :lemmas AND l.site_id = :sitePage", nativeQuery = true)
    List<Lemma> findLemmaListBySite(@Param("lemmas") List<String> lemmaList,
                                    @Param("sitePage") SitePage sitePage);
    @Query(value = "SELECT l.* FROM lemmas l WHERE l.lemma = :lemma ORDER BY frequency ASC", nativeQuery = true)
    List<Lemma> findByLemma(@Param("lemma") String lemma);

}
