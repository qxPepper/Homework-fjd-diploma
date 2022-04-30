package ru.netology.homeworkfjddiploma.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.homeworkfjddiploma.entity.DBFile;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.*;

@Repository
public class MyRepository {
    private PreparedStatement ps = null;
    private final String INSERT_blob = "insert into %s(%s, %s, %s, %s) values(?, ?, ?, ?)";
    private final String SELECT_blob = "select * from %s where %s = ?";

    private final String url = "jdbc:mysql://mysql-service/my_database";
//    private final String url = "jdbc:mysql://localhost:3306/my_db_0";
    private final String username = "root";
    private final String password = "mysql";

        private static final String CLOUD_DIR = "/var/lib/cloud";
//    private static final String CLOUD_DIR = "cloud/";

    private Connection connection = createConnection();

    @Autowired
    private MyUserRepository myUserRepository;
    public MyUserRepository getMyUserRepository() {
        return myUserRepository;
    }

    @Autowired
    private DBFileRepository dbFileRepository;
    public DBFileRepository getDbFileRepository() {
        return dbFileRepository;
    }

    public ResponseEntity<?> uploadFile(MultipartFile file, String filename) {
        Path target = Paths.get(CLOUD_DIR + File.separator
                + StringUtils.cleanPath(Objects.requireNonNull(filename)));
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return isFilePresent(target.getFileName().toString(), new File(CLOUD_DIR))
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    public ResponseEntity<?> downloadFile(String filename) {
        Blob blob = null;
        String content = "";

        File file = new File(CLOUD_DIR + filename);
        Timestamp date = new Timestamp(System.currentTimeMillis());

        Long size = (Long) file.length();

        try {
            String sql = String.format(INSERT_blob, "blobs", "data", "filename", "date", "size");
            FileInputStream is = new FileInputStream(file);
            ps = connection.prepareStatement(sql);
            ps.setBinaryStream(1, is, size);
            ps.setString(2, filename);
            ps.setTimestamp(3, date);
            ps.setLong(4, size);
            ps.executeUpdate();

            connection.commit();
            ps.close();

            sql = String.format(SELECT_blob, "blobs", "filename");
            ps = connection.prepareStatement(sql);
            ps.setString(1, filename);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                blob = rs.getBlob(2);
                content = rs.getString(2);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return !content.equals("")
                ? new ResponseEntity<>(content, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Transactional
    public ResponseEntity<?> deleteFile(String filename) {
        if (!isFilePresent(filename)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            dbFileRepository.deleteDBFileByFilename(filename);
            return !isFilePresent(filename)
                    ? new ResponseEntity<>(HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<List<String>> getFiles(int limit) {
        List<DBFile> allFiles = dbFileRepository.findAll();
        List<String> files = new ArrayList<>();
        int index = 0;

        for (DBFile item : allFiles) {
            if (index >= limit) {
                break;
            } else {
                files.add(item.getFilename());
                index++;
            }
        }
        return !files.isEmpty()
                ? new ResponseEntity<>(files, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Transactional
    public ResponseEntity<?> editFilename(String name, String filename) {
        DBFile fileIn = getDbFileRepository().findDBFileByFilename(filename);

        getDbFileRepository().editFilenameById(name, fileIn.getId());

        return isFilePresent(name)
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    }

    // private
    private boolean isFilePresent(String filename, File dir) {
        boolean isFile = false;
        for (File item : Objects.requireNonNull(dir.listFiles())) {
            if (item.isFile()) {
                if (item.getAbsoluteFile().toString()
                        .contains(Objects.requireNonNull(filename))) {
                    isFile = true;
                    break;
                }
            }
        }
        return isFile;
    }

    private boolean isFilePresent(String filename) {
        boolean isFile = false;
        DBFile dbFile = dbFileRepository.findDBFileByFilename(filename);
        if (dbFile != null) {
            isFile = true;
        }
        return isFile;
    }

    private Connection createConnection() {
        try {
            Properties properties = new Properties();
            properties.setProperty("password", password);
            properties.setProperty("user", username);
            properties.setProperty("useUnicode", "true");
            properties.setProperty("characterEncoding", "utf8");

            connection = DriverManager.getConnection(url, properties);
            connection.setAutoCommit(false);

        } catch (SQLException e) {
            connection = null;
        }
        return connection;
    }
}
