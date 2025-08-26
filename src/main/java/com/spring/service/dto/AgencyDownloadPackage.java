package com.spring.service.dto;

import java.io.InputStream;

import org.springframework.http.MediaType;

public class AgencyDownloadPackage { // 파일이 단건
    private final InputStream inputStream;
    private final String singleOriginalName;
    private final MediaType mediaType;
    private final long contentLength;
    private final boolean single;

    public AgencyDownloadPackage(InputStream inputStream,
                                 String singleOriginalName,
                                 MediaType mediaType,
                                 long contentLength,
                                 boolean single) {
        this.inputStream = inputStream;
        this.singleOriginalName = singleOriginalName;
        this.mediaType = mediaType;
        this.contentLength = contentLength;
        this.single = single;
    }

    public InputStream getInputStream() { return inputStream; }
    public String getSingleOriginalName() { return singleOriginalName; }
    public MediaType getMediaType() { return mediaType; }
    public long getContentLength() { return contentLength; }
    public boolean isSingle() { return single; }

    public boolean isEmpty() { return inputStream == null; }
}
