/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pack.crs.auth;
import pack.crs.models.*;

/**
 *
 * @author Leonardo
 */
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

//data access layer to handle data logic
public class UserDAO {
       private File usersFile;

    public UserDAO(String usersFilePath) {
        this.usersFile = new File(usersFilePath);
    }


    public User authenticate(String username, String password) throws IOException {
        if (!usersFile.exists()) {
            throw new FileNotFoundException("users file not found: " + usersFile.getAbsolutePath());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.toLowerCase().startsWith("userid")) continue;

                StringTokenizer st = new StringTokenizer(line, "|");
                if (st.countTokens() < 4) continue;

                String userId = st.nextToken().trim();
                String uName  = st.nextToken().trim();
                String pwd    = st.nextToken().trim();
                String role   = st.nextToken().trim();
                boolean active = true;
                if (st.hasMoreTokens()) {
                    String act = st.nextToken().trim();
                    active = !"0".equals(act);
                }

                if (uName.equals(username) && pwd.equals(password)){
                  
                    if (!active) {  //if account is not active
                        // return a placeholder user to signal as "deactivated"
                        if ("SystemAdmin".equalsIgnoreCase(role)){
                            return new SystemAdmin(userId, uName, pwd, false);
                        } else {
                            return new AcademicOfficer(userId, uName, pwd, false);
                        }
                    }

                    //if account is active
                    if ("SystemAdmin".equalsIgnoreCase(role)) {
                        return new SystemAdmin(userId, uName, pwd, true);
                    } else {
                        return new AcademicOfficer(userId, uName, pwd, true);
                    }
                    
                }
            }
        }
        return null; // if no credential matches found
    }
    
    // load all users
    public List<User> loadAllUsers() throws IOException {
        List<User> list = new ArrayList<>();
        if (!usersFile.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.toLowerCase().startsWith("userid")) continue;
                String[] tokens = line.split("\\|", -1); // keep empty tokens
                // details: id|username|password|role|active|email
                String id = tokens.length > 0 ? tokens[0].replace("\uFEFF","").trim() : "";
                String uname = tokens.length > 1 ? tokens[1].trim() : "";
                String pwd = tokens.length > 2 ? tokens[2].trim() : "";
                String role = tokens.length > 3 ? tokens[3].trim() : "";
                boolean active = true;
                String email = "";
                if (tokens.length > 4) {
                    String a = tokens[4].trim();
                    active = !"0".equals(a);
                }
                if (tokens.length > 5) {
                    email = tokens[5].trim();
                }
                
                if ("SystemAdmin".equalsIgnoreCase(role)){
                    list.add(new SystemAdmin(id,uname,pwd,active,email));
                } else if ("AcademicOfficer".equalsIgnoreCase(role)){
                    list.add(new AcademicOfficer(id,uname,pwd,active,email));
                }
            }
        }
        return list;
    }

    // save entire list safely (write content to temp file then replace)
    public void saveAllUsers(List<User> users) throws IOException {
        File tmp = new File(usersFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmp))) {
            // optional header
            // bw.write("userID|username|password|role|active");
            // bw.newLine();
            for (User u : users) {
                String activeFlag = u.isActive() ? "1" : "0";
                String email = u.getEmail();
                if (email == null) email = "";
                if (email.length() > 100){
                    email = email.substring(0,100); //char limit for email
                }
                String line = String.format("%s|%s|%s|%s|%s|%s",
                        u.getUserId(), u.getUsername(), u.getPassword(), u.getRole(), activeFlag, email);
                bw.write(line);
                bw.newLine();
            }
        }
        // replace one by one
        if (!tmp.renameTo(usersFile)) {
            // fallback: copy and delete
            try (InputStream in = new FileInputStream(tmp);
                 OutputStream out = new FileOutputStream(usersFile)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
            tmp.delete();
        }
    }

    // helper to create new unique userId (simple incremental)
    public String nextUserId() throws IOException {
        List<User> list = loadAllUsers();
        int max = 0;
        for (User u : list) {
            try { max = Math.max(max, Integer.parseInt(u.getUserId())); } catch (Exception e) {}
        }
        return String.valueOf(max + 1);
    }
}
