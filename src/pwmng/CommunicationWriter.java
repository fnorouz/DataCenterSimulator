/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pwmng;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.omg.PortableServer.THREAD_POLICY_ID;
import sun.security.krb5.internal.APOptions;

/**
 *
 * @author nooshin
 */
public class CommunicationWriter {

    public OutputStreamWriter writer;
    private final JTextArea text;
    boolean notFlushed;
    StringBuilder sb = new StringBuilder();
    final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();
    private final JTextArea initText;
    private final JTextArea destroyText;

    CommunicationWriter(FileOutputStream fileOutputStream, final JTextArea text, final JTextArea initText, final JTextArea destroyText) {
        writer = new OutputStreamWriter(fileOutputStream);
        this.text = text;
        newSingleThreadExecutor.submit(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    synchronized (CommunicationWriter.this) {
//                        flushMessages(notFlushed, sb);
                    }
                }
            }

            private void flushMessages(boolean notFlushed, final StringBuilder sb) {
                if (notFlushed) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            text.setText(sb.toString());
                        }
                    });
                }
            }
        });
        this.initText = initText;
        this.destroyText = destroyText;

    }

    void write(final String string) throws IOException {
//        writer.write(string);
//        synchronized (this) {
//            sb.append(string);
//            notFlushed = true;
//
//
//        }
    }

    void close() throws IOException {
        writer.close();
        newSingleThreadExecutor.shutdownNow();

    }

    void writeDestroy(final String string) throws IOException {
        writer.write(string);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                initText.setText(initText.getText() + string);
            }
        });
    }

    void writeInit(final String string) throws IOException {
        writer.write(string);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                destroyText.setText(destroyText.getText() + string);
            }
        });

    }

    void writeInit(AMGeneral AM, String name, int localTime) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Initialize ").append(AM).append(" of type ").append(AM.getAmType().getMoType()).append(" for ").append(name).append(" At Time= ").append(localTime).append("\n");
        if (AM != null && AM.Alive) {
//            sb.append("============== Begin AM Information =============\n");
            boolean found = false;
            for (AMGeneral aMGeneral : AM.getMyPeers()) {
                if (aMGeneral.Alive) {
                    found = true;
                    break;
                }
            }

            if (found) {
                sb.append("-------------- Begin AM Peers -------------\n");
                for (AMGeneral aMGeneral : AM.getMyPeers()) {
                    if (aMGeneral.Alive) {
                        sb.append("\t ").append(aMGeneral).append(" of type ").append(aMGeneral.amType.getMoType()).append("\n");
                    }
                }
                sb.append("-------------- End AM Peers -------------\n");
            }

            found = false;
            for (AMGeneral aMGeneral : AM.getMySubPrevillages()) {
                if (aMGeneral.Alive) {
                    found = true;
                    break;
                }
            }

            if (found) {
                sb.append("-------------- Begin AM Under Privillages -------------\n");
                for (AMGeneral aMGeneral : AM.getMySubPrevillages()) {
                    if (aMGeneral.Alive) {
                        sb.append("\t").append(aMGeneral).append(" of type ").append(aMGeneral.amType.getMoType()).append("\n");
                    }
                }

                sb.append("-------------- End AM Under Privillages -------------\n");
            }
        }



        writeInit(sb.toString());
    }
}
