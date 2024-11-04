package com.example.flow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlowViewModel : ViewModel() {

    //this is a cold flow
    //will work only when there are collectors
    val flow = flow<DataState> {
        emit(DataState.Loading)
        delay(500)
        emit(DataState.Loaded(listOf("Movie 1", "Movie 2", "Movie 3")))
    }

    //Converts a cold flow to a StateFlow, replaying the latest value to new collectors.
    // Useful when you want to create a shared state that can be observed by multiple collectors.
    val state = flow.stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading)

    // Convert a cold flow to a hot flow, sharing the emissions with multiple collectors.
    // Ideal for creating shared broadcasts of cold flows to multiple collectors
    val shared = flow.shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)

    //this is a hot flow
    //will work even if there are no collectors
    // StateFlow always replays the latest value to any new collectors, making it a great choice when you need the current state available at all times.
    private val _stateFlow = MutableStateFlow<DataState>(DataState.Error("not initialized"))
    val stateFlow = _stateFlow.asStateFlow() // this is immutable flow

    suspend fun updateStateFlow() {
        _stateFlow.value = DataState.Loading
        delay(100)
        _stateFlow.value = DataState.Loaded(listOf("Movie 1", "Movie 2", "Movie 3"))
    }

    //this is a hot shared flow
    //will work even if there are no collectors, but will work only once. Works like a broadcast channel
    // SharedFlow doesn’t require an initial value, as it can start empty
    private val _sharedFlow = MutableSharedFlow<DataState>(replay = 1)
    val sharedFlow = _sharedFlow.asSharedFlow() // this is immutable flow

    fun updateSharedFlow() {
        viewModelScope.launch {
            _sharedFlow.emit(DataState.Loaded(listOf("Movie 1", "Movie 2", "Movie 3")))
        }
    }

    /**
     * Use cases:
     * StateFlow: Ideal for representing UI states or other stateful data where there’s always a single “current state”
     * (e.g., UI components observing a ViewModel in MVVM architecture).
     * SharedFlow: More flexible, often used for one-time events (like navigation events, notifications, or messages)
     * or in situations where you may need to emit multiple past values for new subscribers (like in logging or multi-step processes).
     */
}

class FlowCollector(private val scope: CoroutineScope) {

    private val log = FlowCollector::class.java.simpleName
   // private val dispatchers = DefaultDispatchers()
    private val viewModel = FlowViewModel()

    fun wrongCollectFlow() {
        scope.launch {
            /**
             * which will suspend indefinitely at the first collect and never reach the collectLatest statement.
             * Each collect function is a suspending function that continuously listens to emissions, so once you enter the first collect,
             * it won’t exit until the flow completes, preventing the second collection from starting.
             */
            viewModel.flow.collect { dataState ->
                // Handle each emission (e.g., update the UI)
                println("$log: I will suspend indefinitely all functions below me")
            }
            viewModel.flow.collectLatest { dataState ->
                // Handle only the latest emission, cancel previous if new value arrives
                println("$log: I will never be printed")
            }
        }
    }

    fun collectStateFlow() {
        //will collect all emissions
        scope.launch {
            viewModel.stateFlow.collectLatest { dataState ->
                // Handle each emission (e.g., update the UI)
            }
        }

        //if there are any collisions it will collect only the latest emission and cancel the previous one
        scope.launch {
            viewModel.stateFlow.collectLatest { dataState ->
                // Handle only the latest emission, cancel previous if new value arrives
            }
            viewModel.updateStateFlow()
        }

    }

    /**
     * collect is required to actually trigger the flow; without it, the flow won’t produce any values.
     * onEach is Intermediate operator used to perform side effects on each emitted item returns flow
     */
    fun collectColdFlow() {
        viewModel.flow.onEach { dataState ->
            println("$log: $dataState")
            // Handle each emission
        }.catch { e ->
            // Handle error (e.g., log or update UI state)
        }.launchIn(scope)// Starts collecting in the scope

        scope.launch {
            viewModel.flow.collect { dataState ->
                // Handle only the latest emission, cancel previous if new value arrives
            }
        }
    }

    fun collectSharedFlow() {
        scope.launch {
            viewModel.sharedFlow.collect {
                Log.d(log, "state: $it")
            }
        }
        viewModel.updateSharedFlow()
    }
}
