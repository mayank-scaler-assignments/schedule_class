package com.example.scaler.models;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
public class Batch extends BaseModel{

    private String name;
    private Schedule schedule;

}
