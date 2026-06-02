package com.example.sclms_user.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "school")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}

