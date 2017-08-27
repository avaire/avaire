package com.avairebot.orion.logger;

import com.avairebot.orion.Constants;
import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.logger.AbstractLogger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger extends AbstractLogger {

    private final File logFolder = new File(Constants.STORAGE_PATH, "logs");
    private final SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat fileDate = new SimpleDateFormat("yyyy-MM-dd");

    private File logFile;

    public Logger(Orion orion) {
        super(orion);

        if (!logFolder.isDirectory() && !logFolder.mkdirs()) {
            System.err.println("Failed to create storage/logs directory!");
            System.err.println("Please make sure that the bot has permissions to create folders.");
            System.exit(0);
        }

        this.logFile = new File(this.logFolder, this.getFileTimestamp() + ".log");
        if (!this.logFile.exists()) {
            try {
                this.logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void log(Level level, String message) {
        this.log(String.format("%s %s %s", this.getDateTimestamp(), level.getPrefix(), message));
    }

    @Override
    public void log(Level level, String message, Exception ex) {
        this.log(String.format("%s %s %s", this.getDateTimestamp(), level.getPrefix(), message));
        this.log(this.buildExceptionStackTrace(ex));
    }

    private void log(String message) {
        System.out.println(message);

        if (!logFile.getName().equals(getFileTimestamp() + ".log")) {
            logFile = new File(this.logFolder, getFileTimestamp() + ".log");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(logFile, true);
            bw = new BufferedWriter(fw);

            bw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String buildExceptionStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        return sw.toString();
    }

    private String getDateTimestamp() {
        return logDate.format(Calendar.getInstance().getTime());
    }

    private String getFileTimestamp() {
        return fileDate.format((Calendar.getInstance().getTime()));
    }
}
