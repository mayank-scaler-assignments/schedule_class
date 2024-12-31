package com.example.scaler.services;

import com.example.scaler.exceptions.InvalidBatchException;
import com.example.scaler.exceptions.InvalidInstructorException;
import com.example.scaler.exceptions.InvalidLectureException;
import com.example.scaler.models.*;
import com.example.scaler.repositories.BatchRepository;
import com.example.scaler.repositories.InstructorRepository;
import com.example.scaler.repositories.LectureRepository;
import com.example.scaler.repositories.ScheduledLectureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final InstructorRepository instructorRepository;
    private final BatchRepository batchRepository;
    private final ScheduledLectureRepository scheduledLectureRepository;

    @Override
    public List<ScheduledLecture> scheduleLectures(List<Long> lectureIds, Long instructorId, Long batchId) throws InvalidLectureException, InvalidInstructorException, InvalidBatchException {
        // Step 1: Validate and retrieve entities
        List<Lecture> lectures = lectureRepository.findAllById(lectureIds);
        if (CollectionUtils.isEmpty(lectures) || lectures.size() != lectureIds.size()) {
            throw new InvalidLectureException("Invalid Lecture Ids");
        }

        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new InvalidInstructorException("Invalid Instructor Id"));

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new InvalidBatchException("Invalid Batch Id"));

        // Step 2: Get the batch schedule and last lecture date
        Schedule batchSchedule = batch.getSchedule();
        LocalDateTime lastLectureDate = getLastLectureDate(batch);

        // Step 3: Calculate lecture dates
        List<LocalDateTime> lectureDates = calculateLectureDates(batchSchedule, lastLectureDate, lectures.size());

        // Step 4: Schedule lectures
        List<ScheduledLecture> scheduledLectures = new ArrayList<>();
        for (int i = 0; i < lectures.size(); i++) {
            ScheduledLecture scheduledLecture = new ScheduledLecture();
            scheduledLecture.setLecture(lectures.get(i));
            scheduledLecture.setBatch(batch);
            scheduledLecture.setInstructor(instructor);
            scheduledLecture.setLectureStartTime(java.sql.Timestamp.valueOf(lectureDates.get(i)));
            scheduledLecture.setLectureEndTime(java.sql.Timestamp.valueOf(lectureDates.get(i).plusMinutes(150))); // Fixed 2.5 hours
            scheduledLecture.setLectureLink(generateLectureLink());
            scheduledLectures.add(scheduledLecture);
        }

        // Step 5: Save and return scheduled lectures
        return scheduledLectureRepository.saveAll(scheduledLectures);
    }

    private LocalDateTime getLastLectureDate(Batch batch) {
        ScheduledLecture lastLecture = scheduledLectureRepository.findTopByBatchOrderByLectureStartTimeDesc(batch);
        return lastLecture != null ? lastLecture.getLectureStartTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime() : LocalDateTime.now();
    }

    private List<LocalDateTime> calculateLectureDates(Schedule schedule, LocalDateTime lastLectureDate, int numberOfLectures) {
        List<LocalDateTime> lectureDates = new ArrayList<>();
        DayOfWeek[] days = switch (schedule) {
            case MWF_MORNING -> new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY};
            case MWF_EVENING -> new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY};
            case TTS_MORNING -> new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY};
            case TTS_EVENING -> new DayOfWeek[]{DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY};
            default -> throw new IllegalArgumentException("Unknown schedule type");
        };

        LocalDateTime nextLectureDate = lastLectureDate.plusDays(1); // Start searching from the next day
        while (lectureDates.size() < numberOfLectures) {
            if (matchesSchedule(nextLectureDate, days)) {
                lectureDates.add(nextLectureDate.withHour(schedule.name().contains("MORNING") ? 7 : 21)
                        .withMinute(0).withSecond(0).withNano(0));
            }
            nextLectureDate = nextLectureDate.plusDays(1);
        }

        return lectureDates;
    }

    private boolean matchesSchedule(LocalDateTime date, DayOfWeek[] days) {
        for (DayOfWeek day : days) {
            if (date.getDayOfWeek() == day) {
                return true;
            }
        }
        return false;
    }

    private String generateLectureLink() {
        return "https://lecture.scaler.com/" + UUID.randomUUID();
    }
}
