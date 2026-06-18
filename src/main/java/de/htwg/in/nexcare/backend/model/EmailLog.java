package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_log")
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String recipient;

    @Column(length = 300)
    private String subject;

    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    private LocalDateTime sentAt;

    private boolean success;

    @Column(length = 500)
    private String errorMessage;

    public EmailLog() {}

    public EmailLog(String recipient, String subject, EmailType emailType,
                    LocalDateTime sentAt, boolean success, String errorMessage) {
        this.recipient = recipient;
        this.subject = subject;
        this.emailType = emailType;
        this.sentAt = sentAt;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public EmailType getEmailType() { return emailType; }
    public LocalDateTime getSentAt() { return sentAt; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
