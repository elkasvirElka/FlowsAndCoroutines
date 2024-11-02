package com.example.flow

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis

class CoroutinesViewModel: ViewModel() {

    // Basic launch in the global scope (not recommended for production)
    fun globalScopeLaunch() {
        GlobalScope.launch {
            println("Running in GlobalScope with launch")
        }
    }

    // Launch within a custom coroutine scope
    fun customScopeLaunch() = CoroutineScope(Dispatchers.IO).launch {
        println("Running in a custom CoroutineScope with launch on IO dispatcher")
    }

    // Launch a coroutine with an explicit context
    suspend fun launchWithContext() {
        withContext(Dispatchers.Default) {
            println("Running with withContext on Default dispatcher")
        }
    }

    // Launch within the viewModelScope (for ViewModels)
    fun viewModelScopeLaunch(viewModelScope: CoroutineScope) {
        viewModelScope.launch {
            println("Running in viewModelScope")
        }
    }

    // Launch in lifecycleScope (for Activities or Fragments)
    // Uncomment below if using in an Android component with lifecycleScope
    // fun lifecycleScopeLaunch(lifecycleScope: CoroutineScope) {
    //     lifecycleScope.launch {
    //         println("Running in lifecycleScope")
    //     }
    // }

    // Async coroutine builder example
    suspend fun asyncExample(): Int {
        return CoroutineScope(Dispatchers.Default).async {
            delay(500)
            42
        }.await() // Waits for result of async
    }

    // Run multiple async coroutines concurrently
    suspend fun concurrentAsyncExample(): Int {
        return coroutineScope {
            val deferredOne = async { delay(300); 20 }
            val deferredTwo = async { delay(300); 22 }
            deferredOne.await() + deferredTwo.await()
        }
    }

    // RunBlocking (only recommended for testing or blocking main thread operations)
    fun runBlockingExample() = runBlocking {
        println("Running runBlocking on the main thread")
    }

    // Lazy start of a coroutine
    fun lazyCoroutine() {
        val job = CoroutineScope(Dispatchers.Default).launch(start = CoroutineStart.LAZY) {
            println("Lazy coroutine is now active")
        }
        job.start() // Starts the lazy coroutine
    }

    // Suspending function can be called from a coroutine
    suspend fun suspendingFunction() {
        delay(1000)
        println("Suspending function complete")
    }

    // Example with coroutine timing (using measureTimeMillis)
    suspend fun timedCoroutines() {
        val time = measureTimeMillis {
            coroutineScope {
                launch { delay(500) }
                launch { delay(500) }
            }
        }
        println("Timed coroutine took $time ms")
    }

    fun cancelCoroutine() {
        cancelableCoroutine().cancel()
    }

    // Cancel a coroutine
    fun cancelableCoroutine() = CoroutineScope(Dispatchers.Default).launch {
        try {
            repeat(5) { i ->
                println("Coroutine work $i")
                delay(500)
            }
        } catch (e: CancellationException) {
            println("Coroutine was cancelled")
        }
    }

    /**
     * Purpose: Used to handle errors independently in child coroutines. In a SupervisorScope,
     * if one child coroutine fails, it does not cancel the other coroutines in the scope.
     */
    suspend fun supervisorScopeExample() = supervisorScope {
        val job1 = launch {
            // Task 1
        }
        val job2 = launch {
            throw Exception("Error in Task 2") // Won't cancel job1
        }
    }
}
