package at.rtr.rmbt.utils;

import com.google.common.base.Strings;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@UtilityClass
public class ControllerUtils {

    public void addParametersFromMultipartRequest(MultiValueMap<String, String> parameters, HttpServletRequest request) {
        // 1. Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(10 * 1024 * 1024);

        // 2. Create a new file upload handler
        List<FileItem> items;
        try {
            for (Part part : request.getParts()) {
                if (part.getSubmittedFileName() == null && !Strings.isNullOrEmpty(part.getName()) && !parameters.containsKey(part.getName())) {
                    //if not already added
                    parameters.add(part.getName(), new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
                } else if (part.getSubmittedFileName() != null && part.getInputStream() != null && part.getSize() > 0) {
                    String contentType = part.getContentType();
                    byte[] bytes = IOUtils.toByteArray(part.getInputStream());
                    String base64Str = Base64.encodeBase64String(bytes);
                    String dataUri = "data:" + contentType + ";base64," + base64Str;

                    String fieldName = part.getName().replaceAll("\\[\\]", "");
                    parameters.add(fieldName, dataUri);
                }
            }
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
    }

}
