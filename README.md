grails-errbuddy
===============

Grails Err-buddy plugin.

This Plugin provides a LogAppender which automatically sends Log messages to err-buddy.
It enriches those with session and request parameters as well as full stacktraces (if provided) so you have the full usage of err-buddy.

# Installation

Add this line to the plugins section of your `BuildConfig.groovy`

```
compile ":errbuddy:1.1.2"
```

# Configuration & Usage

If you have not yet signed up on [errbuddy.net](http://errbuddy.net) do so now. The most important part of the Configuration is your ApiKey which you will find in the Application Settings on [errbuddy.net](http://errbuddy.net).

This is a basic configuration that is used by the err-buddy app itself

```groovy
grails {
    plugin {
        errbuddy {
            enabled = true // whether the plugin is enabled and the LogAppender is registered with the root logger
            apiKey = 'YOUR_API_KEY' // Your api key from errbuddy.net
            exceptionsOnly = false // whether to send more then just exceptions
            threshold = "WARN" // if exceptionsOnly is false, this is the threshold 
            params {
                exclude = ['password', 'SPRING_SECURITY_CONTEXT', 'currentApplication', 'applications'] // which parameters to be excluded from sending
            }
        }
    }
}
```


# Deployment tracking

the errbuddyService bean has a postDeployment method. Which can be used in 3 different versions

```groovy
errbuddyService.postDeployment()
```

will send a deployment request to err-buddy with the app.version from application.properties and will leave the hostname resolving to the err-buddy server

```groovy
errbuddyService.postDeployment("foo-1")
```

will send a deployment request to err-buddy with foo-1 as the version. Hostname will still be resolved by the err-buddy server

```groovy
errbuddyService.postDeployment("foo-1", "bar.host.com")
```

will send a deployment to err-buddy with foo-1 as the version and bar.host.com as the host.


# ToDo
* update Documentation to also cover custom message sending
* add performance tracking


# History
* 19th January 2015 - 1.1.2 released
** added deployment tracking
* 6th October 2014 - 1.0.0 released
** initial release with LogAppender