package com.example.demo.extras;/* Export this filter as a jar and place it under directory ".../tomcat/lib" on your Tomcat server/
 In the lib directory, also place the dependencies you need
 (ex. org.apache.commons.io => commons-io-2.8.0.jar)

 Once this is done, in order to activate the filter, on the Tomcat server:
 o in .../tomcat/conf/server.xml, add:
  <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs" prefix="localhost_access_log" suffix=".txt" pattern="%h %l %u %t &quot;%r&quot;  [%{postdata}r] %s %b"/>
  => the server will log the "postdata" attribute we generate in the Java code.
 o in .../tomcat/conf/web.xml, add:
  <filter>
  <filter-name>post-data-dumper-filter</filter-name>
  <filter-class>filters.PostDataDumperFilter</filter-class>
  </filter>
  <filter-mapping>
  <filter-name>post-data-dumper-filter</filter-name>
  <url-pattern>/*</url-pattern>
  </filter-mapping>

Once you've done this, restart your tomcat server. You will get extra infos in file "localhost_access_log.<date>.txt"

*/



import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.stream.Collectors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
    private ByteArrayOutputStream cachedBytes;

    public MultiReadHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null)
            cacheInputStream();

        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    private void cacheInputStream() throws IOException {
        /* Cache the inputstream in order to read it multiple times.
         */
        cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), cachedBytes);
    }

    /* An input stream which reads the cached request body */
    public class CachedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream input;

        public CachedServletInputStream() {
            /* create a new input stream from the cached request body */
            input = new ByteArrayInputStream(cachedBytes.toByteArray());
        }
        //---------------------
        @Override
        public int read() throws IOException {
            return input.read();
        }

        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }
        //---------------------
        @Override
        public void setReadListener(ReadListener arg0) {
            // TODO Auto-generated method stub
            // Ex. : throw new RuntimeException("Not implemented");
        }
    }

}