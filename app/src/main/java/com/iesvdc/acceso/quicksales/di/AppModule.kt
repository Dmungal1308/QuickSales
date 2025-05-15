package com.iesvdc.acceso.quicksales.di

import android.content.Context
import com.iesvdc.acceso.quicksales.data.datasource.network.ProductApi
import com.iesvdc.acceso.quicksales.data.datasource.network.UserApi
import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import com.iesvdc.acceso.quicksales.domain.usercase.AddProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.DeleteProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.DepositUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.GetBalanceUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.GetProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.UpdateProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.WithdrawUseCase
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
    fun provideLogoutUseCase(@ApplicationContext context: Context): LogoutUseCase =
        LogoutUseCase(context)


    @Provides @Singleton
    fun provideProductRepo(
        api: ProductApi,
        @ApplicationContext ctx: Context
    ): ProductRepository = ProductRepository(api, ctx)

    @Provides fun provideGetProductsUseCase(r: ProductRepository) = GetProductsUseCase(r)
    @Provides fun provideAddProductUseCase(r: ProductRepository) = AddProductUseCase(r)
    @Provides fun provideUpdateProductUseCase(r: ProductRepository) = UpdateProductUseCase(r)
    @Provides fun provideDeleteProductUseCase(r: ProductRepository) = DeleteProductUseCase(r)
    @Provides fun providePurchaseProductUseCase(r: ProductRepository) = PurchaseProductUseCase(r)
    @Provides fun provideGetOtherProductsUseCase(r: ProductRepository) = GetOtherProductsUseCase(r)

    @Provides fun provideGetBalanceUseCase(repo: UserRepository) = GetBalanceUseCase(repo)
    @Provides fun provideDepositUseCase(repo: UserRepository)    = DepositUseCase(repo)
    @Provides fun provideWithdrawUseCase(repo: UserRepository)   = WithdrawUseCase(repo)

    @Provides @Singleton
    fun provideUserRepository(
        userApi: UserApi,
        @ApplicationContext ctx: Context
    ): UserRepository = UserRepository(userApi, ctx)


}
