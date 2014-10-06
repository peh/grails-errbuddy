import errbuddy.ErrbuddyLogAppender
import org.apache.log4j.Logger

class ErrbuddyGrailsPlugin {
    // the plugin version
    def version = "1.0.0"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.4 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails Errbuddy Plugin" // Headline display name of the plugin
    def author = "Philipp Eschenbach"
    def authorEmail = "pesch3@gmail.com"
    def description = '''Err-buddy Grails client, and LogAppender.'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/peh/grails-errbuddy"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

//    Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/peh/grails-errbuddy/issues" ]

//    Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/peh/grails-errbuddy" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        if(application.config.grails.plugin.errbuddy.enabled)
            Logger.rootLogger.addAppender(new ErrbuddyLogAppender(service: ctx.errbuddyService, application: application, name: 'errbuddyLogAppender', threshold: org.apache.log4j.Level.WARN))
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->


    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
