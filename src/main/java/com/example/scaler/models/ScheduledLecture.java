package com.example.scaler.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
public class ScheduledLecture extends BaseModel{

    @ManyToOne
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;
    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;
    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;
    private Date lectureStartTime;
    private Date lectureEndTime;
    private String lectureLink;
}
