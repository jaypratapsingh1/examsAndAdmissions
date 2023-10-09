package com.tarento.upsmf.examsAndAdmissions.service;

import com.tarento.upsmf.examsAndAdmissions.model.ExamCenter;
import com.tarento.upsmf.examsAndAdmissions.model.ExamCycle;
import com.tarento.upsmf.examsAndAdmissions.model.Institute;
import com.tarento.upsmf.examsAndAdmissions.model.ResponseDto;
import com.tarento.upsmf.examsAndAdmissions.model.dto.CCTVStatusUpdateDTO;
import com.tarento.upsmf.examsAndAdmissions.model.dto.ExamCenterDTO;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCenterRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.ExamCycleRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.InstituteRepository;
import com.tarento.upsmf.examsAndAdmissions.repository.StudentExamRegistrationRepository;
import com.tarento.upsmf.examsAndAdmissions.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ExamCenterService {

    @Autowired
    private InstituteRepository instituteRepository;
    @Autowired
    private StudentExamRegistrationRepository studentExamRegistrationRepository;
    @Autowired
    private ExamCenterRepository examCenterRepository;
    @Autowired
    private ExamCycleRepository examCycleRepository;
    @Autowired
    private ExamCenterMapper examCenterMapper;

    public ResponseDto getVerifiedExamCentersInDistrict(String district) {
        ResponseDto response = new ResponseDto(Constants.API_GET_VERIFIED_EXAM_CENTERS);
        List<ExamCenter> examCenters = examCenterRepository.findByDistrictAndVerified(district, true);
        if (!examCenters.isEmpty()) {
            List<ExamCenterDTO> examCenterDTOs = examCenterMapper.toDTOs(examCenters);
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examCenterDTOs);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No verified exam centers found for the given district.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseDto assignAlternateExamCenter(Long originalExamCenterId, Long alternateInstituteId) {
        ResponseDto response = new ResponseDto(Constants.API_ASSIGN_ALTERNATE_EXAM_CENTER);
        ExamCenter originalCenter = examCenterRepository.findById(originalExamCenterId).orElse(null);
        Institute alternateInstitute = instituteRepository.findById(alternateInstituteId).orElse(null);

        if (originalCenter == null || alternateInstitute == null) {
            ResponseDto.setErrorResponse(response, "INVALID_INPUT", "Either the original exam center or the alternate institute was not found.", HttpStatus.NOT_FOUND);
            return response;
        }

        ExamCenter alternateCenter = convertInstituteToExamCenter(alternateInstitute, originalCenter.getExamCycle());
        examCenterRepository.save(alternateCenter);
        response.put(Constants.MESSAGE, "Alternate exam center assigned successfully.");
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    public ResponseDto updateCCTVStatus(Long examCenterId, CCTVStatusUpdateDTO updateDTO) {
        ResponseDto response = new ResponseDto(Constants.API_UPDATE_CCTV_STATUS);
        ExamCenter center = examCenterRepository.findById(examCenterId).orElse(null);
        if (center == null) {
            ResponseDto.setErrorResponse(response, "CENTER_NOT_FOUND", "Exam center not found.", HttpStatus.NOT_FOUND);
            return response;
        }

        center.setIpAddress(updateDTO.getIpAddress());
        center.setRemarks(updateDTO.getRemarks());
        center.setVerified(updateDTO.getStatus());
        examCenterRepository.save(center);
        response.put(Constants.MESSAGE, "CCTV status updated successfully.");
        response.setResponseCode(HttpStatus.OK);
        return response;
    }

    private ExamCenter convertInstituteToExamCenter(Institute institute, ExamCycle examCycle) {
        ExamCenter center = new ExamCenter();
        center.setExamCycle(examCycle);
        center.setInstitute(institute);
        center.setName(institute.getInstituteName());
        center.setAddress(institute.getAddress());
        center.setDistrict(institute.getDistrict());
        center.setVerified(true);
        return center;
    }

    public ResponseDto getExamCentersByStatus(Long examCycleId, Boolean isVerifiedStatus) {
        ResponseDto response = new ResponseDto(Constants.API_GET_EXAM_CENTERS_BY_STATUS);
        ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElse(null);
        if (examCycle != null) {
            List<ExamCenter> examCenters = examCenterRepository.findByExamCycleAndVerified(examCycle, isVerifiedStatus);
            if (!examCenters.isEmpty()) {
                response.put(Constants.MESSAGE, "Successful.");
                response.put(Constants.RESPONSE, examCenters);
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No exam centers found for the given criteria.", HttpStatus.NOT_FOUND);
            }
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "Exam cycle not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }
    public ResponseDto getAllExamCenters() {
        ResponseDto response = new ResponseDto(Constants.API_GET_ALL_EXAM_CENTERS);
        List<ExamCenter> examCenters = examCenterRepository.findAll();

        if (!examCenters.isEmpty()) {
            List<ExamCenterDTO> examCenterDTOs = examCenterMapper.toDTOs(examCenters);
            response.put(Constants.MESSAGE, "Successful.");
            response.put(Constants.RESPONSE, examCenterDTOs);
            response.setResponseCode(HttpStatus.OK);
        } else {
            ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No exam centers found.", HttpStatus.NOT_FOUND);
        }

        return response;
    }
    public ResponseDto getExamCentersByExamCycle(Long examCycleId) {
        ResponseDto response = new ResponseDto(Constants.API_GET_EXAM_CENTERS_BY_EXAM_CYCLE);
        ExamCycle examCycle = examCycleRepository.findById(examCycleId).orElse(null);
        if (examCycle != null) {
            List<ExamCenter> examCenters = examCenterRepository.findByExamCycle(examCycle);
            if (!examCenters.isEmpty()) {
                List<ExamCenterDTO> dtos = examCenterMapper.toDTOs(examCenters);
                response.put(Constants.MESSAGE, "Successful.");
                response.put(Constants.RESPONSE, dtos); // Put DTOs in the response
                response.setResponseCode(HttpStatus.OK);
            } else {
                ResponseDto.setErrorResponse(response, "NO_CENTERS_FOUND", "No exam centers found for the given exam cycle.", HttpStatus.NOT_FOUND);
            }
        } else {
            ResponseDto.setErrorResponse(response, "EXAM_CYCLE_NOT_FOUND", "Exam cycle not found.", HttpStatus.NOT_FOUND);
        }
        return response;
    }

}
