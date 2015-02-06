package errbuddy

import grails.util.Environment
import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.springframework.web.context.request.RequestContextHolder

class ErrbuddyService {

    static transactional = false

    def grailsApplication

    private static final String ERROR_PATH = '/api/error'
    private static final String DEPLOY_PATH = '/api/deployment'
    private String apiKey
    private ignoredParams
    private boolean enabled = false
    private AsyncHTTPBuilder httpBuilder
    String hostname = null
    private boolean addHostname = false
    private String hostnameSuffix = ''
    private boolean initiated = false

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

    void put(ErrbuddyPutObject putObject, boolean skipRequestData = false) {
        init()
        if (!enabled) {
            return
        }

        if (!skipRequestData) {
            fillWithRequestParams(putObject)
        }

        httpBuilder.request(Method.POST) { req ->
            uri.path = ERROR_PATH
            headers.key = apiKey
            body = putObject.postBody

            response.success = { resp ->
            }

            response.failure = { resp ->
                if (Environment.developmentMode) {
                    println("Sending message to errbuddy failed. $resp.statusLine")
                }
            }
        }
    }

    void postDeployment(String version = null, String hostname = null) {
        init()
        if (!version)
            version = grailsApplication.metadata['app.version'].toString()
        if (!hostname)
            hostname = this.hostname

        httpBuilder.request(Method.POST) { req ->
            uri.path = DEPLOY_PATH
            headers.key = apiKey
            body = [hostname: hostname, version: version]

            response.success = { resp ->
            }

            response.failure = { resp ->
                if (Environment.developmentMode) {
                    log.error("Could not send deployment notice to errbuddy: $resp.status")
                }
            }
        }
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
            if (!it.key in ignoredParams) {
                object.requestParameters << it
            }
        }
        object.hostname = hostname
    }

    protected static getWebRequest() {
        try {
            return RequestContextHolder.currentRequestAttributes()
        } catch (e) {
            // Do nothing as we know this can fail...
        }
    }

    void init() {
        if (initiated)
            return
        def conf = grailsApplication.config.grails.plugin.errbuddy
        enabled = conf.enabled as boolean
        if (!enabled) {
            return
        }

        apiKey = conf.apiKey
        httpBuilder = new AsyncHTTPBuilder(
                poolSize: conf.poolSize ?: 4,
                uri: conf.host ?: "https://errbuddy.net",
                contentType: ContentType.JSON
        )
        ignoredParams = grailsApplication.config.grails.plugin.errbuddy.params.exclude

        if (grailsApplication.config.grails.plugin.errbuddy.hostname.resolve)
            hostname = "${InetAddress.getLocalHost().getHostName()}$hostnameSuffix"
        else if (grailsApplication.config.grails.plugin.errbuddy.hostname.name)
            hostname = "${grailsApplication.config.grails.plugin.errbuddy.hostname.name}"
        initiated = true
    }

}
