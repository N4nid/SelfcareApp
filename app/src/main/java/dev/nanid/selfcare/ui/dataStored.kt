package dev.nanid.selfcare.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class dataStored: ViewModel() {

    // Create a LiveData with a String
    val currentMood: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}