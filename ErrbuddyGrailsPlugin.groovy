import org.apache.log4j.Level
import org.apache.log4j.Logger

import errbuddy.ErrbuddyLogAppender

class ErrbuddyGrailsPlugin {
    def version = "1.2.3"
    def grailsVersion = "2.0 > *"
    def title = "Grails err-buddy Plugin"
    def author = "Philipp Eschenbach"
    def authorEmail = "pesch3@gmail.com"
    def description = 'Err-buddy Grails client, and LogAppender'
    def documentation = "https://github.com/peh/grails-errbuddy"
    def license = "APACHE"
    def issueManagement = [ system: "GitHub", url: "https://github.com/peh/grails-errbuddy/issues" ]
    def scm = [ url: "https://github.com/peh/grails-errbuddy" ]

    def doWithApplicationContext = { ctx ->
        if (!application.config.grails.plugin.errbuddy.enabled) {
            return
        }

        Logger.rootLogger.addAppender(new ErrbuddyLogAppender(
            service: ctx.errbuddyService, application: application,
            name: 'errbuddyLogAppender', threshold: Level.WARN))
    }
}
