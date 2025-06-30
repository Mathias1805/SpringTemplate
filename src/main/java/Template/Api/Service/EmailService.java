package Template.Api.Service;

public interface EmailService {
   public void sendSimpleEmailAsync(String to, String subject, String text);
}
