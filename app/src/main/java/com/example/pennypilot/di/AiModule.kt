package com.example.pennypilot.di

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.app
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    // Hilt can provide the default FirebaseApp instance automatically.
    // We can add it as a parameter here.
    @Provides
    @Singleton
    fun provideGenerativeModel(firebaseApp: FirebaseApp): GenerativeModel {
        val config = generationConfig {
            temperature = 0.7f
        }

        // Initialize the model from the Firebase Vertex AI SDK
        // using the injected FirebaseApp instance.
        return FirebaseAI.getInstance(firebaseApp).generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = config
        )
    }

    // You also need to tell Hilt how to provide the FirebaseApp instance.
    // Add this provider function to the module.
    @Provides
    @Singleton
    fun provideFirebaseApp(): FirebaseApp {
        return Firebase.app
    }

//    @Provides
//    @Singleton
//    fun provideFirestore(): FirebaseFirestore {
//        return Firebase.firestore
//    }

//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(): FirebaseAuth {
//        return Firebase.auth
//    }
}
