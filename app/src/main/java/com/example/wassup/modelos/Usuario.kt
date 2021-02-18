package com.example.wassup.modelos

class Usuario {
    var id: String? = null
    var nombreUsuario: String? = null
    var imagenURL: String? = null
    var estado: String? = null

    constructor() {}
    constructor(id: String?, nombreUsuario: String?, imagenURL: String?, estado: String?) {
        this.id = id
        this.nombreUsuario = nombreUsuario
        this.imagenURL = imagenURL
        this.estado = estado
    }
}