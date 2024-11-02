package com.example.flow


sealed class DataState {
    object Loading : DataState()
    data class Loaded(val movies: List<String>?) : DataState()
    data class Error(val message: String) : DataState()
}
