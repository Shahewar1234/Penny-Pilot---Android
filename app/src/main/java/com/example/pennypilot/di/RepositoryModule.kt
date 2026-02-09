package com.example.pennypilot.di // Ensure this package is correct

import com.example.pennypilot.data.repository.ExpenseRepository
import com.example.pennypilot.data.repository.FirebaseExpenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        // Hilt needs to know how to create FirebaseExpenseRepositoryImpl.
        // This is typically done via an @Inject constructor in FirebaseExpenseRepositoryImpl.
        // The dependencies for FirebaseExpenseRepositoryImpl (e.g., FirebaseFirestore, FirebaseAuth)
        // should be provided in another module (like AppModule).
        impl: FirebaseExpenseRepositoryImpl
    ): ExpenseRepository
}

//koin

//package com.example.pennypilot.di
//
//import com.example.pennypilot.data.repository.ExpenseRepository
//import com.example.pennypilot.data.repository.FirebaseExpenseRepositoryImpl
//import com.example.pennypilot.data.repository.FirestoreUserRepositoryImpl
//import com.example.pennypilot.data.repository.UserRepository
//import org.koin.dsl.module
//
//val repositoryModule = module {
//
//    /**
//     * Binds the UserRepository interface to its Firestore implementation.
//     * Koin will automatically inject the required `FirebaseFirestore` instance,
//     * which should be provided in another module (like your main AppModule).
//     * This is equivalent to Hilt's @Binds annotation.
//     */
//    single<UserRepository> {
//        FirestoreUserRepositoryImpl(firestore = get())
//    }
//
//    /**
//     * Binds the ExpenseRepository interface to its Firebase implementation.
//     * Koin automatically injects the `FirebaseAuth` and `FirebaseFirestore`
//     * dependencies from the singletons defined elsewhere.
//     */
//    single<ExpenseRepository> {
//        FirebaseExpenseRepositoryImpl(auth = get(), firestore = get())
//    }
//}

