package errbuddy

import grails.transaction.Transactional
import grails.util.Environment
import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder

@Transactional
class ErrbuddyService implements InitializingBean {

    def grailsApplication
    private String requestPath
    private String performanceBucket
    private String apiKey
    private def ignoredParams

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

    void put(ErrbuddyPutObject putObject, boolean skipRequestData = false)  {
        if (!skipRequestData)
            fillWithRequestParams(putObject)

        httpBuilder.request(Method.POST) { req ->
            uri.path = "$requestPath/$apiKey/${putObject.bucket}"
            body = putObject.postBody

            response.success = { resp ->
            }

            response.failure = { resp ->
                if (Environment.developmentMode)
                    println("Sending message to errbuddy failed. $resp.statusLine")

            }
        }
    }

    def fillWithRequestParams(ErrbuddyPutObject object) {
        def request = webRequest
        if (request) {
            object.controllerName = request.controllerName
            object.actionName = request.actionName
            object.path = request.contextPath
            object.sessionParameters = [:]
            request.session.attributeNames.toList().each {
                if(!(it in ignoredParams))
                    object.sessionParameters.put(it, request.session.getAttribute(it))
            }
            object.requestParameters = [:]
            request.parameterMap.each { it ->
                if(!it.key in ignoredParams)
                    object.requestParameters << it
            }
        }

    }

    protected def getWebRequest() {
        def request = null
        try {
            request = RequestContextHolder.currentRequestAttributes()
        } catch (Exception e) {
            // Do nothing as we know this can fail...
        }
        request
    }

    @Override
    void afterPropertiesSet() throws Exception {
        apiKey = grailsApplication.config.grails.plugin.errbuddy.apiKey
        requestPath = "${grailsApplication.config.grails.plugin.errbuddy.path}"
        httpBuilder = new AsyncHTTPBuilder(
                poolSize: grailsApplication.config.grails.plugin.errbuddy.poolSize ?: 4,
                uri: "$grailsApplication.config.grails.plugin.errbuddy.host",
                contentType: ContentType.JSON
        )
        performanceBucket = grailsApplication.config.grails.plugin.errbuddy.buckets.performance ?: 'performance'
        ignoredParams = grailsApplication.config.grails.plugin.errbuddy.params.exclude


    }

}
