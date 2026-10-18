package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NxvJournals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NxvJournals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JournalID")
    private Long journalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private Users user;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "MoodTag", length = 50)
    private String moodTag;

    @Column(name = "AIResponse", length = 1000)
    private String aiResponse;

    @Builder.Default
    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}