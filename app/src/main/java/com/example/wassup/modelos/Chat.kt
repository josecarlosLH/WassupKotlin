package com.example.wassup.modelos

class Chat {
    var emisor: String? = null
    var receptor: String? = null
    var mensaje: String? = null
    var isEsLeido = false

    constructor(emisor: String?, receptor: String?, mensaje: String?, esLeido: Boolean) {
        this.emisor = emisor
        this.receptor = receptor
        this.mensaje = mensaje
        isEsLeido = esLeido
    }

    constructor() {}
}