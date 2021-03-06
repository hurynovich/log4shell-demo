# Log4shell demo
![Pure Java](https://img.shields.io/badge/Pure-Java-orange.svg)

## Introduction
This project demonstrates how [Log4shell](https://en.wikipedia.org/wiki/Log4Shell) vulnarability allows hacker to inject and execute eny code.\
It consist of two applications:
- [victim](victim/) - application which has Log4shell vulnarability.
- [hacker](hacker/) - application which exploits given vulnerability.

> :warning: This project was written for study purpose only. Do not use it for unauthorised hacking.

## How to run

## References
- [RFC 2713 - Schema for Representing Java Objects in an LDAP Directory](https://datatracker.ietf.org/doc/html/rfc2713)
- [Using the In-Memory Directory Server](https://docs.ldap.com/ldap-sdk/docs/in-memory-directory-server.html)
- [Log4Shell: RCE 0-day exploit found in log4j 2](https://www.lunasec.io/docs/blog/log4j-zero-day/)
- [A journey from JNDI/LDAP manipulation to remote code execution dream land](https://www.blackhat.com/docs/us-16/materials/us-16-Munoz-A-Journey-From-JNDI-LDAP-Manipulation-To-RCE.pdf)
- [Java Objects and the Directory - LDAP Directories ](https://docs.oracle.com/javase/jndi/tutorial/objects/representation/ldap.html)
