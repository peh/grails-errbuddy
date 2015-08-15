package errbuddy

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.apache.commons.lang.RandomStringUtils
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(ErrbuddyService)
@TestMixin(ControllerUnitTestMixin)
class ErrbuddyServiceSpec extends Specification {

    def setup() {
        service.afterPropertiesSet()
    }

    def "a simple put should work"() {
        given:
        def putObject = new ErrbuddyErrorObject(
                type: ErrbuddyPutObject.Type.ERROR,
                message: "this is a test",
                level: "error",
                identifier: RandomStringUtils.randomAlphanumeric(32)
        )
        when:
        def result = service.put(putObject, true)
        then:
        result == true
    }

    def "if disabled we do not do any request"(){
        given:
        service.enabled = false
        def putObject = new ErrbuddyErrorObject(
                type: ErrbuddyPutObject.Type.ERROR,
                message: "this is a test",
                level: "error",
                identifier: RandomStringUtils.randomAlphanumeric(32)
        )
        when:
        def result = service.put(putObject, true)
        then:
        result == false
    }
}
