package grails.plugins.errbuddy

import grails.plugins.Plugin

class ErrbuddyGrailsPlugin extends Plugin {
    def grailsVersion = "3.0.0 > *"
    def title = "Errbuddy"
    def author = "Philipp Eschenbach"
    def authorEmail = "philipp@errbuddy.net"
    def description = 'Errbuddy Grails client, and LogAppender'
    def documentation = "https://github.com/errbuddy/grails-errbuddy"
    def license = "APACHE"
    def organization = [name: "Errbuddy", url: "https://errbuddy.net"]

    def issueManagement = [system: "GitHub", url: "https://github.com/peh/grails-errbuddy/issues"]
    def scm = [url: "https://github.com/peh/grails-errbuddy"]
    def pluginExcludes = [
            "test/**"
    ]

    def doWithSpring = {
        println "fooaodasdoaisdoaismdoais"
    }

    def doWithApplicationContext = { applicationContext ->
        if (application.config.grails.plugin.errbuddy.enabled) {
            println "enabling ErrbuddyLogAppender"
            ErrbuddyLogAppender.instance.enable()
        }
    }
}
