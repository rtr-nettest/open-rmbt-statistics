package at.rtr.rmbt.filter;

import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ApiLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLoggingFilter.class);
    private final String requestIdParamName;

    public ApiLoggingFilter(String requestIdParamName) {
        this.requestIdParamName = requestIdParamName;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;

            // Swagger UI / OpenAPI spec and other static infrastructure are not RMBT API calls and
            // must not be wrapped: the BufferedResponseWrapper below would buffer and log the full
            // (~130 KB) spec on every Swagger load, and is the same fragile wrapper that returns an
            // empty body for springdoc on the control server. Pass these through untouched.
            if (isInfrastructurePath(httpServletRequest.getServletPath())) {
                chain.doFilter(request, response);
                return;
            }

            Map<String, String> requestMap = this.getTypesafeRequestMap(httpServletRequest);
            BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpServletRequest);
            BufferedResponseWrapper bufferedResponse = new BufferedResponseWrapper(httpServletResponse);
            String requestId = requestMap.containsKey(requestIdParamName) ? requestMap.get(requestIdParamName)
                    : UUID.randomUUID().toString();
            MDC.put("REQUEST_ID", requestId);
            final StringBuilder logRequest = new StringBuilder("HTTP ").append(httpServletRequest.getMethod())
                    .append(" \"").append(httpServletRequest.getServletPath()).append("\" ").append(", parameters=")
                    .append(requestMap).append(", body=").append(bufferedRequest.getRequestBody())
                    .append(", headers={").append(Collections.list(((HttpServletRequest) request)
                                    .getHeaderNames()).stream()
                            .map(r -> String.format("\"%s\": \"%s\"", r, ((HttpServletRequest) request).getHeader(r)))
                            .collect(Collectors.joining(", ")) + "}");
            LOGGER.info(logRequest.toString());
            try {
                chain.doFilter(bufferedRequest, bufferedResponse);
            } catch (Throwable a) {
                if (isClientAbort(a)) {
                    LOGGER.info("User disconnected before the response was completed: {} {}",
                            httpServletRequest.getMethod(), httpServletRequest.getServletPath());
                } else {
                    LOGGER.error(a.getMessage(), a);
                }
            } finally {
                // Only log textual response bodies; binary payloads (e.g. application/pdf from the
                // export endpoints) would otherwise dump raw bytes into the log.
                final String contentType = httpServletResponse.getContentType();
                if (isTextualContentType(contentType)) {
                    final String content = bufferedResponse.getContent();
                    LOGGER.info("HTTP RESPONSE " + content.substring(0, Math.min(content.length(), 1000)));
                } else {
                    LOGGER.info("HTTP RESPONSE [" + contentType + " body not logged]");
                }
                MDC.clear();
            }
        } catch (Throwable a) {
            LOGGER.error(a.getMessage(), a);
        }
    }

    /** True if the exception (or any cause) is a client-side disconnect (broken pipe / reset). */
    private static boolean isClientAbort(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if ("ClientAbortException".equals(c.getClass().getSimpleName())) {
                return true;
            }
            final String m = c.getMessage();
            if (c instanceof java.io.IOException && m != null
                    && (m.contains("Broken pipe") || m.contains("Connection reset"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Paths that must bypass request/response buffering: the springdoc OpenAPI spec and Swagger UI,
     * plus webjars and the actuator health probe. Matched against the context-relative servlet path
     * (e.g. {@code /api-docs} or {@code /v3/api-docs}).
     */
    private static boolean isInfrastructurePath(String servletPath) {
        if (servletPath == null) {
            return false;
        }
        return servletPath.startsWith("/v3/api-docs")
                || servletPath.startsWith("/api-docs")
                || servletPath.startsWith("/swagger-ui")
                || servletPath.startsWith("/swagger-resources")
                || servletPath.startsWith("/webjars")
                || servletPath.equals("/health");
    }

    private static boolean isTextualContentType(String contentType) {
        if (contentType == null) {
            return true;
        }
        final String ct = contentType.toLowerCase();
        return ct.startsWith("text/") || ct.contains("json") || ct.contains("xml");
    }

    private Map<String, String> getTypesafeRequestMap(HttpServletRequest request) {
        Map<String, String> typesafeRequestMap = new HashMap<String, String>();
        Enumeration<?> requestParamNames = request.getParameterNames();
        while (requestParamNames.hasMoreElements()) {
            String requestParamName = (String) requestParamNames.nextElement();
            String requestParamValue;
            if (requestParamName.equalsIgnoreCase("password")) {
                requestParamValue = "********";
            } else {
                requestParamValue = request.getParameter(requestParamName);
            }
            typesafeRequestMap.put(requestParamName, requestParamValue);
        }
        return typesafeRequestMap;
    }

    private static final class BufferedRequestWrapper extends HttpServletRequestWrapper {
        private ByteArrayInputStream bais = null;
        private ByteArrayOutputStream baos = null;
        private BufferedServletInputStream bsis = null;
        private byte[] buffer = null;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);
            // Read InputStream and store its content in a buffer.
            InputStream is = req.getInputStream();
            this.baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int read;
            while ((read = is.read(buf)) > 0) {
                this.baos.write(buf, 0, read);
            }
            this.buffer = this.baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            this.bais = new ByteArrayInputStream(this.buffer);
            this.bsis = new BufferedServletInputStream(this.bais);
            return this.bsis;
        }

        String getRequestBody() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
            String line = null;
            StringBuilder inputBuffer = new StringBuilder();
            do {
                line = reader.readLine();
                if (null != line) {
                    inputBuffer.append(line.trim());
                }
            } while (line != null);
            reader.close();
            return inputBuffer.toString().trim();
        }
    }

    private static final class BufferedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        @Override
        public int available() {
            return this.bais.available();
        }

        @Override
        public int read() {
            return this.bais.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return this.bais.read(buf, off, len);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }

    public class TeeServletOutputStream extends ServletOutputStream {
        private final TeeOutputStream targetStream;

        public TeeServletOutputStream(OutputStream one, OutputStream two) {
            targetStream = new TeeOutputStream(one, two);
        }

        @Override
        public void write(int arg0) throws IOException {
            this.targetStream.write(arg0);
        }

        public void flush() throws IOException {
            super.flush();
            this.targetStream.flush();
        }

        public void close() throws IOException {
            super.close();
            this.targetStream.close();
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }

    public class BufferedResponseWrapper extends HttpServletResponseWrapper {
        TeeServletOutputStream tee;
        ByteArrayOutputStream bos;

        public BufferedResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        public String getContent() throws IOException {
            if (Objects.isNull(bos)) {
                this.getOutputStream();
            }
            return bos.toString();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (tee == null) {
                bos = new ByteArrayOutputStream();
                tee = new TeeServletOutputStream(getResponse().getOutputStream(), bos);
            }
            return tee;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (tee != null) {
                tee.flush();
            } else {
                super.flushBuffer();
            }
        }
    }
}
