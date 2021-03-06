package at.rtr.rmbt.utils;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@UtilityClass
public class ControllerUtils {

    public void addParametersFromMultipartRequest(MultiValueMap<String, String> parameters, HttpServletRequest request) {
        // 1. Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(10 * 1024 * 1024);

        // 2. Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items;
        try {
            items = upload.parseRequest(new ServletRequestContext(request));
            for (FileItem item : items) {
                if (item.isFormField() && item.getFieldName() != null && !Strings.isNullOrEmpty(item.getString("utf-8"))) {
                    parameters.add(item.getFieldName(), item.getString("utf-8"));
                } else if (!item.isFormField() && item.getFieldName() != null && item.getInputStream() != null && item.getSize() > 0) {
                    //it is really a file - parse it, add it as base64 input
                    String contentType = item.getContentType();
                    byte[] bytes = IOUtils.toByteArray(item.getInputStream());
                    String base64Str = Base64.encodeBase64String(bytes);
                    String dataUri = "data:" + contentType + ";base64," + base64Str;

                    if (item.getFieldName().endsWith("[]")) {
                        String fieldName = item.getFieldName().replaceAll("\\[\\]", "");
                        parameters.add(fieldName, dataUri);
                    } else {
                        parameters.add(item.getFieldName(), dataUri);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
