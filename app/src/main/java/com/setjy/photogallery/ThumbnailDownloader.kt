package com.setjy.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
) : HandlerThread(TAG) {

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()


    val fragmentLifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            Log.i(TAG, "Starting background thread")
            start()
            looper

        }

        override fun onDestroy(owner: LifecycleOwner) {
            Log.i(TAG, "Destroying background thread")
            quit()
        }

    }
    val viewLifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            fun clearQueue() {
                Log.i(TAG, "Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        Log.d(TAG,"Looper is being prepared")
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got request for URL:${requestMap[target]}")
                    handleRequest(target)
                }
            }

            private fun handleRequest(target: T) {
                val url = requestMap[target] ?: return
                val bitmap = flickrFetchr.fetchPhoto(url) ?: return
                responseHandler.post(Runnable {
                    if (requestMap[target] != url || hasQuit) return@Runnable

                    requestMap.remove(target)
                    onThumbnailDownloaded(target, bitmap)
                })
            }
        }

    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }
}


