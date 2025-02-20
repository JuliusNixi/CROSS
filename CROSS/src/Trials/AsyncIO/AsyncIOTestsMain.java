package Trials.AsyncIO;

import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// IT: Documentazione di questo file solo in italiano. 
// EN: Documentation of this file only in italian.

/**
 * 
 * Esempio di lettura non bloccante da terminale con la libreria JLine 3.
 * Utilizzata nel progetto CROSS, nella CLI del client per la gestione dell'input.
 * Risolve il problema di dover leggere l'input e contemporaneamente ricevere dati asincronamente da stampare.
 * 
 */
public class AsyncIOTestsMain {

    private static StringBuffer buffer = new StringBuffer();

    public static void main(String[] args) {

        // Un semplice thread che "spamma" ogni 3 secondi per simulare l'arrivo asincrono di dati.
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            try {
                while (true) {
                    
                    Thread.sleep(1000 * 3);

                    /*
                     * 2 casi:
                     * 1. L'utente ha già digitato qualcosa.
                     * 2. L'utente non ha digitato nulla.
                     * 
                     * Esempio:
                     * 1. -> 
                     * 2. -> SOMETHING
                     */

                    String str = "\n";
                    str += "SPAM.\n";
                    str += "-> ";
                    // Per riscrivere, se presente, il buffer, cioè i caratteri digitati dall'utente nel caso 2.
                    str += buffer.toString();

                    // Effettuare la stampa in un'unica istruzione per evitare l'interleaving di stampe tra i thread.
                    System.out.print(str);

                }
            } catch (Exception ex) {
                System.err.println("Errore nello spamming thread: " + ex.getMessage());
                ex.printStackTrace();
                System.exit(-1);
            }

        });

        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)         // Usa stdin / stdout di sistema.
                .build()) {

            // Salva gli attributi "originali" del terminale per ripristinarli al termine.
            Attributes originalAttributes = terminal.getAttributes();

            // Entra in raw mode, per avere un controllo più fine sull'input e nessuna proprietà pre-impostata.
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

            // Stampiamo il prompt iniziale.
            System.out.print("-> ");

            // Loop finchè non usciamo.
            Boolean running = true;
            while (running) {

                // Legge un carattere con timeout (ms). 
                // Se scade -> ch == NonBlockingReader.READ_EXPIRED.
                int ch = reader.read(100);

                if (ch == NonBlockingReader.READ_EXPIRED) {
                    // Nessun tasto premuto entro il timeout. Torna all'inizio del ciclo a leggere nuovamente.
                    continue;
                }

                if (ch == -1) {
                    // EOF ricevuto (terminale chiuso).
                    System.out.println("EOF (-1) ricevuto. Uscita...");
                    running = false;
                    continue;
                } else if (ch == 4) {
                    // ASCII 4 = Ctrl + D in raw mode.
                    System.out.println("Ctrl + D catturato. Uscita...");
                    running = false;
                    continue;
                } else if (ch >= 0) {
                    // Altro carattere digitato, aggiungilo al buffer.
                    char c = (char) ch;
                    buffer.append(c);
                }

                if (buffer.length() > 0 && buffer.toString().endsWith("\n")) {
                    // Fine riga, '\n' rilevato. Comando inserito dall'utente.
                    String line = buffer.toString();
                    // '\n' già presente nel buffer inserito dall'utente.
                    System.out.print("Riga letta: " + line);

                    // Uscita.
                    if (line.trim().equalsIgnoreCase("exit")) {
                        running = false;
                        continue;
                    }

                    // Reset buffer.
                    buffer.setLength(0);

                    // Stampa prompt.
                    System.out.print("-> ");

                }

            }

            System.out.println("Fine letture.");

            // Ripristina gli attributi originali.
            terminal.setAttributes(originalAttributes);

        } catch (IOException ex) {
            System.err.println("Errore I/O durante il setting o l'uso del terminale: " + ex.getMessage());
            ex.printStackTrace();

            System.exit(-1);
        }

    }

}
