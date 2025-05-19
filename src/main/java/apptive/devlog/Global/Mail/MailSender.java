package apptive.devlog.Global.Mail;

public interface MailSender {
    void send(String to, String content) throws MailSendException;
} 