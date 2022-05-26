package ru.netology.homeworkfjddiploma.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.homeworkfjddiploma.entity.DBFile;
import ru.netology.homeworkfjddiploma.model.AuthEditFilename;
import ru.netology.homeworkfjddiploma.model.FileResponse;

import java.io.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
@Transactional
public class MyRepository {
    private PreparedStatement ps = null;
    private final String INSERT_blob = "insert into %s(%s, %s, %s, %s) values(?, ?, ?, ?)";
    private final String SELECT_blob = "select * from %s where %s = ?";

    private final String url = "jdbc:mysql://mysql-service/my_database";
    private final String username = "root";
    private final String password = "mysql";

    private final int BUFFER_length = 2 * 1024;

    private static final String CLOUD_DIR = "/var/lib/cloud/";

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
        String content = "";
        Blob blob = null;

        if (isFilePresent(filename)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            ZoneId zona = ZoneId.of("Europe/Moscow");
            LocalDateTime date = LocalDateTime.now(zona);

            int size = (int) file.getSize();

            try {
                String sql = String.format(INSERT_blob, "blobs", "data", "filename", "date", "size");

                FileInputStream is = (FileInputStream) file.getInputStream();
                ps = connection.prepareStatement(sql);
                ps.setBinaryStream(1, is, size);
                ps.setString(2, filename);
                ps.setObject(3, date);
                ps.setInt(4, size);
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
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(content, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> downloadFile(String filename) {
        String content = "";
        Blob blob = null;
        String sql = String.format(SELECT_blob, "blobs", "filename");

        if (!isFilePresent(filename)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            try {
                ps = connection.prepareStatement(sql);
                ps.setString(1, filename);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    blob = rs.getBlob(2);
                    content = rs.getString(2);
                }

                if (blob != null) {
                    OutputStream os = new FileOutputStream(CLOUD_DIR + filename);
                    InputStream is = blob.getBinaryStream();
                    int length = -1;
                    byte[] buf = new byte[BUFFER_length];
                    while ((length = is.read(buf)) != -1) {
                        os.write(buf, 0, length);
                    }
                    is.close();
                    os.close();
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            return isFilePresent(filename, new File(CLOUD_DIR))
                    ? new ResponseEntity<>(content, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> deleteFile(String filename) {
        if (!isFilePresent(filename)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            dbFileRepository.deleteDBFileByFilename(filename);
            return !isFilePresent(filename)
                    ? new ResponseEntity<>(HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<List<FileResponse>> getFiles(int limit) {
        List<DBFile> allFiles = dbFileRepository.findAll();
        if (allFiles == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<FileResponse> files = new ArrayList<>();

        if (allFiles.size() >= 1) {
            int index = 0;

            for (DBFile item : allFiles) {
                if (index >= limit) {
                    break;
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm");

                    FileResponse fileResponse = new FileResponse(item.getFilename(),
                            item.getDate().format(formatter), item.getSize());
                    files.add(fileResponse);
                    index++;
                }
            }
        }
        return files != null
                ? new ResponseEntity<>(files, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<?> updateFile(AuthEditFilename authEditFilename, String filename) {
        DBFile fileIn = getDbFileRepository().findDBFileByFilename(filename);
        if (!isFilePresent(filename)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        getDbFileRepository().editFilenameById(authEditFilename.getName(), fileIn.getId());

        return isFilePresent(authEditFilename.getName())
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // private
    private boolean isFilePresent(String filename) {
        boolean isFile = false;
        DBFile dbFile = dbFileRepository.findDBFileByFilename(filename);
        if (dbFile != null) {
            isFile = true;
        }
        return isFile;
    }

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
