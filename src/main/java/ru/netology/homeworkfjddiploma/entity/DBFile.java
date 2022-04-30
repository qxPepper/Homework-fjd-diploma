package ru.netology.homeworkfjddiploma.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;


@Data
@Entity
@Table(name = "blobs")
public class DBFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Timestamp date;

    @Column(nullable = false)
    private Long size;

    @Lob
    private byte[] data;
}

