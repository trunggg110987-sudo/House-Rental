package vn.codegym.house_rental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendHostApprovalEmail(String toEmail, String fullName, String reason) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            helper.setSubject("Thông báo duyệt đăng ký chủ nhà - HouseRental");

            String content = "<h2>Xin chào " + fullName + ",</h2>" +

                    "<p>HouseRental xin thông báo:</p>" +

                    "<p><strong>Yêu cầu đăng ký trở thành chủ nhà của bạn đã được duyệt.</strong></p>" +

                    "<p><strong>Lý do:</strong></p>" +

                    "<p>" + reason + "</p>" +

                    "<p>Bạn hiện đã có thể sử dụng các chức năng dành cho chủ nhà.</p>" +

                    "<br>" +

                    "<p>Trân trọng,</p>" + "<p><strong>HouseRental Team</strong></p>";

            helper.setText(content, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException("Không thể gửi email thông báo duyệt chủ nhà.", e);
        }
    }

    public void sendHostRejectionEmail(String toEmail, String fullName, String reason) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            helper.setSubject("Thông báo từ chối đăng ký chủ nhà - HouseRental");

            String content = "<h2>Xin chào " + fullName + ",</h2>" +

                    "<p>HouseRental xin thông báo:</p>" +

                    "<p><strong>Yêu cầu đăng ký trở thành chủ nhà của bạn chưa được chấp nhận.</strong></p>" +

                    "<p><strong>Lý do:</strong></p>" +

                    "<p>" + reason + "</p>" +

                    "<p>Bạn có thể kiểm tra thông tin và thực hiện đăng ký lại theo quy định.</p>" +

                    "<br>" +

                    "<p>Trân trọng,</p>" + "<p><strong>HouseRental Team</strong></p>";

            helper.setText(content, true);

            mailSender.send(message);

        } catch (MessagingException e) {

            throw new RuntimeException("Không thể gửi email thông báo từ chối chủ nhà.", e);
        }
    }
}