package comp.app.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
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
    private static FileHandler sLog;
    
    private static final String TOKEN_LOG = "Tokens.log";
    private static final String LOG_FILE  = "Compiler.log";

    static {
    	// Nao mostra o log na saida padrao
    	sLogger.setUseParentHandlers(false);
    }

    /** Ativa/desativa logs */
    public static final boolean ENABLED = true;

    public static void clearCompilerLog() {
        File tokens = new File(LOG_FILE);
        if (tokens.exists()) {
            boolean deleted = tokens.delete();
            if (!deleted) {
                // TODO fazer alguma coisa quando o arquivo nao e removido
            }
        }        
    }

    /**
     * Limpa todos os arquivos de log
     * (Recomendado ser chamado antes de iniciar o programa)
     */
    public static void clearLogFiles() {
        clearTokensLog();
        clearCompilerLog();
    }

    public static void message(String message) {
        try {
            sLog = new FileHandler(LOG_FILE, true);
            sLog.setFormatter(new LogFormatter());
            sLogger.addHandler(sLog);
            sLogger.info(message);
            sLog.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void error(String message, Throwable thrown) {
        try {
            sLog = new FileHandler(LOG_FILE, true);
            sLog.setFormatter(new LogFormatter());
            sLogger.addHandler(sLog);
            sLogger.log(Level.WARNING, message, thrown);
            sLog.close();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Mensagens de erro tambem sao exibidas na saida padrao
        thrown.printStackTrace();
    }

    private static class LogFormatter extends Formatter {

        private SimpleDateFormat dateFormatter = new SimpleDateFormat("hh:mm:ss aaa");
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
            sb.append(dateFormatter.format(new Date()));
            sb.append(" LOG");
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

/*------------------ Log de tokens Tokens.log ---------------------*/


    public static void clearTokensLog() {
        File tokens = new File(TOKEN_LOG);
        if (tokens.exists()) {
            boolean deleted = tokens.delete();
            if (!deleted) {
                // TODO fazer alguma coisa quando o arquivo nao e removido
            }
        }        
    }

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
            sb.append(lineSeparator);
            return sb.toString();
        }
    }

}
