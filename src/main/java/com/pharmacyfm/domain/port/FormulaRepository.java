package com.pharmacyfm.domain.port;

import com.pharmacyfm.domain.model.Formula;

import java.util.List;

/**
 * Puerto de salida (Output Port) para la persistencia de fórmulas magistrales.
 *
 * Define el contrato que debe cumplir cualquier implementación de acceso a datos
 * de fórmulas, sin acoplarse a ninguna tecnología concreta (JDBC, JPA, REST…).
 *
 * La capa de dominio y servicio solo conoce esta interfaz; la implementación
 * JDBC reside en infrastructure.persistence y se conecta mediante inyección
 * de dependencias.
 *
 * Regla de dependencia (Clean Architecture):
 *   domain.port  ←  service  →  domain.port
 *   infrastructure.persistence  →  domain.port  (implementa)
 */
public interface FormulaRepository {

    /**
     * Recupera el catálogo completo de fórmulas, ordenado por nombre ascendente.
     *
     * @return Lista de fórmulas; nunca null, lista vacía si no hay datos.
     */
    List<Formula> findAll();

    /**
     * Persiste una nueva fórmula y devuelve el ID autogenerado.
     *
     * @param f Fórmula a insertar (f.isNew() debe ser true, es decir, id == 0).
     * @return ID asignado por la base de datos, o -1 si ocurrió un error.
     */
    int insert(Formula f);

    /**
     * Actualiza los datos de una fórmula existente.
     *
     * @param f Fórmula con datos actualizados (id > 0).
     * @return true si la fila fue modificada; false si el ID no existía.
     */
    boolean update(Formula f);

    /**
     * Elimina una fórmula del catálogo por su identificador.
     *
     * @param id ID de la fórmula a eliminar.
     * @return true si se eliminó; false si el ID no existía.
     */
    boolean delete(int id);
}
