package com.example.model

/**
 * Definition of an LSM (Lengua de Señas Mexicana) sign in the vocabulary.
 */
data class LsmSign(
    val id: String,
    val displayName: String,
    val category: String, // "Letras y Vocales" or "Palabras Clave"
    val instructions: String,
    val spokenText: String,
    val iconEmoji: String,
    val fingerStateDescription: String
) {
    companion object {
        val VOCABULARY = listOf(
            LsmSign(
                id = "A",
                displayName = "A",
                category = "Letras y Vocales",
                instructions = "Puño cerrado con los cuatro dedos doblados sobre la palma y el pulgar extendido y pegado verticalmente al costado del dedo índice.",
                spokenText = "Letra A",
                iconEmoji = "✊",
                fingerStateDescription = "Pulgar extendido al costado, 4 dedos cerrados"
            ),
            LsmSign(
                id = "B",
                displayName = "B",
                category = "Letras y Vocales",
                instructions = "Cuatro dedos (índice, medio, anular y meñique) estirados juntos hacia arriba; el pulgar doblado cruzando sobre la palma de la mano.",
                spokenText = "Letra B",
                iconEmoji = "✋",
                fingerStateDescription = "4 dedos estirados juntos, pulgar doblado en palma"
            ),
            LsmSign(
                id = "C",
                displayName = "C",
                category = "Letras y Vocales",
                instructions = "Mano curvada de perfil en forma de la letra 'C', con los dedos juntos arqueados y el pulgar opuesto.",
                spokenText = "Letra C",
                iconEmoji = "🤏",
                fingerStateDescription = "Todos los dedos arqueados formando una C"
            ),
            LsmSign(
                id = "D",
                displayName = "D",
                category = "Letras y Vocales",
                instructions = "Dedo índice recto apuntando hacia arriba; el pulgar, medio, anular y meñique se tocan por las puntas formando un círculo.",
                spokenText = "Letra D",
                iconEmoji = "☝️",
                fingerStateDescription = "Índice arriba, otros dedos tocan el pulgar formando círculo"
            ),
            LsmSign(
                id = "E",
                displayName = "E",
                category = "Letras y Vocales",
                instructions = "Dedos curvados hacia abajo tocando el pulgar que está horizontalmente doblado contra la palma.",
                spokenText = "Letra E",
                iconEmoji = "✊",
                fingerStateDescription = "Dedos flexionados tocando el borde del pulgar"
            ),
            LsmSign(
                id = "I",
                displayName = "I",
                category = "Letras y Vocales",
                instructions = "Meñique totalmente estirado hacia arriba; los demás tres dedos y el pulgar forman un puño cerrado.",
                spokenText = "Letra I",
                iconEmoji = "🤙",
                fingerStateDescription = "Meñique arriba, resto de dedos en puño"
            ),
            LsmSign(
                id = "L",
                displayName = "L",
                category = "Letras y Vocales",
                instructions = "Dedo índice vertical y pulgar horizontal formando un ángulo recto de 90° (forma de L); otros tres dedos cerrados.",
                spokenText = "Letra L",
                iconEmoji = "👆",
                fingerStateDescription = "Índice y pulgar en ángulo de 90 grados"
            ),
            LsmSign(
                id = "O",
                displayName = "O",
                category = "Letras y Vocales",
                instructions = "Todos los dedos y el pulgar se curvan hacia adentro tocándose por las yemas para formar un círculo cerrado u óvalo.",
                spokenText = "Letra O",
                iconEmoji = "👌",
                fingerStateDescription = "Dedos y pulgar unidos formando una O completa"
            ),
            LsmSign(
                id = "U",
                displayName = "U",
                category = "Letras y Vocales",
                instructions = "Dedos índice y medio estirados juntos hacia arriba; pulgar dobla y sostiene los dedos anular y meñique.",
                spokenText = "Letra U",
                iconEmoji = "✌️",
                fingerStateDescription = "Índice y medio juntos hacia arriba"
            ),
            LsmSign(
                id = "V",
                displayName = "V",
                category = "Letras y Vocales",
                instructions = "Dedos índice y medio separados en ángulo formando la letra 'V' de victoria; los demás doblados contra la palma.",
                spokenText = "Letra V",
                iconEmoji = "✌️",
                fingerStateDescription = "Índice y medio abiertos en V"
            ),
            LsmSign(
                id = "W",
                displayName = "W",
                category = "Letras y Vocales",
                instructions = "Tres dedos estirados hacia arriba y separados: índice, medio y anular. El pulgar sujeta el meñique contra la palma.",
                spokenText = "Letra W",
                iconEmoji = "🖖",
                fingerStateDescription = "Índice, medio y anular levantados formando W"
            ),
            LsmSign(
                id = "Y",
                displayName = "Y",
                category = "Letras y Vocales",
                instructions = "Pulgar y meñique totalmente estirados en extremos opuestos; los tres dedos centrales bien cerrados sobre la palma.",
                spokenText = "Letra Y",
                iconEmoji = "🤙",
                fingerStateDescription = "Pulgar y meñique estirados, centrales cerrados"
            ),
            LsmSign(
                id = "HOLA",
                displayName = "Hola",
                category = "Palabras Clave",
                instructions = "Mano abierta con palma al frente o configuración en 'B' cerca de la sien derecha, realizando un gesto de saludo hacia afuera.",
                spokenText = "Hola, mucho gusto",
                iconEmoji = "👋",
                fingerStateDescription = "Palma abierta en saludo formal LSM"
            ),
            LsmSign(
                id = "GRACIAS",
                displayName = "Gracias",
                category = "Palabras Clave",
                instructions = "Mano abierta con el dedo medio ligeramente flexionado hacia adelante; se toca la barbilla/pecho y se proyecta amablemente hacia el frente.",
                spokenText = "Muchas gracias",
                iconEmoji = "🙏",
                fingerStateDescription = "Mano abierta con dedo medio extendido hacia el receptor"
            ),
            LsmSign(
                id = "TE_QUIERO",
                displayName = "Te quiero",
                category = "Palabras Clave",
                instructions = "Seña universal ILY: Pulgar, índice y meñique levantados; dedo medio y anular doblados tocando la palma.",
                spokenText = "Te quiero mucho",
                iconEmoji = "🤟",
                fingerStateDescription = "Pulgar, índice y meñique estirados (ILY LSM)"
            ),
            LsmSign(
                id = "AYUDA",
                displayName = "Ayuda",
                category = "Palabras Clave",
                instructions = "Mano con puño cerrado y pulgar hacia arriba apoyada sobre la palma plana horizontal de la otra mano elevándose suavemente.",
                spokenText = "Necesito ayuda, por favor",
                iconEmoji = "🆘",
                fingerStateDescription = "Puño con pulgar arriba (signo de auxilio)"
            )
        )

        fun findById(id: String): LsmSign? = VOCABULARY.firstOrNull { it.id == id }

        val SIGN_IDS = VOCABULARY.map { it.id }
    }
}
