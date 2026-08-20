package com.pharmacyfm.domain.model;

/**
 * Modelo de dominio que representa un usuario autenticado en el sistema.
 *
 * Se modela como Java Record para garantizar inmutabilidad total:
 * una vez creado, ningún campo puede cambiar durante el ciclo de vida
 * de la sesión. El compilador genera automáticamente constructor,
 * getters canónicos (id(), email()…), equals, hashCode y toString.
 *
 * El campo 'role' utiliza el enum Role en lugar de un String libre,
 * lo que obliga a manejar los roles de forma tipada y segura.
 */
public record User(int id, String email, String nombre, String telefono, Role role) {}
