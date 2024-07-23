package it.portfolio.hr.humanResource.services;

import it.portfolio.hr.humanResource.entities.*;
import it.portfolio.hr.humanResource.exceptions.employee.EmployeesException;
import it.portfolio.hr.humanResource.exceptions.hirirng.HiringException;
import it.portfolio.hr.humanResource.models.DTOs.request.EmployeesRequestDTO;
import it.portfolio.hr.humanResource.models.DTOs.response.*;
import it.portfolio.hr.humanResource.repositories.*;
import it.portfolio.hr.humanResource.validator.EmployeesValidator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class EmployeeService {

    @Autowired
    private EmployeesRepository employeesRepository;
    @Autowired
    private EmployeesValidator employeesValidator;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private HiringRepository hiringRepository;
    @Autowired
    private PermitsRepository permitsRepository;

    @Autowired
    private OvertimeRepository overtimeRepository;
    @Autowired
    private SickDaysRepository sickDaysRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${pathStorageProfileImages}")
    private String pathProfileImages;

    public EmployeesResponseDTO createEmployee(EmployeesRequestDTO employeesRequestDTO, String companyName) throws HiringException, EmployeesException, IOException {
        if (employeesValidator.isEmployeeValid(employeesRequestDTO, companyName)) {
            Employees employees = new Employees(employeesRequestDTO.getName(),
                    employeesRequestDTO.getAddress(),
                    employeesRequestDTO.getFiscalCode(),
                    employeesRequestDTO.getHiringDate(),
                    companyName,
                    false
            );
            employeesRepository.saveAndFlush(employees);
            return modelMapper.map(employees, EmployeesResponseDTO.class);
        }
        throw new EmployeesException("The inserted employee's information's are not valid", 400);
    }

    public boolean uploadProfileImage(MultipartFile profileImage, Long id, String companyName) throws IOException {
        try {
            Set<String> validExtensions = Set.of("png", "jpeg", "jpg");
            if (profileImage == null || profileImage.getOriginalFilename() == null) {
                throw new IOException("File is empty or missing.");
            }
            Employees employees = employeesRepository.findById(id, companyName).orElseThrow(() -> new IOException("No employee found with this id"));
            String extension = FilenameUtils.getExtension(profileImage.getOriginalFilename()).toLowerCase();

            if (!validExtensions.contains(extension)) {
                throw new IOException("Invalid file type. Allowed types are: png, jpeg, jpg.");
            }
            String profileImageName = (employees.getName() + "." + extension).replace(" ", "");
            File destinationDirectory = new File(pathProfileImages, profileImageName);
            File controlFile = new File(pathProfileImages);
            if (!controlFile.exists()) throw new IOException("Folder doesn't exist");
            if (!controlFile.isDirectory()) throw new IOException("This is not a directory");
            if (destinationDirectory.exists()) throw new IOException("File already exist");
            profileImage.transferTo(destinationDirectory.toPath());
            return true;
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean deleteProfileImage(Long id, String companyName) throws IOException {
        Employees employees = employeesRepository.findById(id, companyName).orElseThrow(() -> new IOException("No employees found"));
        String fileName = employees.getName().replace(" ", "");
        File controlFile = new File(pathProfileImages);
        if (!controlFile.exists()) throw new IOException("Folder doesn't exist");
        Set<String> validExtensions = Set.of("png", "jpeg", "jpg");
        for(String extension : validExtensions) {
            String profileImageName = fileName + "." + extension;
            File destinationDirectory = new File(pathProfileImages, profileImageName);
            if(destinationDirectory.exists()) {
                return destinationDirectory.delete();
            }
        }
        return false;
    }


    public byte[] getProfileImage(Long id, String companyName) throws IOException {
        Employees employees = employeesRepository.findById(id, companyName).orElseThrow(() -> new IOException("No Employee found"));

        String filename = employees.getName().replace(" ", "");
        String path = pathProfileImages + "\\" + filename + ".png";

        File file = new File(path);
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                return IOUtils.toByteArray(in);
            }
        } else {
            throw new IOException("File does not exist at path: " + path);
        }
    }

    public List<EmployeesResponseDTO> getAllEmployees(String companyName) {
        List<Employees> employees = employeesRepository.findAll(companyName);
        List<EmployeesResponseDTO> responseDTO = new ArrayList<>();
        for (Employees employees1 : employees) {
            EmployeesResponseDTO response = modelMapper.map(employees1, EmployeesResponseDTO.class);
            response.setHiringDate(employees1.getHiringDate());
            response.setAddress(employees1.getAddress());
            responseDTO.add(response);
        }
        return responseDTO;
    }

    public EmployeesResponseDTO getById(Long id, String companyName) throws EmployeesException {
        Employees employees = employeesRepository.findById(id, companyName).orElseThrow(() -> new EmployeesException("No employees retrieved with id: " + id, 400));
        return modelMapper.map(employees, EmployeesResponseDTO.class);
    }

    public EmployeesResponseDTO getByFiscalCode(String fiscalCode, String companyName) throws EmployeesException {
        Employees employees = employeesRepository.findByFiscalCode(fiscalCode, companyName).orElseThrow(() -> new EmployeesException("No employees retrieved with Fiscal Code: " + fiscalCode, 400));

        return modelMapper.map(employees, EmployeesResponseDTO.class);
    }

    public EmployeesResponseDTO updateById(Long id, EmployeesRequestDTO employeesRequestDTO, String companyName) throws EmployeesException, HiringException {
        if (employeesValidator.isEmployeeValid(employeesRequestDTO, companyName)) {
            Employees employees = employeesRepository.findById(id).orElseThrow(() -> new EmployeesException("The inserted employee's information's are not valid", 400));

            employees.setAddress(employeesRequestDTO.getAddress());
            employees.setName(employeesRequestDTO.getName());
            employees.setFiscalCode(employeesRequestDTO.getFiscalCode());

            employeesRepository.saveAndFlush(employees);
            return modelMapper.map(employees, EmployeesResponseDTO.class);
        }
        throw new EmployeesException("The inserted employee's information's aree not valid", 400);
    }

    public EmployeesResponseDTO deleteById(Long id, String companyName) throws EmployeesException {
        Employees employees = employeesRepository.findById(id, companyName).orElseThrow(() -> new EmployeesException("No employees retrieved with id: " + id, 400));
        employees.setDeleted(true);
        employeesRepository.saveAndFlush(employees);
        return modelMapper.map(employees, EmployeesResponseDTO.class);
    }

    public SituationResponseDTO getSituationByName(String name, String companyName) throws EmployeesException {

        Employees employees = employeesRepository.findByName(name, companyName).orElseThrow(() -> new EmployeesException("No employees retrieved with name : " + name, 400));
        List<SickDays> sickDays = sickDaysRepository.findByName(name, companyName);
        List<Permits> permits = permitsRepository.findByName(name, companyName);
        List<Overtime> overtime = overtimeRepository.findByName(name, companyName);
        List<SickDaysResponseDTO> sickDaysResponseDTOList = new ArrayList<>();
        List<PermitsResponseDTO> permitsResponseDTOList = new ArrayList<>();
        List<OvertimeResponseDTO> overtimeResponseDTOList = new ArrayList<>();

        for (SickDays days : sickDays) {
            SickDaysResponseDTO sickDaysResponseDTO = modelMapper.map(days, SickDaysResponseDTO.class);
            sickDaysResponseDTOList.add(sickDaysResponseDTO);
        }
        for (Permits perm : permits) {
            PermitsResponseDTO permitsResponseDTO = modelMapper.map(perm, PermitsResponseDTO.class);
            permitsResponseDTOList.add(permitsResponseDTO);
        }
        for (Overtime over : overtime) {
            OvertimeResponseDTO overtimeResponseDTO = modelMapper.map(over, OvertimeResponseDTO.class);
            overtimeResponseDTOList.add(overtimeResponseDTO);
        }


        SituationResponseDTO situationResponseDTO = new SituationResponseDTO();
        situationResponseDTO.setEmployees(employees);
        situationResponseDTO.setSickDays(sickDaysResponseDTOList);
        situationResponseDTO.setPermits(permitsResponseDTOList);
        situationResponseDTO.setOvertime(overtimeResponseDTOList);

        return situationResponseDTO;
    }
}
