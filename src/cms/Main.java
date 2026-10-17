package cms;

import java.util.ArrayList;
import java.io.*;


public class Main {

    public static void main(String[] args) {
        dataFilesExist();
        createDefaultAdmin();

        java.awt.EventQueue.invokeLater(() -> new LoginPage().setVisible(true));
    }

    // Makes sure all four data files exist on disk before anything tries to
    // read/write them. Existing files and their content are left untouched -
    // this only creates a file if it's genuinely missing (e.g. first run on
    // a fresh machine, or after a fresh git clone where .txt data files
    // aren't committed).
    private static void dataFilesExist() {
        String[] fileNames = {
            FileManager.USERS_FILE,
            FileManager.APPOINTMENTS_FILE,
            FileManager.RECORDS_FILE,
            FileManager.SCHEDULE_FILE
        };

        for (String fileName : fileNames) {
            File file = new File(fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    System.out.println("Created missing file: " + fileName);
                } catch (IOException e) {
                    System.out.println("Error creating " + fileName + ": " + e.getMessage());
                }
            }
        }
    }

    // Only creates the default Admin account if users.txt is genuinely empty
    // (first run) - avoids creating a duplicate "admin" account every time
    // the app is launched once real data already exists.
    private static void createDefaultAdmin() {
        FileManager fileManager = new FileManager();
        ArrayList<User> users = fileManager.loadUsers();
        if (!users.isEmpty()) {
            return;
        }

        String adminID = fileManager.generateId(FileManager.USERS_FILE, "A");
        fileManager.registerUser(new Admin(adminID, "admin", "admin123", "YC Admin"));
        System.out.println("Default admin account created: admin / admin123");
    }
}
