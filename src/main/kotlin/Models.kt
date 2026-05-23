package com.example

import kotlinx.serialization.Serializable

@Serializable
data class ComputeRequest(
    val task: String,
    val matrixA: List<List<Double>>,
    val matrixB: List<List<Double>>? = null
)

@Serializable
enum class ComputationStatus { PENDING, DONE, ERROR }

@Serializable
data class SubmitResponse(
    val id: String,
    val status: ComputationStatus
)

@Serializable
data class ResultResponse(
    val id: String,
    val status: ComputationStatus,
    val task: String? = null,
    val matrix: List<List<Double>>? = null,
    val scalar: Double? = null,
    val error: String? = null
)
