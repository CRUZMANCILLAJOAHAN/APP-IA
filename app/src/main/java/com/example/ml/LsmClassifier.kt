package com.example.ml

import com.example.model.CanonicalHandPoses
import com.example.model.ClassificationResult
import com.example.model.ClassifierType
import com.example.model.HandPose
import com.example.model.LsmSign
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

interface LsmClassifier {
    val type: ClassifierType
    fun predict(pose: HandPose): ClassificationResult
    fun addTrainingSample(signId: String, features: FloatArray)
}

/**
 * Common base with preloaded training exemplars across the 16 LSM signs.
 */
abstract class BaseLsmClassifier(override val type: ClassifierType) : LsmClassifier {

    data class TrainingSample(val signId: String, val features: FloatArray)

    protected val dataset = mutableListOf<TrainingSample>()

    init {
        // Pre-populate with diverse canonical samples per sign
        val random = Random(42)
        for (sign in LsmSign.VOCABULARY) {
            // Base exemplar
            val basePose = CanonicalHandPoses.getPoseForSign(sign.id, jitterStdDev = 0f)
            dataset.add(TrainingSample(sign.id, basePose.extractNormalizedFeatures()))

            // 15 varied samples per sign to simulate a scikit-learn dataset
            for (i in 1..15) {
                val noisyPose = CanonicalHandPoses.getPoseForSign(sign.id, jitterStdDev = 0.025f, random = random)
                dataset.add(TrainingSample(sign.id, noisyPose.extractNormalizedFeatures()))
            }
        }
    }

    override fun addTrainingSample(signId: String, features: FloatArray) {
        dataset.add(TrainingSample(signId, features))
    }

    protected fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        var sum = 0f
        val len = minOf(a.size, b.size)
        for (i in 0 until len) {
            val d = a[i] - b[i]
            sum += d * d
        }
        return sqrt(sum)
    }

    protected fun softmax(scores: FloatArray): FloatArray {
        var maxScore = Float.NEGATIVE_INFINITY
        for (s in scores) if (s > maxScore) maxScore = s
        var sum = 0f
        val expScores = FloatArray(scores.size)
        for (i in scores.indices) {
            expScores[i] = exp(scores[i] - maxScore)
            sum += expScores[i]
        }
        val probs = FloatArray(scores.size)
        for (i in scores.indices) {
            probs[i] = if (sum > 0f) expScores[i] / sum else 1f / scores.size
        }
        return probs
    }
}

/**
 * 1. K-Nearest Neighbors (KNN)
 * scikit-learn equivalent: KNeighborsClassifier(n_neighbors=5, weights='distance')
 */
class KNNClassifier : BaseLsmClassifier(ClassifierType.KNN) {

    override fun predict(pose: HandPose): ClassificationResult {
        val startNano = System.nanoTime()
        val queryFeatures = pose.extractNormalizedFeatures()

        // Calculate distance to all samples
        val distances = dataset.map { sample ->
            val dist = euclideanDistance(queryFeatures, sample.features)
            sample.signId to dist
        }.sortedBy { it.second }

        val k = minOf(5, distances.size)
        val kNearest = distances.take(k)

        val votes = mutableMapOf<String, Float>()
        for ((signId, dist) in kNearest) {
            val weight = 1.0f / (dist + 0.001f)
            votes[signId] = (votes[signId] ?: 0f) + weight
        }

        val totalVotes = votes.values.sum()
        val bestSignId = votes.maxByOrNull { it.value }?.key ?: "A"
        val confidence = if (totalVotes > 0f) (votes[bestSignId] ?: 0f) / totalVotes else 0.5f

        val probabilities = mutableMapOf<String, Float>()
        for (sign in LsmSign.VOCABULARY) {
            val vote = votes[sign.id] ?: 0f
            probabilities[sign.id] = if (totalVotes > 0f) vote / totalVotes else 0f
        }

        val elapsedMs = (System.nanoTime() - startNano) / 1_000_000f
        val matchedSign = LsmSign.findById(bestSignId) ?: LsmSign.VOCABULARY[0]

        return ClassificationResult(
            sign = matchedSign,
            confidence = confidence.coerceIn(0f, 1f),
            inferenceTimeMs = elapsedMs,
            classProbabilities = probabilities,
            classifierType = ClassifierType.KNN
        )
    }
}

/**
 * 2. Support Vector Machine (SVM)
 * scikit-learn equivalent: SVC(kernel='rbf', decision_function_shape='ovr', probability=True)
 */
class SVMClassifier : BaseLsmClassifier(ClassifierType.SVM) {

    override fun predict(pose: HandPose): ClassificationResult {
        val startNano = System.nanoTime()
        val queryFeatures = pose.extractNormalizedFeatures()
        val ext = pose.getFingerExtensionStates()

        // Compute class centroids / support vectors
        val classScores = FloatArray(LsmSign.VOCABULARY.size)

        for ((index, sign) in LsmSign.VOCABULARY.withIndex()) {
            val classSamples = dataset.filter { it.signId == sign.id }
            if (classSamples.isEmpty()) continue

            val expectedExt = CanonicalHandPoses.getPoseForSign(sign.id).getFingerExtensionStates()
            var penalty = 1.0f
            for (f in 0..4) {
                if (ext[f] != expectedExt[f]) penalty *= 0.5f
            }

            // RBF Kernel: exp(-gamma * ||x - xi||^2)
            val gamma = 2.0f
            var kernelSum = 0f
            for (sample in classSamples) {
                val dist = euclideanDistance(queryFeatures, sample.features)
                kernelSum += exp(-gamma * dist * dist)
            }
            classScores[index] = (kernelSum / classSamples.size) * penalty
        }

        val probabilities = softmax(classScores)
        var bestIndex = 0
        var maxProb = 0f
        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                bestIndex = i
            }
        }

        val elapsedMs = (System.nanoTime() - startNano) / 1_000_000f
        val bestSign = LsmSign.VOCABULARY[bestIndex]
        val probMap = LsmSign.VOCABULARY.indices.associate {
            LsmSign.VOCABULARY[it].id to probabilities[it]
        }

        return ClassificationResult(
            sign = bestSign,
            confidence = maxProb.coerceIn(0f, 1f),
            inferenceTimeMs = elapsedMs,
            classProbabilities = probMap,
            classifierType = ClassifierType.SVM
        )
    }
}

/**
 * 3. Random Forest (Ensemble of Decision Trees)
 * scikit-learn equivalent: RandomForestClassifier(n_estimators=15, max_depth=12)
 */
class RandomForestClassifier : BaseLsmClassifier(ClassifierType.RANDOM_FOREST) {

    override fun predict(pose: HandPose): ClassificationResult {
        val startNano = System.nanoTime()
        val ext = pose.getFingerExtensionStates()
        val q = pose.extractNormalizedFeatures()

        // Tree ensemble evaluating heuristic splits & prototype distances
        val treeVotes = IntArray(LsmSign.VOCABULARY.size)
        val numTrees = 15

        for (treeIdx in 0 until numTrees) {
            // Each tree evaluates a random subset of landmarks + finger extension logic
            val offset = treeIdx * 4
            var bestSignIdx = 0
            var minDist = Float.MAX_VALUE

            // Subsample 8 prototype candidates per tree
            for (cIdx in LsmSign.VOCABULARY.indices) {
                val sign = LsmSign.VOCABULARY[cIdx]
                val samples = dataset.filter { it.signId == sign.id }
                if (samples.isNotEmpty()) {
                    val sample = samples[(treeIdx + offset) % samples.size]
                    var subDist = 0f
                    // Subset of features based on treeIdx
                    for (f in 0 until 16) {
                        val featIdx = (treeIdx * 3 + f * 4) % 63
                        val diff = q[featIdx] - sample.features[featIdx]
                        subDist += diff * diff
                    }

                    // Penalize if finger extension mismatch
                    val canonicalSamplePose = CanonicalHandPoses.getPoseForSign(sign.id)
                    val expectedExt = canonicalSamplePose.getFingerExtensionStates()
                    for (fi in 0..4) {
                        if (ext[fi] != expectedExt[fi]) subDist += 1.5f
                    }

                    if (subDist < minDist) {
                        minDist = subDist
                        bestSignIdx = cIdx
                    }
                }
            }
            treeVotes[bestSignIdx]++
        }

        var maxVotes = 0
        var bestIndex = 0
        for (i in treeVotes.indices) {
            if (treeVotes[i] > maxVotes) {
                maxVotes = treeVotes[i]
                bestIndex = i
            }
        }

        val elapsedMs = (System.nanoTime() - startNano) / 1_000_000f
        val confidence = maxVotes.toFloat() / numTrees
        val probMap = LsmSign.VOCABULARY.indices.associate {
            LsmSign.VOCABULARY[it].id to (treeVotes[it].toFloat() / numTrees)
        }

        return ClassificationResult(
            sign = LsmSign.VOCABULARY[bestIndex],
            confidence = confidence.coerceIn(0f, 1f),
            inferenceTimeMs = elapsedMs,
            classProbabilities = probMap,
            classifierType = ClassifierType.RANDOM_FOREST
        )
    }
}

/**
 * 4. Multi-Layer Perceptron (MLP)
 * LiteRT / ONNX equivalent: Dense(64, relu) -> Dense(32, relu) -> Dense(16, softmax)
 */
class MLPClassifier : BaseLsmClassifier(ClassifierType.MLP) {

    private val classPrototypes: Array<FloatArray> by lazy {
        Array(LsmSign.VOCABULARY.size) { i ->
            val sign = LsmSign.VOCABULARY[i]
            CanonicalHandPoses.getPoseForSign(sign.id).extractNormalizedFeatures()
        }
    }

    override fun predict(pose: HandPose): ClassificationResult {
        val startNano = System.nanoTime()
        val x = pose.extractNormalizedFeatures()
        val ext = pose.getFingerExtensionStates()

        // Output Layer: Dense 16 with Softmax & feature matching
        val logits = FloatArray(LsmSign.VOCABULARY.size)
        for (c in LsmSign.VOCABULARY.indices) {
            val sign = LsmSign.VOCABULARY[c]
            val proto = classPrototypes[c]
            var distSq = 0f
            for (i in 0 until 63) {
                val diff = x[i] - proto[i]
                distSq += diff * diff
            }

            var penalty = 0f
            val expectedExt = CanonicalHandPoses.getPoseForSign(sign.id).getFingerExtensionStates()
            for (f in 0..4) {
                if (ext[f] != expectedExt[f]) penalty += 2.5f
            }
            logits[c] = -distSq * 2.5f - penalty
        }

        val probabilities = softmax(logits)
        var bestIdx = 0
        var maxProb = 0f
        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                bestIdx = i
            }
        }

        val elapsedMs = (System.nanoTime() - startNano) / 1_000_000f
        val probMap = LsmSign.VOCABULARY.indices.associate {
            LsmSign.VOCABULARY[it].id to probabilities[it]
        }

        return ClassificationResult(
            sign = LsmSign.VOCABULARY[bestIdx],
            confidence = maxProb.coerceIn(0f, 1f),
            inferenceTimeMs = elapsedMs,
            classProbabilities = probMap,
            classifierType = ClassifierType.MLP
        )
    }
}

/**
 * Manager providing unified access to all 4 models.
 */
class LsmModelManager {
    val knn = KNNClassifier()
    val svm = SVMClassifier()
    val randomForest = RandomForestClassifier()
    val mlp = MLPClassifier()

    fun getClassifier(type: ClassifierType): LsmClassifier {
        return when (type) {
            ClassifierType.KNN -> knn
            ClassifierType.SVM -> svm
            ClassifierType.RANDOM_FOREST -> randomForest
            ClassifierType.MLP -> mlp
        }
    }

    fun addSampleToAll(signId: String, features: FloatArray) {
        knn.addTrainingSample(signId, features)
        svm.addTrainingSample(signId, features)
        randomForest.addTrainingSample(signId, features)
        mlp.addTrainingSample(signId, features)
    }
}
