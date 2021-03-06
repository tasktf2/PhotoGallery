package com.setjy.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel : ViewModel() {
    val galleryItemLiveData: LiveData<List<GalleryItem>>
    init {
        galleryItemLiveData=FlickrFetchr().fetchPhotos()
    }

    override fun onCleared() {
        super.onCleared()

    }
}