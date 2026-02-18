/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pack.crs.auth;

/**
 *
 * @author Leonardo
 */

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogStampUtility {
    /*private File logFile; //FOR TXT FILE
    private static final DateTimeFormatter TF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public LogStampUtility(String logFilePath) {
        this.logFile = new File(logFilePath);
    }

    public synchronized void appendStamp(String userId, String action, String flag) throws IOException {
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        String logId = UUID.randomUUID().toString();
        String ts = LocalDateTime.now().format(TF);
        String line = String.format("%s|%s|%s|%s|%s", logId, userId, action, ts, flag);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.write(line);
            bw.newLine();
        }
    }*/
    
    private File logFile; //FOR DAT (BINARY) FILE
    private static final DateTimeFormatter TF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public LogStampUtility(String logFilePath) {
        this.logFile = new File(logFilePath);
    }

    /**
     * Append a log stamp to a binary file.
     * action = "login" or "logout"
     * flag   = "1" (login) or "0" (logout)**/
     
    public synchronized void appendStamp(String userId, String action, String flag) throws IOException {
        if (!logFile.exists()) logFile.createNewFile();

        String logId = UUID.randomUUID().toString();
        String ts    = LocalDateTime.now().format(TF);
        int flagInt  = flag.equals("1") ? 1 : 0;

        try (DataOutputStream dos =
                     new DataOutputStream(new BufferedOutputStream(new FileOutputStream(logFile, true)))) {

            dos.writeUTF(logId);     // binary UTF string
            dos.writeUTF(userId);
            dos.writeUTF(action);
            dos.writeUTF(ts);
            dos.writeInt(flagInt);
        }
    }

    public List<String[]> readAllLogs() throws IOException {
        List<String[]> logs = new ArrayList<>();

        try (DataInputStream dis =
                     new DataInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
            while (dis.available() > 0) {
                String logId = dis.readUTF();
                String userId = dis.readUTF();
                String action = dis.readUTF();
                String ts = dis.readUTF();
                int flag = dis.readInt();

                logs.add(new String[]{
                    logId,
                    userId,
                    action,
                    ts,
                    String.valueOf(flag)
                });
            }
        } catch (EOFException e) {
            // normal: reached end of file
        }

        return logs;
    }

    
    /*optional helper for debugging: read binary log entries*/
    public void printAllLogs() throws IOException {
        try (DataInputStream dis =
                     new DataInputStream(new BufferedInputStream(new FileInputStream(logFile)))) {
            while (dis.available() > 0) {
                String logId = dis.readUTF();
                String userId = dis.readUTF();
                String action = dis.readUTF();
                String ts = dis.readUTF();
                int flag = dis.readInt();
                System.out.printf("%s | %s | %s | %s | %d%n", logId, userId, action, ts, flag);
            }
        }
    }
    
    
}
