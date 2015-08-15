grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compile 'org.apache.httpcomponents:httpclient:4.5'
        compile 'org.apache.httpcomponents:httpcore:4.4.1'
    }

    plugins {
        build ':release:3.0.1', ':rest-client-builder:2.0.3' {
            export = false
        }

    }
}
