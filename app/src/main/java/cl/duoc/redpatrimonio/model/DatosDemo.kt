package cl.duoc.redpatrimonio.model

import java.time.LocalDate

/** Nombres, direcciones, historias y horarios sintéticos para el trabajo académico. */
object DatosDemo {
    val recintos = listOf(
        Recinto(1, "Templo San Gabriel", "Iglesia", "Santiago", "Pasaje Patrimonial 125",
            "Recinto ficticio inspirado en las iglesias de barrio. Su nave y patio permiten estudiar la relación entre arquitectura, memoria y comunidad.",
            "Lunes a viernes: 09:00–18:00 · Sábado: 10:00–14:00", EstadoRecinto.ABIERTO),
        Recinto(2, "Capilla del Parque", "Capilla", "Ñuñoa", "Avenida de la Memoria 240",
            "Capilla ficticia de pequeña escala, vinculada a las celebraciones de su comunidad. Destaca por sus vitrales y jardín interior.",
            "Martes a domingo: 10:00–17:00", EstadoRecinto.EN_CELEBRACION, "La visita interior se retoma al finalizar la celebración."),
        Recinto(3, "Museo del Encuentro", "Museo", "Recoleta", "Calle del Encuentro 48",
            "Museo de ejemplo dedicado al arte sacro y los oficios tradicionales. Su colección simulada incluye textiles, tallas y documentos.",
            "Miércoles a domingo: 10:00–18:00", EstadoRecinto.ABIERTO),
        Recinto(4, "Templo Santa Elena", "Iglesia", "Providencia", "Avenida del Patrimonio 360",
            "Edificio ficticio con un espacio central destinado a conciertos y encuentros culturales. Su historia permite explorar el patrimonio vivo.",
            "Lunes a sábado: 09:00–19:00", EstadoRecinto.CERRADO, "Cierre temporal de ejemplo por conservación."),
        Recinto(5, "Archivo de la Comunidad", "Archivo", "San Miguel", "Pasaje de los Oficios 72",
            "Archivo ficticio que reúne testimonios de la vida barrial. Las visitas educativas presentan cómo se conservan fotografías y manuscritos.",
            "Lunes a viernes: 10:00–16:00", EstadoRecinto.ABIERTO),
        Recinto(6, "Casa Patrimonial del Sur", "Edificación", "La Florida", "Camino de la Historia 810",
            "Casa ficticia organizada alrededor de un patio. Sus salas recrean espacios de reunión y aprendizaje de la comunidad.",
            "Martes a sábado: 10:00–17:00", EstadoRecinto.ABIERTO)
    )

    fun eventos(): List<Evento> = listOf(
        Evento(1, 1, "Concierto de voces", LocalDate.now().plusDays(7).toString(), "18:00",
            "Encuentro coral de demostración. Entrada libre; datos ficticios."),
        Evento(2, 3, "Taller de conservación", LocalDate.now().plusDays(10).toString(), "11:00",
            "Actividad educativa de ejemplo sobre el cuidado de objetos patrimoniales."),
        Evento(3, 6, "Memorias del barrio", LocalDate.now().plusDays(14).toString(), "15:30",
            "Recorrido de ejemplo por relatos y espacios de la comunidad.")
    )

    val rutas = listOf(
        RutaPatrimonial(1, "Historias y arte sacro", listOf(1, 3, 5), sugerida = true),
        RutaPatrimonial(2, "Patrimonio en los barrios", listOf(2, 4, 6), sugerida = true)
    )
}
