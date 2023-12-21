package searchengine.model;

import lombok.*;

import javax.persistence.*;
import javax.persistence.Index;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pages", indexes = {@Index(name="path_index", columnList = "path"),
        @Index(name = "id_index", columnList = "id", unique=true)})
public class Page implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(columnDefinition = "VARCHAR(515)", nullable = false)
    private String path;
    @Column(nullable = false)
    private int code;
    @ToString.Exclude
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false, referencedColumnName = "id")
    private SitePage siteId;
    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<IndexModel> index = new ArrayList<>();
    public Page(SitePage siteId, String path, int code, String content) {
        this.siteId = siteId;
        this.path = path;
        this.code = code;
        this.content = content;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page that = (Page) o;
        return id == that.id && code == that.code &&
                siteId.equals(that.siteId) &&
                path.equals(that.path) &&
                content.equals(that.content) &&
                index.equals(that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, path, code, content, index);
    }

    @Override
    public String toString() {
        return "PageEntity{" +
                "id=" + id +
                ", siteId=" + siteId +
                ", path='" + path + '\'' +
                ", code=" + code +
                ", content='" + content + '\'' +
                ", index=" + index +
                '}';
    }
}
