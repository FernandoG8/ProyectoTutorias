package com.universidad.tutorias.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarProcesoRequest {

    @NotNull(message = "El archivo es obligatorio")
    private MultipartFile archivo;

    @NotBlank(message = "El semestre académico es obligatorio")
    @Pattern(regexp = "^\\d{4}-\\d{4}-F[12]$", message = "Formato inválido. Use: YYYY-F1 o YYYY-F2")
    private String semestreAcademico;

    @NotBlank(message = "El usuario es obligatorio")
    private String usuario;
}