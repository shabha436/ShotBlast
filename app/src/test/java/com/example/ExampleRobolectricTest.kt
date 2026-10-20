package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VideoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shotblast", appName)
  }

  @Test
  fun `verify video repository initial content and categories`() {
    val videos = VideoRepository.videos.value
    assertTrue("Videos repository should have items", videos.isNotEmpty())
    val travelVideo = videos.find { it.category == "Travel" }
    assertNotNull("Travel category video should exist", travelVideo)
    assertEquals("The Most Beautiful Places on Earth | 4K Cinematic Travel Video", travelVideo?.title)
  }
}
