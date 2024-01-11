/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.logging;//NOPMD

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import com.mongodb.BasicDBObject;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.application.RequestLog;
import com.publicissapient.kpidashboard.common.repository.application.RequestLogRepository;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Logging filter.
 */
@Slf4j
public class LoggingFilter implements Filter {

	@Autowired
	private RequestLogRepository requestLogRepository;
	@Autowired
	private CustomApiConfig settings;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// implement
		// in future
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		if (isRequestContainsHttpMethods(httpServletRequest)) {
			Map<String, String> requestMap = getTypesafeRequestMap(httpServletRequest);
			BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpServletRequest);
			BufferedResponseWrapper bufferedResponse = new BufferedResponseWrapper(httpServletResponse);

			RequestLog requestLog = new RequestLog();
			requestLog.setClient(httpServletRequest.getRemoteAddr());
			requestLog.setEndpoint(httpServletRequest.getRequestURI());
			requestLog.setMethod(httpServletRequest.getMethod());
			requestLog.setParameter(requestMap.toString());
			requestLog.setRequestSize(httpServletRequest.getContentLengthLong());
			requestLog.setRequestContentType(httpServletRequest.getContentType());

			chain.doFilter(bufferedRequest, bufferedResponse);
			requestLog.setResponseContentType(httpServletResponse.getContentType());
			try {
				if (StringUtils.isNotBlank(httpServletRequest.getContentType())
						&& (new MimeType(httpServletRequest.getContentType())
								.match(new MimeType(APPLICATION_JSON_VALUE)))
						&& StringUtils.isNotBlank(bufferedRequest.getRequestBody())) {
					requestLog.setRequestBody(BasicDBObject.parse(bufferedRequest.getRequestBody()));

				}
				if (StringUtils.isNotBlank(bufferedResponse.getContentType())
						&& (new MimeType(bufferedResponse.getContentType()).match(new MimeType(APPLICATION_JSON_VALUE)))
						&& StringUtils.isNotBlank(bufferedResponse.getContent())) {
					requestLog.setResponseBody(BasicDBObject.parse(bufferedResponse.getContent()));
				}
			} catch (MimeTypeParseException e) {
				log.error("Invalid MIME Type detected. Request MIME type={}, Response MIME Type={}",
						httpServletRequest.getContentType(), bufferedResponse.getContentType());
			}
			requestLog.setResponseSize(bufferedResponse.getContent().length());

			requestLog.setResponseCode(bufferedResponse.getStatus());
			requestLog.setTimestamp(System.currentTimeMillis());

			requestLogRepository.save(requestLog);

		} else {
			if (settings.isCorsEnabled()) {

				String clientOrigin = httpServletRequest.getHeader("Origin");

				String corsWhitelist = settings.getCorsWhitelist();
				if (!StringUtils.isEmpty(corsWhitelist)) {
					List<String> incomingURLs = Arrays.asList(corsWhitelist.trim().split(","));

					addHeader(httpServletResponse, clientOrigin, incomingURLs);
				}

			}
			chain.doFilter(request, response);
		}
	}

	/**
	 * Checks for http methods in request
	 *
	 * @param httpServletRequest
	 * @return boolean
	 */
	private boolean isRequestContainsHttpMethods(HttpServletRequest httpServletRequest) {
		return httpServletRequest.getMethod().equals(HttpMethod.PUT.toString())
				|| (httpServletRequest.getMethod().equals(HttpMethod.POST.toString()))
				|| (httpServletRequest.getMethod().equals(HttpMethod.DELETE.toString()));
	}

	/**
	 * Adds headers for response
	 *
	 * @param httpServletResponse
	 * @param clientOrigin
	 * @param incomingURLs
	 */
	private void addHeader(HttpServletResponse httpServletResponse, String clientOrigin, List<String> incomingURLs) {
		if (incomingURLs.contains(clientOrigin)) {
			// adds headers to response to allow CORS
			httpServletResponse.addHeader("Access-Control-Allow-Origin", clientOrigin);
			httpServletResponse.addHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST, PUT, DELETE");
			httpServletResponse.addHeader("Access-Control-Allow-Headers", "Content-Type");
			httpServletResponse.addHeader("Access-Control-Max-Age", "1");
		}
	}

	/**
	 * Gets request param
	 *
	 * @param request
	 * @return typesafeRequestMap
	 */
	private Map<String, String> getTypesafeRequestMap(HttpServletRequest request) {
		Map<String, String> typesafeRequestMap = new HashMap<>();
		Enumeration<?> requestParamNames = request.getParameterNames();
		if (null != requestParamNames) {
			while (requestParamNames.hasMoreElements()) {
				String requestParamName = (String) requestParamNames.nextElement();
				String requestParamValue = request.getParameter(requestParamName);
				typesafeRequestMap.put(requestParamName, requestParamValue);
			}
		}
		return typesafeRequestMap;
	}

	@Override
	public void destroy() {
		// do something
		// before destroy
	}

	private static final class BufferedRequestWrapper extends HttpServletRequestWrapper {

		private final ByteArrayOutputStream baos;
		private final byte[] buffer;
		private BufferedServletInputStream bsis;

		/**
		 * Instantiates a new Buffered request wrapper.
		 *
		 * @param req
		 *            the req
		 * @throws IOException
		 *             the io exception
		 */
		public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
			super(req);
			// Read InputStream and store its content in a buffer.

			this.baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int letti;
			try (InputStream is = req.getInputStream()) {
				while ((letti = is.read(buf)) > 0) {
					this.baos.write(buf, 0, letti);
				}
			}
			this.buffer = this.baos.toByteArray();
			bsis = null;
		}

		@Override
		public ServletInputStream getInputStream() {
			this.bsis = new BufferedServletInputStream(new ByteArrayInputStream(this.buffer));
			return this.bsis;
		}

		/**
		 * Gets request body.
		 *
		 * @return the request body
		 * @throws IOException
		 *             the io exception
		 */
		/* package */ String getRequestBody() throws IOException {
			String line;
			StringBuilder inputBuffer = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()))) {
				do {
					line = reader.readLine();
					if (null != line) {
						inputBuffer.append(line);
					}
				} while (line != null);
			}
			return inputBuffer.toString();
		}

	}

	private static final class BufferedServletInputStream extends ServletInputStream {

		private final ByteArrayInputStream bais;

		/**
		 * Instantiates a new Buffered servlet input stream.
		 *
		 * @param bais
		 *            the bais
		 */
		public BufferedServletInputStream(ByteArrayInputStream bais) {
			super();
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
			return false;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			// shoiuld be implemented
			// in future

		}
	}

	/**
	 * The type Tee servlet output stream.
	 */
	public class TeeServletOutputStream extends ServletOutputStream {

		private final TeeOutputStream targetStream;

		/**
		 * Instantiates a new Tee servlet output stream.
		 *
		 * @param one
		 *            the one
		 * @param two
		 *            the two
		 */
		public TeeServletOutputStream(OutputStream one, OutputStream two) {
			super();
			targetStream = new TeeOutputStream(one, two);
		}

		@Override
		public void write(int arg0) throws IOException {
			this.targetStream.write(arg0);
		}

		@Override
		public void flush() throws IOException {
			super.flush();
			this.targetStream.flush();
		}

		@Override
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
			// implementation
			// pending

		}
	}

	/**
	 * The type Buffered response wrapper.
	 */
	public class BufferedResponseWrapper implements HttpServletResponse {

		private final HttpServletResponse original;
		private TeeServletOutputStream teeStream;
		private ByteArrayOutputStream bos;
		private PrintWriter teeWriter;

		/**
		 * Instantiates a new Buffered response wrapper.
		 *
		 * @param response
		 *            the response
		 */
		public BufferedResponseWrapper(HttpServletResponse response) {
			original = response;
		}

		/**
		 * Gets content.
		 *
		 * @return the content
		 */
		public String getContent() {

			return (bos == null) ? "" : bos.toString();
		}

		@Override
		public PrintWriter getWriter() throws IOException {

			if (this.teeWriter == null) {
				this.teeWriter = new PrintWriter(new OutputStreamWriter(getOutputStream()));
			}
			return this.teeWriter;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {

			if (LoggingFilter.BufferedResponseWrapper.this.teeStream == null) {
				bos = new ByteArrayOutputStream();
				LoggingFilter.BufferedResponseWrapper.this.teeStream = new TeeServletOutputStream(
						original.getOutputStream(), bos);
			}
			return LoggingFilter.BufferedResponseWrapper.this.teeStream;
		}

		@Override
		public String getCharacterEncoding() {
			return original.getCharacterEncoding();
		}

		@Override
		public void setCharacterEncoding(String charset) {
			original.setCharacterEncoding(charset);
		}

		@Override
		public String getContentType() {
			return original.getContentType();
		}

		@Override
		public void setContentType(String type) {
			original.setContentType(type);
		}

		@Override
		public void setContentLength(int len) {
			original.setContentLength(len);
		}

		@Override
		public void setContentLengthLong(long value) {
			// implementation
			// pending
		}

		@Override
		public int getBufferSize() {
			return original.getBufferSize();
		}

		@Override
		public void setBufferSize(int size) {
			original.setBufferSize(size);
		}

		@Override
		public void flushBuffer() throws IOException {
			if (teeStream != null) {
				teeStream.flush();
			}
			if (this.teeWriter != null) {
				this.teeWriter.flush();
			}
		}

		@Override
		public void resetBuffer() {
			original.resetBuffer();
		}

		@Override
		public boolean isCommitted() {
			return original.isCommitted();
		}

		@Override
		public void reset() {
			original.reset();
		}

		@Override
		public Locale getLocale() {
			return original.getLocale();
		}

		@Override
		public void setLocale(Locale loc) {
			original.setLocale(loc);
		}

		@Override
		public void addCookie(Cookie cookie) {
			original.addCookie(cookie);
		}

		@Override
		public boolean containsHeader(String name) {
			return original.containsHeader(name);
		}

		@Override
		public String encodeURL(String url) {
			return original.encodeURL(url);
		}

		@Override
		public String encodeRedirectURL(String url) {
			return original.encodeRedirectURL(url);
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			original.sendError(sc, msg);
		}

		@Override
		public void sendError(int sc) throws IOException {
			original.sendError(sc);
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			original.sendRedirect(location);
		}

		@Override
		public void setDateHeader(String name, long date) {
			original.setDateHeader(name, date);
		}

		@Override
		public void addDateHeader(String name, long date) {
			original.addDateHeader(name, date);
		}

		@Override
		public void setHeader(String name, String value) {
			original.setHeader(name, value);
		}

		@Override
		public void addHeader(String name, String value) {
			original.addHeader(name, value);
		}

		@Override
		public void setIntHeader(String name, int value) {
			original.setIntHeader(name, value);
		}

		@Override
		public void addIntHeader(String name, int value) {
			original.addIntHeader(name, value);
		}

		@Override
		public int getStatus() {
			return original.getStatus();
		}

		@Override
		public void setStatus(int sc) {
			original.setStatus(sc);
		}

		@Override
		public String getHeader(String value) {
			return null;
		}

		@Override
		public Collection<String> getHeaders(String value) {
			return Collections.emptyList();
		}

		@Override
		public Collection<String> getHeaderNames() {
			return Collections.emptyList();
		}

	}

}
