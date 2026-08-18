package vn.codegym.house_rental.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // [CẢI TIẾN]: Bổ sung hằng số định dạng đuôi file cho phép và giới hạn 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu trữ tập tin upload", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // [CẢI TIẾN]: Kiểm tra kích thước file không quá 5MB
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
        String fileExtension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            fileExtension = originalFilename.substring(i).toLowerCase();
        }

        // [CẢI TIẾN]: Kiểm tra định dạng đuôi file hình ảnh hợp lệ
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Định dạng file không hợp lệ! Chỉ chấp nhận JPG, JPEG, PNG, WEBP.");
        }

        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi lưu trữ tập tin " + originalFilename, ex);
        }
    }
}
