package apptive.devlog.Global.Mail;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MailQueueService {
    private final Queue<MailTask> mailQueue = new ConcurrentLinkedQueue<>();
    private final Queue<MailTask> retryQueue = new ConcurrentLinkedQueue<>();
    private final MailSender mailSender;

    public MailQueueService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enqueueMail(String to, String content) {
        mailQueue.add(new MailTask(to, content, 0));
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void processQueue() {
        process(mailQueue);
        process(retryQueue);
    }

    private void process(Queue<MailTask> queue) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            MailTask task = queue.poll();
            if (task == null) continue;
            try {
                mailSender.send(task.to, task.content);
            } catch (MailSendException e) {
                if (task.retryCount < 2) {
                    queue.add(new MailTask(task.to, task.content, task.retryCount + 1));
                }
            }
        }
    }

    private static class MailTask {
        final String to;
        final String content;
        final int retryCount;

        MailTask(String to, String content, int retryCount) {
            this.to = to;
            this.content = content;
            this.retryCount = retryCount;
        }
    }
} 