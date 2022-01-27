package com.setjy.photogallery.api

import com.google.gson.annotations.SerializedName
import com.setjy.photogallery.GalleryItem

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>

}