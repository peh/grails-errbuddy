package grails.plugins.errbuddy

import com.google.gson.GsonBuilder
import grails.core.GrailsApplication
import grails.util.Environment
import groovy.transform.CompileStatic
import org.apache.http.Header
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.LaxRedirectStrategy
import org.apache.http.message.BasicHeader
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletRequest
import java.nio.charset.Charset

@CompileStatic
class ErrbuddyService implements InitializingBean {

    static transactional = false

    GrailsApplication grailsApplication

    public static final String REQUEST_IDENTIFIER_KEY = "errbuddyIdentifier"

    private static final String ERROR_PATH = '/api/error'
    private static final String DEPLOY_PATH = '/api/deployment'
    private static final String AUTH_KEY_HEADER_NAME = "key"
    private static final Header CONTENT_TYPE_HEADER = new BasicHeader('Content-Type', 'application/json')
    private static final int MAX_WAIT_TIME = 10000
    private static final Charset CHARSET = Charset.forName("UTF-8")
    private static final GsonBuilder JSON_BUILDER = new GsonBuilder()

    private static HttpClientBuilder clientBuilder
    private List<String> ignoredParams
    private boolean enabled = false
    String hostname = null

    void measured(Closure c) {
        long start = System.currentTimeMillis()
        c.call()
        long end = System.currentTimeMillis()
        put(new ErrbuddyPerfomanceObject(
                type: ErrbuddyPutObject.Type.PERFORMANCE,
                start: start,
                end: end
        ))
    }

    void error(String message) {
        put(new ErrbuddyLogObject(level: 'ERROR', message: message))
    }

    void log(String message) {
        put(new ErrbuddyLogObject(level: 'LOG', message: message))
    }

    void put(ErrbuddyPutObject putObject) {
        if (enabled) {
            fillWithRequestParams(putObject)
            new SendThread(buildPost(ERROR_PATH, putObject.postBody), httpClient).start()
        }
    }

    void postDeployment(String version = null, String hostname = null) {
        if (!enabled) {
            return
        }

        if (!version)
            version = grailsApplication.metadata.getApplicationVersion()

        new SendThread(buildPost(DEPLOY_PATH, [hostname: hostname ?: this.hostname, version: version]), httpClient).start()
    }

    HttpPost buildPost(String target, Map body) {
        HttpPost post = new HttpPost("$errbuddyUrl$target")
        post.setEntity(new StringEntity(JSON_BUILDER.create().toJson(body), CHARSET))
        post
    }

    void fillWithRequestParams(ErrbuddyPutObject object) {
        GrailsWebRequest request = webRequest
        if (!request) {
            return
        }

        object.controllerName = request.controllerName
        object.actionName = request.actionName
        object.path = "${request.request.requestURL}".replace(".dispatch", '')

        object.sessionParameters = [:]

        request.session.attributeNames.each { String k ->
            if (!ignoredParams.contains(k)) {
                object.sessionParameters.put(k, request.session.getAttribute(k).toString())
            }
        }

        object.requestParameters = [:]

        request.parameterMap.each { it ->
            String k = it.key
            if (!ignoredParams.contains(k)) {
                object.requestParameters.put(k, it.value.toString())
            }
        }
        object.hostname = hostname

        // push the errbuddyIdentifier in the request so users can get it in there custom error controller
        request.request.setAttribute(REQUEST_IDENTIFIER_KEY, object.identifier)
    }

    String getErrbuddyIdentifier(HttpServletRequest request) {
        return request.getAttribute(REQUEST_IDENTIFIER_KEY)
    }

    String getShortLink(HttpServletRequest request) {
        "$errbuddyUrl/s/${getErrbuddyIdentifier(request)}"
    }

    protected static GrailsWebRequest getWebRequest() {
        try {
            return WebUtils.retrieveGrailsWebRequest()
        } catch (ignore) {
            // Do nothing as we know this can fail...
        }
    }

    private getErrbuddyUrl() {
        grailsApplication.config.getProperty("grails.plugin.errbuddy.host")
    }

    private CloseableHttpClient getHttpClient() {
        clientBuilder.build()
    }

    void afterPropertiesSet() {
        if (!grailsApplication.config.getProperty("grails.plugin.errbuddy.enabled", Boolean)) {
            return
        }

        enabled = true

        ignoredParams = grailsApplication.config.getProperty("grails.plugin.errbuddy.params.exclude", List, [])

        if (grailsApplication.config.getProperty("grails.plugin.errbuddy.hostname.resolve", Boolean)) {
            String suffix = grailsApplication.config.getProperty("grails.plugin.errbuddy.hostname.suffix")
            hostname = "${InetAddress.getLocalHost().getHostName()}$suffix"
        } else if (grailsApplication.config.getProperty("grails.plugin.errbuddy.hostname.name")) {
            hostname = "${grailsApplication.config.getProperty("grails.plugin.errbuddy.hostname.name")}"
        }

        String apiKey = grailsApplication.config.getProperty("grails.plugin.errbuddy.apiKey")
        RequestConfig.Builder requestBuilder = RequestConfig.custom().setConnectTimeout(MAX_WAIT_TIME).setConnectionRequestTimeout(MAX_WAIT_TIME)
        clientBuilder = HttpClients
                .custom()
                .setDefaultHeaders([CONTENT_TYPE_HEADER, new BasicHeader(AUTH_KEY_HEADER_NAME, apiKey)])
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setDefaultRequestConfig(requestBuilder.build())
    }

    private class SendThread extends Thread {

        private HttpPost httpPost
        private CloseableHttpClient client

        SendThread(HttpPost httpPost, CloseableHttpClient client) {
            this.httpPost = httpPost
            this.client = client
        }

        @Override
        void run() {
            try {
                def response = client.execute(httpPost)
                if (response.statusLine.statusCode != HttpStatus.OK.value()) {
                    if (Environment.developmentMode) {
                        println("Sending Post failed. $response.statusLine.statusCode -> $response.statusLine.reasonPhrase")
                    }
                }
            } catch (Throwable t) {
                if (Environment.developmentMode) {
                    println("Sending Post failed. $t.message")
                }
            } finally {
                client.close()
            }
        }
    }
}
