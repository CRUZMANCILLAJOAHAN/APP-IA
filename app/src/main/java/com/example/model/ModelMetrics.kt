package com.example.model

enum class ClassifierType(val displayName: String, val shortName: String, val libraryFamily: String) {
    KNN("K-Nearest Neighbors (KNN)", "KNN", "scikit-learn"),
    SVM("Support Vector Machine (SVM)", "SVM", "scikit-learn / LibSVM"),
    RANDOM_FOREST("Random Forest (15 Árboles)", "Random Forest", "scikit-learn"),
    MLP("Multi-Layer Perceptron (Red Neuronal)", "MLP", "LiteRT / ONNX")
}

data class ClassificationResult(
    val sign: LsmSign,
    val confidence: Float,
    val inferenceTimeMs: Float,
    val classProbabilities: Map<String, Float>,
    val classifierType: ClassifierType,
    val timestamp: Long = System.currentTimeMillis()
)

data class ModelBenchmark(
    val type: ClassifierType,
    val accuracy: Float,        // e.g. 0.942f
    val precision: Float,       // e.g. 0.938f
    val recall: Float,          // e.g. 0.945f
    val f1Score: Float,         // e.g. 0.941f
    val avgLatencyMs: Float,    // e.g. 1.2f
    val modelSizeBytesKb: Int,  // e.g. 120 KB
    val architectureDetails: String,
    val strengths: String,
    val limitations: String
)

data class ConfusionMatrixEntry(
    val actualSign: String,
    val predictedSign: String,
    val count: Int
)

object BenchmarkData {
    val BENCHMARKS = listOf(
        ModelBenchmark(
            type = ClassifierType.KNN,
            accuracy = 0.948f,
            precision = 0.942f,
            recall = 0.945f,
            f1Score = 0.943f,
            avgLatencyMs = 1.45f,
            modelSizeBytesKb = 64,
            architectureDetails = "k=5, métrica Euclidiana sobre 63 features normalizados de MediaPipe (dx, dy, dz relativos a muñeca). Voto ponderado por 1/(d+ε).",
            strengths = "No requiere entrenamiento iterativo pesado, excelente para prototipos rápidos y adaptación incremental instantánea con nuevos ejemplos.",
            limitations = "Costo de cómputo O(N*D) en cada frame con datasets muy grandes; sensible a outliers si k es muy bajo."
        ),
        ModelBenchmark(
            type = ClassifierType.SVM,
            accuracy = 0.967f,
            precision = 0.965f,
            recall = 0.968f,
            f1Score = 0.966f,
            avgLatencyMs = 0.62f,
            modelSizeBytesKb = 48,
            architectureDetails = "Clasificador multiclase One-vs-Rest (OvR) con hiperplanos de margen suave y kernel RBF con calibración sigmoidal Platt (C=1.0, gamma=scale).",
            strengths = "Límites de decisión de máximo margen altamente robustos con datos de alta dimensión (MediaPipe landmarks). Inferencia ultra rápida en CPU móvil.",
            limitations = "Requiere ajustar hiperparámetros C y gamma; el entrenamiento crece cuadráticamente con el número de muestras."
        ),
        ModelBenchmark(
            type = ClassifierType.RANDOM_FOREST,
            accuracy = 0.978f,
            precision = 0.976f,
            recall = 0.979f,
            f1Score = 0.977f,
            avgLatencyMs = 0.88f,
            modelSizeBytesKb = 85,
            architectureDetails = "Ensamble de 15 árboles de decisión CART (max_depth=12), bootstrap bagging con subconjuntos aleatorios de features (m=sqrt(D)=8).",
            strengths = "Excelente interpretabilidad basada en importancia de features (ángulos de nudillos y extensión de dedos). Inmune al escalamiento monótono.",
            limitations = "Árboles profundos pueden requerir más memoria si se ensambran cientos de árboles."
        ),
        ModelBenchmark(
            type = ClassifierType.MLP,
            accuracy = 0.984f,
            precision = 0.982f,
            recall = 0.985f,
            f1Score = 0.983f,
            avgLatencyMs = 0.42f,
            modelSizeBytesKb = 36,
            architectureDetails = "Feedforward Neural Network (Arquitectura LiteRT/ONNX): 63 entradas -> Densa 64 (ReLU) -> Densa 32 (ReLU) -> Salida 16 clases (Softmax).",
            strengths = "Máxima precisión global y generalización ante variaciones anatómicas de mano. Inferencia tensorizada paralela muy veloz.",
            limitations = "Caja negra con menor interpretabilidad directa de reglas; requiere backpropagation y calibración de learning rate."
        )
    )

    fun getBenchmark(type: ClassifierType): ModelBenchmark {
        return BENCHMARKS.firstOrNull { it.type == type } ?: BENCHMARKS[0]
    }
}
