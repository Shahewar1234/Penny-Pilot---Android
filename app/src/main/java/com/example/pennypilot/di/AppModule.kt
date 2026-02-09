package com.example.pennypilot.di

import android.content.Context
import androidx.room.Room
import com.example.pennypilot.data.local.AppDatabase
import com.example.pennypilot.data.local.dao.UserDao
import com.example.pennypilot.data.repository.FirestoreUserRepositoryImpl
import com.example.pennypilot.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
// --- START OF FIX ---
// Import FirebaseDatabase because you are now using it.
import com.google.firebase.database.FirebaseDatabase
// --- END OF FIX ---
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- START OF FIX ---
    // Uncomment this entire block to provide FirebaseDatabase to Hilt.
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
    // --- END OF FIX ---

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "penny_pilot_db"
        )
            .fallbackToDestructiveMigration(false) // Remember: Implement proper migrations for production
            .build()

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore
    ): UserRepository {
        return FirestoreUserRepositoryImpl(firestore)
    }
}


//Koin Module
//
//package com.example.pennypilot.di
//
//import androidx.room.Room
//import com.example.pennypilot.data.local.AppDatabase
//import com.example.pennypilot.data.repository.ExpenseRepository
//import com.example.pennypilot.data.repository.FirebaseExpenseRepositoryImpl
//import com.example.pennypilot.data.repository.FirestoreUserRepositoryImpl
//import com.example.pennypilot.data.repository.UserRepository
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.firestore.firestore
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.client.plugins.logging.*
//import io.ktor.serialization.kotlinx.json.json
//import kotlinx.serialization.json.Json
//import org.koin.android.ext.koin.androidContext
//import org.koin.androidx.viewmodel.dsl.viewModel // <-- Important import for ViewModels
//import org.koin.dsl.module
//
///**
// * A single Koin module that provides all application dependencies.
// * This replaces the previous Hilt modules (AppModule, NetworkModule, RepositoryModule).
// */
//val appModule = module {
//
//    // --- Singleton Dependencies (single { ... }) ---
//    // These are created once and shared across the app.
//
//    // Firebase Services
//    single { FirebaseAuth.getInstance() }
//    single { FirebaseDatabase.getInstance() }
//    // Use the KTX extension for a cleaner way to get Firestore instance
//    single { Firebase.firestore }
//
//    // Ktor HTTP Client
//    single {
//        HttpClient(CIO) {
//            install(Logging) {
//                level = LogLevel.ALL
//            }
//            install(ContentNegotiation) {
//                json(Json {
//                    prettyPrint = true
//                    isLenient = true
//                    ignoreUnknownKeys = true
//                })
//            }
//        }
//    }
//
//    // Room Database and DAOs
//    single {
//        Room.databaseBuilder(
//            androidContext(), // Provides the application context
//            AppDatabase::class.java,
//            "penny_pilot_db"
//        )
//            .fallbackToDestructiveMigration() // Note: Be cautious with this in production
//            .build()
//    }
//    // Provide the UserDao from the AppDatabase
//    single { get<AppDatabase>().userDao() }
//
//
//    // --- Repository Dependencies (single<Interface> { Implementation }) ---
//    // Binds interfaces to their concrete implementations.
//
//    single<UserRepository> {
//        FirestoreUserRepositoryImpl(firestore = get()) // Koin injects Firestore
//    }
//
//    single<ExpenseRepository> {
//        // Koin automatically resolves the dependencies (FirebaseAuth, FirebaseFirestore) from the singletons defined above
//        FirebaseExpenseRepositoryImpl(auth = get(), firestore = get())
//    }
//}
//
///**
// * A separate module for your ViewModels.
// * Using viewModel { ... } allows Koin to handle the ViewModel lifecycle correctly.
// */
//val viewModelModule = module {
//    // Example:
//    // viewModel { MyViewModel(userRepository = get(), expenseRepository = get()) }
//    // viewModel { LoginViewModel(auth = get()) }
//    // Add your actual ViewModel definitions here.
//}
