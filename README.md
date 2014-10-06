grails-errbuddy
===============

Grails Errbuddy plugin

# Installation

Add this line to the plugins section of your `BuildConfig.groovy`


```
compile ":errbuddy:1.0.0"
```

# Configuration

If you have not yet signed up on [errbuddy.net](http://errbuddy-net) do so now. The most important part of the Configuration is your ApiKey which you will find in the Application Settings on [errbuddy.net](http://errbuddy-net).

This is a basic configuration that is used by the errbuddy app itself

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

That's it.


# ToDo
* update Documentation to also cover custom messag sending
* add performance tracking


# History
* 6th October 2014 - 1.0.0 released
** initial release with LogAppender