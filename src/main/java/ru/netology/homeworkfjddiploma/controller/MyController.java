package ru.netology.homeworkfjddiploma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.homeworkfjddiploma.model.AuthEditFilename;
import ru.netology.homeworkfjddiploma.model.FileResponse;
import ru.netology.homeworkfjddiploma.service.MyService;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:8081")
@RequestMapping(value = "/")
public class MyController {
    private final MyService myService;

    public MyController(MyService myService) {
        this.myService = myService;
    }

    @PostMapping(value = "file")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file, @RequestParam("filename") String filename) {
        return myService.uploadFile(file, filename);
    }

    @GetMapping(value = "file")
    public ResponseEntity<?> downloadFile(@RequestParam(value = "filename") String filename) {
        return myService.downloadFile(filename);
    }

    @DeleteMapping(value = "file")
    public ResponseEntity<?> deleteFile(@RequestParam(value = "filename") String filename) {
        return myService.deleteFile(filename);
    }

    @GetMapping("list")
    public ResponseEntity<List<FileResponse>> getFiles(@RequestParam(value = "limit") int limit) {
        return myService.getFiles(limit);
    }

    @PutMapping(value = "file")
    public ResponseEntity<?> updateFile(@RequestBody AuthEditFilename authEditFilename,
                                        @RequestParam(value = "filename") String filename) {
        return myService.updateFile(authEditFilename, filename);
    }
}

