package errbuddy

abstract class ErrbuddyPutObject {

    long time
    String bucket
    String message
    String hostname

    String actionName
    String controllerName
    String serviceName
    String path

    def request

    Map requestParameters
    Map sessionParameters

    protected abstract String getType()

    protected abstract Map getPostBodyExtension()

    Map getPostBody() {
        Map postBody = [
                time             : time,
                message          : message,
                controller       : controllerName,
                action           : actionName,
                service          : serviceName,
                path             : path,
                hostname         : hostname,
                requestParameters: requestParameters,
                sessionParameters: sessionParameters,
                type             : type
        ]
        postBody.putAll(postBodyExtension)
        postBody
    }

}
