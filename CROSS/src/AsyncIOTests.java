import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncIOTests {

    private static StringBuffer buffer = new StringBuffer();

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            try {
                while (true) {
                    
                    Thread.sleep(1000 * 3);

                    /*
                     * 2 cases:
                     * 1. The user wrote something.
                     * 2. The user didn't write anything.
                     * 
                     * 1. -> 
                     * 2. -> SOMETHING
                     */

                    String str = "\n";

                    str += "SPAM.\n";
                    str += "-> ";

                    str += buffer.toString();

                    // Only ONE print statement to avoid interleaving.
                    System.out.print(str);

                }
            } catch (Exception ex) {
                // TODO: Handle this exception.
            }
        });

        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)         // Usa stdin/stdout di sistema.
                .build()) {

            // Salva gli attributi "originali" per ripristinarli al termine.
            Attributes originalAttributes = terminal.getAttributes();

            // Entra in raw mode.
            terminal.enterRawMode();

            // Dopo essere entrati in raw mode, otteniamo gli attributi attuali per modificarli.
            Attributes rawAttributes = terminal.getAttributes();
            // Riabilitiamo l’ECHO (mostra tasto premuto) di default disabilitato in raw mode.
            rawAttributes.setLocalFlag(LocalFlag.ECHO, true);
            // Disabilitiamo il CR/NL (carriage return / new line) in raw mode.
            rawAttributes.setInputFlag(InputFlag.ICRNL, true); 
            // Applichiamo i nuovi attributi.
            terminal.setAttributes(rawAttributes);

            NonBlockingReader reader = terminal.reader();

            System.out.print("-> ");

            Boolean running = true;
            while (running) {

                // Legge una riga con timeout (ms). Se scade -> ch == NonBlockingReader.READ_EXPIRED.
                int ch = reader.read(100);

                if (ch == NonBlockingReader.READ_EXPIRED) {
                    // Nessun tasto premuto entro il timeout.
                    continue;
                }

                if (ch == -1) {
                    // EOF ricevuto (terminale chiuso).
                    System.out.println("EOF (-1) ricevuto. Uscita...");
                    running = false;
                } else if (ch == 4) {
                    // ASCII 4 = Ctrl + D in raw mode.
                    System.out.println("Ctrl + D catturato. Uscita...");
                    running = false;
                } else if (ch >= 0) {
                    // Altro carattere digitato, aggiungilo al buffer.
                    char c = (char) ch;
                    buffer.append(c);
                }

                if (buffer.length() > 0 && buffer.toString().endsWith("\n")) {
                    // Fine riga, '\n' rilevato.
                    String line = buffer.toString();
                    System.out.print("Riga letta: " + line);

                    // Reset buffer.
                    buffer.setLength(0);

                    System.out.print("-> ");
                }

            }

            System.out.println("Fine letture.");

            // Ripristina gli attributi originali.
            terminal.setAttributes(originalAttributes);

        } catch (IOException ex) {
            // TODO: Handle exception.
        }

    }

}