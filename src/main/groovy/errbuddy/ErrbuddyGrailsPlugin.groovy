package errbuddy

import grails.plugins.Plugin

class ErrbuddyGrailsPlugin extends Plugin {
    def grailsVersion = "3.1.4 > *"
    def title = "Errbuddy"
    def author = "Philipp Eschenbach"
    def authorEmail = "philipp@errbuddy.net"
    def description = 'Errbuddy Grails client, and LogAppender'
    def documentation = "https://github.com/errbuddy/grails-errbuddy"
    def license = "APACHE"
    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]
    def issueManagement = [system: "GitHub", url: "https://github.com/peh/grails-errbuddy/issues"]
    def scm = [url: "https://github.com/peh/grails-errbuddy"]
    def pluginExcludes = [
            "test/**",
    ]

    def doWithSpring = {

        errbuddyLogAppender(ErrbuddyLogAppender) { bean ->
            bean.autowire = "byName"
        }
    }

    def doWithApplicationContext = { ctx ->
        if (application.config.grails.plugin.errbuddy.enabled) {
            ctx.errbuddyLogAppender.enable()
        }
    }
}
