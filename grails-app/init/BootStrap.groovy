import grails.plugins.errbuddy.ErrbuddyLogAppender
import grails.plugins.errbuddy.ErrbuddyService
import grails.core.GrailsApplication

class BootStrap {

    ErrbuddyService errbuddyService
    GrailsApplication grailsApplication

    def init = { servletContext ->
        ErrbuddyLogAppender.instance.enable()
        errbuddyService.postDeployment("foo", "bar")
    }
    def destroy = {
    }
}
