package errbuddy

import grails.util.Environment
import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method

import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder

class ErrbuddyService implements InitializingBean {

    static transactional = false

    def grailsApplication

    private String requestPath
    private String performanceBucket
    private String apiKey
    private ignoredParams
    private boolean enabled = false
    private AsyncHTTPBuilder httpBuilder

    void measured(String bucketKey = performanceBucket, Closure c) {
        long start = System.currentTimeMillis()
        c.call()
        long end = System.currentTimeMillis()
        put(new ErrbuddyPerfomanceObject(
                bucket: bucketKey,
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
        if (!enabled) {
            return
        }

        if (!skipRequestData) {
            fillWithRequestParams(putObject)
        }

        httpBuilder.request(Method.POST) { req ->
            uri.path = "$requestPath/$apiKey/${putObject.bucket}"
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
    }

    protected static getWebRequest() {
        try {
            return RequestContextHolder.currentRequestAttributes()
        } catch (e) {
            // Do nothing as we know this can fail...
        }
    }

    void afterPropertiesSet() {

        def conf = grailsApplication.config.grails.plugin.errbuddy
        enabled = conf.enabled as boolean
        if (!enabled) {
            return
        }

        apiKey = conf.apiKey
        requestPath = conf.path ?: "/api/put"
        httpBuilder = new AsyncHTTPBuilder(
                poolSize: conf.poolSize ?: 4,
                uri: conf.host ?: "http://errbuddy.net",
                contentType: ContentType.JSON
        )
        performanceBucket = conf.buckets.performance ?: 'performance'
        ignoredParams = conf.params.exclude
    }
}
