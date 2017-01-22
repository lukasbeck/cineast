package org.vitrivr.cineast.api.rest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectAllActionHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.StatusInvokationHandler;
import org.vitrivr.cineast.api.rest.handlers.actions.FindObjectByActionHandler;

import static spark.Spark.*;

/**
 * This class establishes a RESTful endpoint listening on the specified port. Incoming requests are
 * routed towards an ActionHandler based on the HTTP method and the URI, provided that such a handler
 * hasn been registered beforehand.
 *
 * @see org.vitrivr.cineast.api.rest.handlers.interfaces.ActionHandler
 *
 * @author rgasser
 * @version 1.0
 * @created 19.01.17
 */
public class RestfulAPI {

    private static Logger LOGGER = LogManager.getLogger();

    /** Version of the protocol used by the RESTful endpoint. Will be appended to the endpoint URL. */
    private static final String VERSION = "v1";

    /** Named context of the RESTful endpoint. Will be appended to the endpoint URL. */
    private static final String CONTEXT = "api";

    /**
     * Starts the RESTful API.
     *
     * @param port Port on which the WebSocket endpoint should listen.
     * @param numberOfThreads Maximum number of threads that should be used to handle messages.
     */
    public static void start(int port, short numberOfThreads) {

        if (port > 0 && port <= 65535) port(port);
        threadPool(numberOfThreads);

        /* Register routes! */
        get(path("status"), new StatusInvokationHandler());
        get(path("find/object/by/:attribute/:value"), new FindObjectByActionHandler());
        get(path("find/object/all/:type"), new FindObjectAllActionHandler());
        //post(path("find/object/similar"), new FindObjectSimilarActionHandler());

        get(path("find/segment/all/:attribute/:value"), (request, response) -> {
            /* TODO: Implement! */
            return null;
        });
        get(path("find/segment/all/:type"), (request, response) -> {
            /* TODO: Implement! */
            return null;
        });

        /*
         * Configure the result after processing was completed.
         */
        after((request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
        });

        /* TODO: Add fine grained exception handling. */
        exception(Exception.class, (exception, request, response) -> {
            LOGGER.log(Level.ERROR, exception);
        });
    }

    /**
     *
     * @param name
     * @return
     */
    private static String path(String name) {
        return String.format("/%s/%s/%s", CONTEXT, VERSION, name);
    }
}
