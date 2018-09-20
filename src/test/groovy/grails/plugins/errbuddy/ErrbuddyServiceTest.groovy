package grails.plugins.errbuddy

import com.stehno.ersatz.Decoders
import com.stehno.ersatz.ErsatzServer
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.transform.ToString
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

import java.time.LocalDateTime

@TestFor(ErrbuddyService)
@Stepwise
@TestMixin(ControllerUnitTestMixin)
class ErrbuddyServiceTest extends Specification {

    void setup() {
        grailsApplication.config.grails.plugin.errbuddy.enabled = "true"
        grailsApplication.config.grails.plugin.errbuddy.apiKey = "123456"
        grailsApplication.config.grails.plugin.errbuddy.params.exclude = ["password"]
        grailsApplication.config.grails.plugin.errbuddy.hostname.suffix = "hostname_suffix"
        grailsApplication.config.grails.plugin.errbuddy.hostname.resolve = false
        grailsApplication.config.grails.plugin.errbuddy.hostname.name = "errbuddy.test"

        service.afterPropertiesSet()
    }

    void "The service properties are properly set after bean initialization"() {
        expect:
        service.enabled == true
        service.ignoredParams == ["password"]
        service.hostname == "errbuddy.test"
        service.clientBuilder != null
        service.clientBuilder.defaultHeaders.any { it.name == "key" && it.value == "123456" }
        service.clientBuilder.defaultHeaders.contains(service.CONTENT_TYPE_HEADER)
    }

    void "when hostname resolve is set to true, the hostname is properly set"() {
        given:
        grailsApplication.config.grails.plugin.errbuddy.hostname.resolve = true

        and:
        ErrbuddyService errbuddyService = new ErrbuddyService()
        errbuddyService.grailsApplication = grailsApplication

        when:
        errbuddyService.afterPropertiesSet()

        then:
        errbuddyService.hostname == "${InetAddress.getLocalHost().getHostName()}hostname_suffix"

        cleanup:
        grailsApplication.config.grails.plugin.errbuddy.hostname.resolve = false
    }

    void "Basic information is being send to errbuddy for an error event"() {
        given:

        ErsatzServer ersatz = ersatzServer

        def requestContent = [
                message: "this is a test",
                type   : "LOG",
                level  : "ERROR"
        ]

        ersatz.expectations {
            post(service.ERROR_PATH) {
                called 1
                header('key', '123456')
                body(createJsonMatcher(requestContent), 'application/json')
                responder {
                    content([success: true], 'application/json')
                }
            }
        }
        grailsApplication.config.grails.plugin.errbuddy.host = ersatz.httpUrl

        when:
        service.put(new ErrbuddyLogObject(type: ErrbuddyPutObject.Type.LOG, message: "this is a test", level: "ERROR"))

        then:
        new PollingConditions().eventually {
            ersatz.verify()
        }

        cleanup:
        ersatz.stop()
    }

    void "request attributes are automagically added to the error event that is send to errbuddy (as strings!)"() {
        given:

        ComplexTestClass test = new ComplexTestClass("foo", "bar", LocalDateTime.now())
        ErsatzServer ersatz = ersatzServer

        def requestContent = [
                message          : "this is a test",
                type             : "LOG",
                level            : "ERROR",
                requestParameters: [foo: "bar"],
                sessionParameters: [ctc: test.toString()]
        ]

        ersatz.expectations {
            post(service.ERROR_PATH) {
                called 1
                header('key', '123456')
                body(createJsonMatcher(requestContent), 'application/json')
                responder {
                    content([success: true], 'application/json')
                }
            }
        }
        grailsApplication.config.grails.plugin.errbuddy.host = ersatz.httpUrl

        and:
        request.setParameter("foo", "bar")
        request.session.setAttribute("ctc", test)

        when:
        service.put(new ErrbuddyLogObject(type: ErrbuddyPutObject.Type.LOG, message: "this is a test", level: "ERROR"))

        then:
        new PollingConditions().eventually {
            ersatz.verify()
        }

        cleanup:
        ersatz.stop()
    }

    private static ErsatzServer getErsatzServer() {
        new ErsatzServer({ decoder('application/json', Decoders.parseJson) })
    }

    private static Matcher createJsonMatcher(Map match) {
        new BaseMatcher() {
            @Override
            boolean matches(Object item) {
                !match.any { item[it.key] != it.value }
            }

            @Override
            void describeTo(Description description) {

            }
        }
    }

    @ToString
    private class ComplexTestClass {
        String foo
        String bar
        LocalDateTime ldt

        ComplexTestClass(foo, bar, ldt) {
            this.foo = foo
            this.bar = bar
            this.ldt = ldt
        }
    }

}
