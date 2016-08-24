import errbuddy.ErrbuddyLogAppender
import errbuddy.ErrbuddyService
import grails.core.GrailsApplication
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.beans.factory.support.GenericBeanDefinition

class BootStrap {

    ErrbuddyService errbuddyService
    GrailsApplication grailsApplication

    def init = { servletContext ->
        def errbuddyLogAppender = new GenericBeanDefinition(beanClass: ErrbuddyLogAppender, autowireMode: AbstractBeanDefinition.AUTOWIRE_BY_NAME)

        grailsApplication.mainContext.registerBeanDefinition('errbuddyLogAppender', errbuddyLogAppender)
        def logAppender = grailsApplication.mainContext.getBean('errbuddyLogAppender')

        logAppender.enable()
        log.error("wattt????")
        errbuddyService.postDeployment("foo", "bar")
    }
    def destroy = {
    }
}
