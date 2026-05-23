package com.example

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class ComputationEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val results = ConcurrentHashMap<String, ResultResponse>()

    fun submit(id: String, request: ComputeRequest) {
        results[id] = ResultResponse(id = id, status = ComputationStatus.PENDING)
        scope.launch {
            val response = try {
                withContext(Dispatchers.Default) { compute(id, request) }
            } catch (e: Exception) {
                ResultResponse(id = id, status = ComputationStatus.ERROR, task = request.task, error = e.message)
            }
            results[id] = response
        }
    }

    fun getResult(id: String): ResultResponse? = results[id]

    private fun compute(id: String, req: ComputeRequest): ResultResponse = when (req.task) {
        "matrix_multiply" -> {
            val b = req.matrixB ?: error("matrixB is required for matrix_multiply")
            ResultResponse(id = id, status = ComputationStatus.DONE, task = req.task, matrix = multiply(req.matrixA, b))
        }
        "matrix_transpose" ->
            ResultResponse(id = id, status = ComputationStatus.DONE, task = req.task, matrix = transpose(req.matrixA))
        "determinant" ->
            ResultResponse(id = id, status = ComputationStatus.DONE, task = req.task, scalar = determinant(req.matrixA))
        else -> error("Unknown task '${req.task}'. Supported: matrix_multiply, matrix_transpose, determinant")
    }

    private fun multiply(a: List<List<Double>>, b: List<List<Double>>): List<List<Double>> {
        val n = a.size; val m = a[0].size
        require(b.size == m) { "Dimension mismatch" }
        val k = b[0].size
        return List(n) { i -> List(k) { j -> (0 until m).sumOf { p -> a[i][p] * b[p][j] } } }
    }

    private fun transpose(a: List<List<Double>>): List<List<Double>> {
        val n = a.size; val m = a[0].size
        return List(m) { j -> List(n) { i -> a[i][j] } }
    }

    private fun determinant(matrix: List<List<Double>>): Double {
        val n = matrix.size
        require(matrix.all { it.size == n }) { "Matrix must be square" }
        if (n == 1) return matrix[0][0]
        val a = Array(n) { i -> DoubleArray(n) { j -> matrix[i][j] } }
        var sign = 1.0
        for (col in 0 until n) {
            val pivotRow = (col until n).maxByOrNull { kotlin.math.abs(a[it][col]) } ?: col
            if (kotlin.math.abs(a[pivotRow][col]) < 1e-12) return 0.0
            if (pivotRow != col) { val tmp = a[col]; a[col] = a[pivotRow]; a[pivotRow] = tmp; sign = -sign }
            for (row in col + 1 until n) {
                val f = a[row][col] / a[col][col]
                for (j in col until n) a[row][j] -= f * a[col][j]
            }
        }
        var det = sign
        for (i in 0 until n) det *= a[i][i]
        return det
    }
}
