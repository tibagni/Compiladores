package comp.app.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import comp.app.Token;

/**
 * C_Log - Compiler Log
 * 
 * @author Tiago
 */
public class C_Log {
    private static Logger sLogger = Logger.getLogger("C_Log");
    private static FileHandler sTokens;
    
    private static final String TOKEN_LOG = "Tokens.log";

    static {
    	// Nao mostra o log na saida padrao
    	sLogger.setUseParentHandlers(false);
    }

    /** Ativa/desativa logs */
    public static final boolean ENABLED = true;

    public static void logToken(Token token) {
        try {
            sTokens = new FileHandler(TOKEN_LOG, true);
            sTokens.setFormatter(new TokenFormatter());
            sLogger.addHandler(sTokens);
            sLogger.info(token.toString());
            sTokens.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Limpa todos os arquivos de log
     * (Recomendado ser chamado antes de iniciar o programa)
     */
    public static void clearLogFiles() {
        clearTokensLog();
    }

    public static void clearTokensLog() {
        File tokens = new File(TOKEN_LOG);
        if (tokens.exists()) {
            boolean deleted = tokens.delete();
            if (!deleted) {
                // TODO fazer alguma coisa quando o arquivo nao e removido
            }
        }        
    }

    private static class TokenFormatter extends Formatter {

        // Line separator string.  This is the value of the line.separator
        // property at the moment that the SimpleFormatter was created.
        private String lineSeparator = (String) java.security.AccessController.doPrivileged(
                   new sun.security.action.GetPropertyAction("line.separator"));

        /**
         * Format the given LogRecord.
         * @param record the log record to be formatted.
         * @return a formatted log record
         */
        public synchronized String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            // Minimize memory allocations here.

            String message = formatMessage(record);
            //sb.append(record.getLevel().getLocalizedName());
            sb.append("TOKEN");
            sb.append(": ");
            sb.append(message);
            sb.append(" ");
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            sb.append(lineSeparator);
            return sb.toString();
        }
    }

}
