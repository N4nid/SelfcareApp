package dev.nanid.selfcare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MoodData : ViewModel() {

  // Create a LiveData with a String
  val mood: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}
