package com.nickmcdowall.lsd.http.common;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.nickmcdowall.lsd.http.naming.DestinationNameMappings;
import com.nickmcdowall.lsd.http.naming.SourceNameMappings;
import lombok.EqualsAndHashCode;

import java.util.Map;

import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.SOURCE_SERVICE_NAME;
import static com.nickmcdowall.lsd.http.common.Headers.HeaderKeys.TARGET_SERVICE_NAME;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.requestOf;
import static com.nickmcdowall.lsd.http.common.HttpInteractionMessageTemplates.responseOf;

@EqualsAndHashCode
public class DefaultHttpInteractionHandler implements HttpInteractionHandler {

    private final TestState testState;
    private final SourceNameMappings sourceNameMappings;
    private final DestinationNameMappings destinationNameMappings;

    public DefaultHttpInteractionHandler(TestState testState, SourceNameMappings sourceNameMappings, DestinationNameMappings destinationNameMappings) {
        this.testState = testState;
        this.sourceNameMappings = sourceNameMappings;
        this.destinationNameMappings = destinationNameMappings;
    }

    @Override
    public void handleRequest(String method, Map<String, String> headers, String path, String body) {
        String sourceName = deriveSourceName(headers, path);
        String destinationName = deriveTargetName(headers, path);
        testState.log(requestOf(method, path, sourceName, destinationName), body);
    }

    @Override
    public void handleResponse(String statusMessage, Map<String, String> headers, String path, String body) {
        String destinationName = deriveTargetName(headers, path);
        String sourceName = deriveSourceName(headers, path);
        testState.log(responseOf(statusMessage, destinationName, sourceName), body);
    }

    private String deriveTargetName(Map<String, String> headers, String path) {
        return headers.containsKey(TARGET_SERVICE_NAME.key())
                ? headers.get(TARGET_SERVICE_NAME.key())
                : destinationNameMappings.mapForPath(path);
    }

    private String deriveSourceName(Map<String, String> headers, String path) {
        return headers.containsKey(SOURCE_SERVICE_NAME.key())
                ? headers.get(SOURCE_SERVICE_NAME.key())
                : sourceNameMappings.mapForPath(path);
    }
}