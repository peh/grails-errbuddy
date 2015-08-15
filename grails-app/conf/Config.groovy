log4j = {
    error 'org.codehaus.groovy.grails',
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'
}

grails.plugin.errbuddy.host = "https://internal.errbuddy.net"
grails.plugin.errbuddy.path = "/api/error"
grails.plugin.errbuddy.apiKey = "mIk60j9J626chS0Spkv3O4Yz7y9Kk5HW4LsQzGT2UjD6vbHggAs7LmXHFHHDB4AT"
grails.plugin.errbuddy.exceptionsOnly = true
grails.plugin.errbuddy.params.exclude = ['password']
grails.plugin.errbuddy.enabled = true
