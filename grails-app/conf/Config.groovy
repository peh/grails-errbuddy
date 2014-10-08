log4j = {
    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'
}

grails.plugin.errbuddy.host = "http://errbuddy.net"
grails.plugin.errbuddy.path = "/api/put/"
grails.plugin.errbuddy.exceptionsOnly = true
grails.plugin.errbuddy.params.exclude = ['password']
