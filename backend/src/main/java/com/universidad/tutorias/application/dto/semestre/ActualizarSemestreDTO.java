package com.universidad.tutorias.application.dto.semestre;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarSemestreDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;
}
