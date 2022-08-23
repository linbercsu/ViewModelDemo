package com.m.x.player.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*


class Data

class SimpleViewModel : ViewModel() {
    val data: LiveData<Data> get() = _data

    private val _data = MutableLiveData<Data>()
    private var job: Job? = null

    private val loading: Boolean get() = job != null && !job!!.isCompleted

    fun load() {
        if (loading)
            return

        job = viewModelScope.launch {
            val r = withContext(Dispatchers.Default) {

                val result =
                    try {
                        doLoad()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@withContext null
                    }

                return@withContext result
            } ?: return@launch

            _data.value = r
        }
    }

    private fun doLoad(): Data {
        Thread.sleep(5000)
        return Data()
    }
}

//=============================================================
/*

 */

class ViewModelResult<D, E>(val status: Status, val data: D?, val error: E?) {

    enum class Status {
        Loading, Error, Success
    }
}

class FullViewModel : ViewModel() {

    val data: LiveData<ViewModelResult<Data, Exception>> get() = _data
    private val _data: MutableLiveData<ViewModelResult<Data, Exception>> = MutableLiveData()
    var lastData: Data? = null

    private val loading: Boolean get() = job != null && !job!!.isCompleted

    private var job: Job? = null


    fun load() {
        if (loading)
            return


        val result = ViewModelResult<Data, Exception>(ViewModelResult.Status.Loading, null, null)
        _data.postValue(result)

        job = viewModelScope.launch {

            val r = withContext(Dispatchers.Default) {

                val data =
                    try {
                        doLoad()
                    } catch (e: Exception) {

                        return@withContext ViewModelResult<Data, Exception>(ViewModelResult.Status.Error, null, e)
                    }

                return@withContext ViewModelResult<Data, Exception>(ViewModelResult.Status.Success, data, null)
            }

            if (r.status == ViewModelResult.Status.Success) {
                lastData = r.data
            }

            _data.value = r
        }
    }

    private fun doLoad(): Data {
        Thread.sleep(5000)
        return Data()
    }
}