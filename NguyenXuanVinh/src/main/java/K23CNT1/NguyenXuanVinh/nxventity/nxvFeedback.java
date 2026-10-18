package K23CNT1.NguyenXuanVinh.nxventity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Feedbacks")
public class nxvFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FeedbackID")
    private Integer feedbackId;

    @ManyToOne
    @JoinColumn(name = "UserID")
    private nxvUser user; // Có thể null

    @Column(name = "CustomerName")
    private String customerName;

    @Column(name = "RepliedAt")
    private LocalDateTime repliedAt;

    @Column(name = "Email")
    private String email;

    @Column(name = "Subject")
    private String subject;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "Status")
    private String status; // PENDING, RESOLVED

    @Column(name = "SentAt")
    private LocalDateTime sentAt;

    @Column(name = "ReplyContent")
    private String replyContent;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}