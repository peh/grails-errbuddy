import errbuddy.ErrbuddyLogAppender
import org.apache.log4j.Logger

class ErrbuddyGrailsPlugin {
    def version = "1.0.0"
    def grailsVersion = "2.4 > *"
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Grails err-buddy Plugin"
    def author = "Philipp Eschenbach"
    def authorEmail = "pesch3@gmail.com"
    def description = '''Err-buddy Grails client, and LogAppender.'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/peh/grails-errbuddy"

    def license = "APACHE"

    def issueManagement = [ system: "GitHub", url: "https://github.com/peh/grails-errbuddy/issues" ]

    def scm = [ url: "https://github.com/peh/grails-errbuddy" ]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithApplicationContext = { ctx ->
        if(application.config.grails.plugin.errbuddy.enabled)
            Logger.rootLogger.addAppender(new ErrbuddyLogAppender(service: ctx.errbuddyService, application: application, name: 'errbuddyLogAppender', threshold: org.apache.log4j.Level.WARN))
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
    }

    def onShutdown = { event ->
    }
}
