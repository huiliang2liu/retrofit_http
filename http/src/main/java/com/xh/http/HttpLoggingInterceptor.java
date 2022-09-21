package com.xh.http;

import static okhttp3.internal.platform.Platform.INFO;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

class HttpLoggingInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private List<String> filters = new ArrayList<>();


    public interface Logger {
        void log(String message);

        /**
         * A {@link Logger} defaults output appropriate for the current platform.
         */
        Logger DEFAULT = message -> Platform.get().log(message, INFO, null);
    }

    HttpLoggingInterceptor() {
        this(Logger.DEFAULT);
    }

    HttpLoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    public HttpLoggingInterceptor setFilters(List<String> filters) {
        this.filters = filters;
        return this;
    }

    private final Logger logger;

    private volatile Set<String> headersToRedact = Collections.emptySet();

    public void redactHeader(String name) {
        Set<String> newHeadersToRedact = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newHeadersToRedact.addAll(headersToRedact);
        newHeadersToRedact.add(name);
        headersToRedact = newHeadersToRedact;
    }


    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        StringBuilder sb = new StringBuilder().append(request.url()).append('\n');
        sb.append(request.method()).append('\n');
        if (hasRequestBody) {
            // Request body headers are only present when installed as a network interceptor. Force
            // them to be included (when available) so there values are known.
            if (requestBody.contentType() != null) {
                sb.append("Content-Type: ").append(requestBody.contentType()).append('\n');
            }
            if (requestBody.contentLength() != -1) {
                sb.append("Content-Length: ").append(requestBody.contentLength()).append('\n');
            }
        }

        Headers headers = request.headers();
        for (int i = 0, count = headers.size(); i < count; i++) {
            String name = headers.name(i);
            // Skip headers from the request body as they are explicitly logged above.
            if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                String value = headersToRedact.contains(headers.name(i)) ? "██" : headers.value(i);
                sb.append(headers.name(i)).append(": ").append(value).append('\n');
            }
        }
        if (hasRequestBody && !bodyHasUnknownEncoding(request.headers()) && !requestBody.isDuplex() && !filter(request.url().toString())) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            isPlaintext(buffer);
            sb.append('\n');
            sb.append(buffer.readString(charset)).append('\n');
        }
        sb.append("================================================\n");
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            sb.append(e);
            logger.log(sb.toString());
            throw e;
        }
        headers = response.headers();
        for (int i = 0, count = headers.size(); i < count; i++) {
            String value = headersToRedact.contains(headers.name(i)) ? "██" : headers.value(i);
            sb.append(headers.name(i)).append(": ").append(value).append('\n');
        }
        if (!HttpHeaders.hasBody(response) || bodyHasUnknownEncoding(response.headers()) || filter(request.url().toString())) {
            logger.log(sb.toString());
            return response;
        }
        sb.append('\n');
        ResponseBody responseBody = response.body();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.getBuffer();

        Long gzippedLength = null;
        if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
            gzippedLength = buffer.size();
            try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            }
        }
        isPlaintext(buffer);
        sb.append(buffer.clone().readString(charset));
        logger.log(sb.toString());
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
    }

    public boolean filter(String url) {
        if (filters == null || filters.isEmpty())
            return false;
        for (String filter : filters)
            if (url.startsWith(filter))
                return true;
        return false;
    }
}
