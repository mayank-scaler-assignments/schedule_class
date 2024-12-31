package com.example.scaler.controllers;

import com.example.scaler.dtos.ResponseStatus;
import com.example.scaler.dtos.ScheduleLectureRequestDto;
import com.example.scaler.dtos.ScheduleLecturesResponseDto;
import com.example.scaler.exceptions.InvalidBatchException;
import com.example.scaler.exceptions.InvalidInstructorException;
import com.example.scaler.exceptions.InvalidLectureException;
import com.example.scaler.services.LectureService;
import org.springframework.stereotype.Controller;

@Controller
public class LectureController {


    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    public ScheduleLecturesResponseDto scheduleLectures(ScheduleLectureRequestDto requestDto) {
        ScheduleLecturesResponseDto scheduleLecturesResponseDto = new ScheduleLecturesResponseDto();
        try {
            scheduleLecturesResponseDto.setScheduledLectures(lectureService.scheduleLectures(
                    requestDto.getLectureIds(),
                    requestDto.getInstructorId(),
                    requestDto.getBatchId()));
            scheduleLecturesResponseDto.setResponseStatus(ResponseStatus.SUCCESS);
        } catch (InvalidLectureException | InvalidInstructorException | InvalidBatchException e) {
            scheduleLecturesResponseDto.setResponseStatus(ResponseStatus.FAILURE);
        }
        return scheduleLecturesResponseDto;
    }
}
