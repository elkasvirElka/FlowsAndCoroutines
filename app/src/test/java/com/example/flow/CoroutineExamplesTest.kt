package com.example.flow

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration

class CoroutineExamplesTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var coroutineViewModel: CoroutinesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coroutineViewModel = CoroutinesViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test globalScopeLaunch`() = runTest {
        coroutineViewModel.globalScopeLaunch()
        // Verify if any output or action based on actual implementation.
    }

    @Test
    fun `test customScopeLaunch`() = runTest {
        coroutineViewModel.customScopeLaunch().join()
        // Check for expected results or outputs.
    }

    @Test
    fun `test launchWithContext`() = runTest {
        coroutineViewModel.launchWithContext()
        // Assert actions or state if launchWithContext modifies anything
    }

    @Test
    fun `test asyncExample`() = runTest {
        val result = coroutineViewModel.asyncExample()
        assertEquals(42, result)
    }

    @Test
    fun `test concurrentAsyncExample`() = runTest {
        val result = coroutineViewModel.concurrentAsyncExample()
        assertEquals(42, result) // Should return the sum of async coroutines
    }

    @Test
    fun `test runBlockingExample`() {
        coroutineViewModel.runBlockingExample()
        // Verify any output if necessary
    }

    @Test
    fun `test lazyCoroutine`() = runTest {
        coroutineViewModel.lazyCoroutine()
        advanceUntilIdle() // Ensures the coroutine has completed if it’s been started
    }

   /* @Test
    fun `test exampleFlow`() = runTest(timeout = Duration(2000)) {
        coroutineViewModel.exampleFlow().test {
            assertEquals(1, awaitItem())
            advanceTimeBy(500) // Advances virtual time
            assertEquals(2, awaitItem())
            advanceTimeBy(500)
            assertEquals(3, awaitItem())
            awaitComplete()
        }
    }*/

    @Test
    fun `test suspendingFunction`() = runTest {
        coroutineViewModel.suspendingFunction()
        // Check or verify if it causes any side effects
    }

    @Test
    fun `test timedCoroutines`() = runTest {
        coroutineViewModel.timedCoroutines()
        // You can verify time taken or actions performed here
    }

    @Test
    fun `test cancelableCoroutine`() = runTest {
        val job = coroutineViewModel.cancelableCoroutine()
        advanceTimeBy(1000) // Moves time forward
        job.cancelAndJoin() // Cancels and waits for the coroutine to finish
        assertTrue(job.isCancelled) // Verify cancellation
    }
}
