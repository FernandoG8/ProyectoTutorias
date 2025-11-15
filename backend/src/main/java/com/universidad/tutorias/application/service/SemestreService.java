package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.semestre.ActualizarSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.CrearSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.EstadisticasSemestreDTO;
import com.universidad.tutorias.domain.entity.Semestre;

import java.util.List;
import java.util.Optional;

public interface SemestreService {

    // CRUD básico
    Semestre crearSemestre(CrearSemestreDTO dto);

    Semestre actualizarSemestre(Long id, ActualizarSemestreDTO dto);

    void eliminarSemestre(Long id);

    // Consultas
    Semestre obtenerPorId(Long id);

    Semestre obtenerPorCodigo(String codigo);

    Optional<Semestre> obtenerSemestreActivo();

    List<Semestre> listarTodos();

    List<Semestre> listarUltimos(int cantidad);

    // Activación
    void activarSemestre(Long id);

    void desactivarSemestre(Long id);

    // Validaciones
    boolean existeCodigo(String codigo);

    void validarFormatoCodigo(String codigo);

    void validarSemestreParaEliminacion(Long id);

    // Estadísticas
    EstadisticasSemestreDTO obtenerEstadisticas(Long id);

    // Conversión legacy (para compatibilidad con código existente)
    Long convertirCodigoAId(String codigoLegacy);

    String convertirIdACodigo(Long id);
}
