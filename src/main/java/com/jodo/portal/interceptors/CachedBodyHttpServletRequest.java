//package com.jodo.portal.interceptors;
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import jakarta.servlet.ReadListener;
//import jakarta.servlet.ServletInputStream;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletRequestWrapper;
//
//public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
//    private byte[] cachedBody;
//
//    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
//        super(request);
//        cachedBody = request.getInputStream().readAllBytes();
//    }
//
//    @Override
//    public ServletInputStream getInputStream() {
//        return new CachedBodyServletInputStream(cachedBody);
//    }
//
//    @Override
//    public BufferedReader getReader() {
//        return new BufferedReader(new InputStreamReader(getInputStream()));
//    }
//
//    public byte[] getCachedBody() {
//        return cachedBody;
//    }
//
//    private static class CachedBodyServletInputStream extends ServletInputStream {
//        private final ByteArrayInputStream inputStream;
//
//        public CachedBodyServletInputStream(byte[] cachedBody) {
//            this.inputStream = new ByteArrayInputStream(cachedBody);
//        }
//
//        @Override
//        public boolean isFinished() {
//            return inputStream.available() == 0;
//        }
//
//        @Override
//        public boolean isReady() {
//            return true;
//        }
//
//        @Override
//        public void setReadListener(ReadListener readListener) {
//            throw new UnsupportedOperationException();
//        }
//
//        @Override
//        public int read() throws IOException {
//            return inputStream.read();
//        }
//
//    }
//}
