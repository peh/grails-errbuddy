package errbuddy

class ErrbuddyLogObject extends ErrbuddyPutObject {

    Map payload
    String level

    private static final String TYPE = 'LOG'

    @Override
    protected String getType() {
        TYPE
    }

    @Override
    protected Map getPostBodyExtension() {
        [
                payload: payload,
                level: level
        ]
    }
}
