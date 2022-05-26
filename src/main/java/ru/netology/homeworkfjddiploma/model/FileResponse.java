package ru.netology.homeworkfjddiploma.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {
    private String filename;
    private String date;
    private int size;
}
