package ru.pt.api.dto.email;

import java.util.ArrayList;
import java.util.List;

public class EmailMessage {
    private String to;
    private String subject;
    private String body;
    private boolean html;
    private List<EmailAttachment> attachments = new ArrayList<>();

    public EmailMessage() {
    }

    public EmailMessage(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public EmailMessage(String to, String subject, String body, boolean html, List<EmailAttachment> attachments) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.html = html;
        if (attachments != null) {
            this.attachments = attachments;
        }
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments == null ? new ArrayList<>() : attachments;
    }
}
