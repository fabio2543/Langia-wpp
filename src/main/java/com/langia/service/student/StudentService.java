package com.langia.service.student;

import com.langia.dto.StudentCreateRequest;
import com.langia.entity.Student;
import com.langia.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(StudentCreateRequest request) {
        if (studentRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Student already exists with phone: " + request.getPhone());
        }

        Student student = new Student();
        student.setName(request.getName());
        student.setPhone(request.getPhone());
        student.setLanguage(request.getLanguage());
        student.setTimezone(request.getTimezone());
        student.setSource(request.getSource());

        return studentRepository.save(student);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }
}
