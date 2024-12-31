package com.example.scaler.models;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class BaseModel {
    @Id
    @GeneratedValue(generator = "increment")
    private long id;
}
