package ru.netology.homeworkfjddiploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.netology.homeworkfjddiploma.entity.DBFile;

import java.util.List;

@Repository
public interface DBFileRepository extends JpaRepository<DBFile, Long> {
    DBFile findDBFileByFilename(@Param("filename") String filename);

    @Modifying
    void deleteDBFileByFilename(@Param("filename") String filename);

    @Query(value = "select d from DBFile d")
    List<DBFile> findAll();

    @Modifying
    @Query(value = "update DBFile d set d.filename=:name where d.id=:id")
    void editFilenameById(@Param("name") String name, @Param("id") Long id);
}
