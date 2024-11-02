package com.example.flow

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FlowViewModelTest {

    private lateinit var viewModel: FlowViewModel

    @Before
    fun setUp() {
        viewModel = FlowViewModel()
    }

    //runBlocking is a coroutine builder that allows you to run coroutines in a blocking way.
    //This is useful when you want to test suspending functions.
    @Test
    fun `coldFlowTest, check that it changes the value`() = runBlocking {
        viewModel.flow.test {
            //awaits for 1st emission
            val emission = awaitItem()
            assertThat(emission).isEqualTo(DataState.Loading)
            //awaits for 2nd emission
            val emission2 = awaitItem()
            assertThat(emission2).isEqualTo(
                DataState.Loaded(
                    listOf(
                        "Movie 1",
                        "Movie 2",
                        "Movie 3"
                    )
                )
            )
            assertThat(awaitComplete()).isEqualTo(Unit)
        }
    }

    @Test
    fun `testSharedSlow`() = runBlocking() {
        val job = launch {
            viewModel.sharedFlow.test {
                val emision = awaitItem()
                assertThat(emision).isEqualTo(DataState.Loaded(listOf("Movie 1", "Movie 2", "Movie 3")))
            }
        }
        viewModel.updateSharedFlow()
        job.join()
        job.cancel()
    }

    @Test
    fun combineFlows(){
        GlobalScope.launch {
            val flow1 = flowOf(1, 2, 3)
            val flow2 = flowOf("a", "b", "c")
            flow1.combine(flow2) { num, str -> "$num$str" }.collect { println(it) }
        }
    }
}
