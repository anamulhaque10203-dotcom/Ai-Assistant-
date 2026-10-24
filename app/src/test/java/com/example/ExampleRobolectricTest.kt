package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ConversationEntity
import com.example.ui.viewmodel.NovaVoiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("NOVA AI", appName)
  }

  @Test
  fun `verify conversation entity instantiation`() {
    val entity = ConversationEntity(
      question = "What is AI?",
      answer = "Artificial Intelligence is the simulation of human intelligence.",
      languageCode = "en"
    )
    assertEquals("What is AI?", entity.question)
    assertNotNull(entity.timestamp)
  }

  @Test
  fun `verify voice states`() {
    assertEquals(5, NovaVoiceState.values().size)
  }
}
