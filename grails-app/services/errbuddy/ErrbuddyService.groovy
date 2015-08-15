package errbuddy

import grails.converters.JSON
import grails.util.Environment
import org.apache.http.Header

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder

import java.nio.charset.Charset

class ErrbuddyService implements InitializingBean {

    static transactional = false

    def grailsApplication

    private static final String ERROR_PATH = '/api/error'
    private static final String DEPLOY_PATH = '/api/deployment'
    private static final String DEFAULT_ERRBUDDY_URL = "https://errbuddy.net"
    private static final String AUTH_KEY_HEADER_NAME = "key"
    private static final Header CONTENT_TYPE_HEADER = new BasicHeader('Content-Type', 'application/json')
    private static final Charset CHARSET = Charset.forName("UTF-8")
    private String apiKey
    private ignoredParams
    private boolean enabled = false
    private PoolingHttpClientConnectionManager connectionManager
    String hostname = null
    private boolean addHostname = false
    private String hostnameSuffix = ''

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

    def put(ErrbuddyPutObject putObject, boolean skipRequestData = false) {
        if (!enabled) {
            return false
        }

        if (!skipRequestData) {
            fillWithRequestParams(putObject)
        }

        def client = HttpClients.createDefault()
        try {
            def response = client.execute(buildPost(ERROR_PATH, putObject.postBody))
            if (response.statusLine.statusCode == 200) {
                return true
            } else {
                if (!Environment.warDeployed) {
                    String resp = EntityUtils.toString(response.entity)
                    println("Sending message to errbuddy failed. $response.statusLine.statusCode -> $resp")
                    return false
                }
            }
        } finally {
            client.close()
        }
        return false
    }

    void postDeployment(String version = null, String hostname = null) {
        if (!version)
            version = grailsApplication.metadata['app.version'].toString()
        if (!hostname)
            hostname = this.hostname

        def client = HttpClients.createDefault()
        try {
            def response = client.execute(buildPost(DEPLOY_PATH, [hostname: hostname, version: version]))
            if (response.statusLine.statusCode == 200) {
                true
            } else {
                if (Environment.developmentMode) {
                    println("Could not send deployment notice to errbuddy. $response.statusLine.statusCode")
                }
            }
        } finally {
            client.close()
        }
    }

    HttpPost buildPost(String target, Map body) {
        HttpPost post = new HttpPost("$errbuddyUrl$target")
        post.setEntity(new StringEntity((body as JSON).toString(), CHARSET))
        post.addHeader(AUTH_KEY_HEADER_NAME, apiKey)
        post.addHeader(CONTENT_TYPE_HEADER)
        post
    }

    void fillWithRequestParams(ErrbuddyPutObject object) {
        def request = webRequest
        if (!request) {
            return
        }

        object.controllerName = request.controllerName
        object.actionName = request.actionName
        object.path = "${request.request.requestURL}".replace(".dispatch", '')
        object.sessionParameters = [:]
        request.session.attributeNames.each {
            if (!(it in ignoredParams)) {
                object.sessionParameters.put(it, request.session.getAttribute(it))
            }
        }
        object.requestParameters = [:]
        request.parameterMap.each { it ->
            if (!(it.key in ignoredParams)) {
                object.requestParameters << it
            }
        }
        object.hostname = hostname
    }

    protected static getWebRequest() {
        try {
            return RequestContextHolder.currentRequestAttributes()
        } catch (Exception ignore) {
            // Do nothing as we know this can fail...
        }
    }

    private CloseableHttpClient getHttpClient() {
        HttpClients.custom().setConnectionManager(connectionManager).build()
    }

    private getErrbuddyUrl() {
        grailsApplication.config.grails.plugin.errbuddy.host ?: DEFAULT_ERRBUDDY_URL
    }

    void afterPropertiesSet() {

        def conf = grailsApplication.config.grails.plugin.errbuddy
        enabled = conf.enabled as boolean
        if (!enabled) {
            return
        }

        apiKey = conf.apiKey
        connectionManager = new PoolingHttpClientConnectionManager()
        ignoredParams = grailsApplication.config.grails.plugin.errbuddy.params.exclude
        hostnameSuffix = grailsApplication.config.grails.plugin.errbuddy.hostname.suffix
        if (grailsApplication.config.grails.plugin.errbuddy.hostname.resolve)
            hostname = "${InetAddress.getLocalHost().getHostName()}$hostnameSuffix"
        else if (grailsApplication.config.grails.plugin.errbuddy.hostname.name)
            hostname = "${grailsApplication.config.grails.plugin.errbuddy.hostname.name}"
    }
}
