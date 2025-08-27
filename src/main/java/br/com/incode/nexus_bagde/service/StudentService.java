package br.com.incode.nexus_bagde.service;



import br.com.incode.nexus_bagde.dto.StudentDTO;
import br.com.incode.nexus_bagde.entitys.Student;
import br.com.incode.nexus_bagde.repository.Studentrepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    @Autowired
    private Studentrepository studentRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .collect(Collectors.toList());
    }

    public Optional<StudentDTO> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(student -> modelMapper.map(student, StudentDTO.class));
    }

    public Optional<StudentDTO> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .map(student -> modelMapper.map(student, StudentDTO.class));
    }

    public List<StudentDTO> searchStudents(String searchTerm) {
        return studentRepository.findBySearchTerm(searchTerm).stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .collect(Collectors.toList());
    }

    public StudentDTO createStudent(StudentDTO studentDTO) {
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new RuntimeException("Já existe um estudante com este email");
        }

        if (studentDTO.getRegistration() != null &&
                studentRepository.existsByRegistration(studentDTO.getRegistration())) {
            throw new RuntimeException("Já existe um estudante com esta matrícula");
        }

        Student student = modelMapper.map(studentDTO, Student.class);
        Student savedStudent = studentRepository.save(student);
        return modelMapper.map(savedStudent, StudentDTO.class);
    }

    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

        // Verificar se email já existe em outro estudante
        if (!existingStudent.getEmail().equals(studentDTO.getEmail()) &&
                studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new RuntimeException("Já existe um estudante com este email");
        }

        // Verificar se matrícula já existe em outro estudante
        if (studentDTO.getRegistration() != null &&
                !studentDTO.getRegistration().equals(existingStudent.getRegistration()) &&
                studentRepository.existsByRegistration(studentDTO.getRegistration())) {
            throw new RuntimeException("Já existe um estudante com esta matrícula");
        }

        existingStudent.setName(studentDTO.getName());
        existingStudent.setEmail(studentDTO.getEmail());
        existingStudent.setRegistration(studentDTO.getRegistration());
        existingStudent.setCourse(studentDTO.getCourse());

        Student updatedStudent = studentRepository.save(existingStudent);
        return modelMapper.map(updatedStudent, StudentDTO.class);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Estudante não encontrado");
        }
        studentRepository.deleteById(id);
    }
}
