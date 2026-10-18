package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "News")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nxvNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NewsID")
    private Integer newsId;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "Thumbnail")
    private String thumbnail; // Link ảnh bìa bài viết

    @Column(name = "PublishedAt")
    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        if (publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }
}