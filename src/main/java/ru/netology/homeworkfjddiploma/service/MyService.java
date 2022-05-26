package ru.netology.homeworkfjddiploma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.homeworkfjddiploma.model.AuthEditFilename;
import ru.netology.homeworkfjddiploma.model.FileResponse;
import ru.netology.homeworkfjddiploma.repository.MyRepository;

import java.util.List;

@Service
public class MyService {

    @Autowired
    private MyRepository myRepository;

    public ResponseEntity<?> uploadFile(MultipartFile file, String filename) {
        return myRepository.uploadFile(file, filename);
    }

    public ResponseEntity<?> downloadFile(String filename) {
        return myRepository.downloadFile(filename);
    }

    public ResponseEntity<?> deleteFile(String filename) {
        return myRepository.deleteFile(filename);
    }

    public ResponseEntity<List<FileResponse>> getFiles(int limit) {
        return myRepository.getFiles(limit);
    }

    public ResponseEntity<?> updateFile(AuthEditFilename authEditFilename, String filename) {
        return myRepository.updateFile(authEditFilename, filename);
    }
}
