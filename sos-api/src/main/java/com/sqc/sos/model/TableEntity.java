package com.sqc.sos.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableEntity {
    @Id
    @GeneratedValue
    UUID id;

    String name;
    String area;
    Integer capacity;
    Boolean isAvailable;

    Long areaId;
    Integer posX;
    Integer posY;

    @Enumerated(EnumType.STRING)
    TableStatus tableStatus;
}
