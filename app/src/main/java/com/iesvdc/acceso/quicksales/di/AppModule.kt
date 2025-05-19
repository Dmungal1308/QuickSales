package com.iesvdc.acceso.quicksales.di

import android.content.Context
import com.iesvdc.acceso.quicksales.data.datasource.network.FavoriteApi
import com.iesvdc.acceso.quicksales.data.datasource.network.ProductApi
import com.iesvdc.acceso.quicksales.data.datasource.network.UserApi
import com.iesvdc.acceso.quicksales.data.repository.FavoriteRepository
import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import com.iesvdc.acceso.quicksales.domain.usercase.cartera.DepositUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.cartera.WithdrawUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.ChangePasswordUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.GetBalanceUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.AddFavoriteUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.GetFavoritesUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.RemoveFavoriteUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.AddProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.DeleteProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.UpdateProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.GetProfileUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.UpdateProfileUseCase
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
    fun provideLogoutUseCase(@ApplicationContext ctx: Context): LogoutUseCase =
        LogoutUseCase(ctx)

    @Provides @Singleton
    fun provideProductRepo(
        api: ProductApi,
        @ApplicationContext ctx: Context
    ): ProductRepository = ProductRepository(api, ctx)

    @Provides @Singleton
    fun provideUserRepository(
        api: UserApi,
        @ApplicationContext ctx: Context
    ): UserRepository = UserRepository(api, ctx)

    @Provides @Singleton
    fun provideFavoriteRepository(
        api: FavoriteApi
    ): FavoriteRepository = FavoriteRepository(api)

    // PRODUCT USE CASES
    @Provides fun provideGetProductsUC(r: ProductRepository)      = GetProductsUseCase(r)
    @Provides fun provideAddProductUC(r: ProductRepository)       = AddProductUseCase(r)
    @Provides fun provideUpdateProductUC(r: ProductRepository)    = UpdateProductUseCase(r)
    @Provides fun provideDeleteProductUC(r: ProductRepository)    = DeleteProductUseCase(r)
    @Provides fun providePurchaseProductUC(r: ProductRepository)  = PurchaseProductUseCase(r)
    @Provides fun provideGetOtherProductsUC(r: ProductRepository) = GetOtherProductsUseCase(r)

    // WALLET USE CASES
    @Provides fun provideGetBalanceUC(r: UserRepository) = GetBalanceUseCase(r)
    @Provides fun provideDepositUC(r: UserRepository)    = DepositUseCase(r)
    @Provides fun provideWithdrawUC(r: UserRepository)   = WithdrawUseCase(r)

    // FAVORITE USE CASES
    @Provides fun provideGetFavoritesUC(r: FavoriteRepository)   = GetFavoritesUseCase(r)
    @Provides fun provideAddFavoriteUC(r: FavoriteRepository)    = AddFavoriteUseCase(r)
    @Provides fun provideRemoveFavoriteUC(r: FavoriteRepository) = RemoveFavoriteUseCase(r)

    @Provides fun provideGetProfileUseCase(repo: UserRepository) = GetProfileUseCase(repo)
    @Provides fun provideUpdateProfileUseCase(repo: UserRepository) = UpdateProfileUseCase(repo)
    @Provides fun provideChangePasswordUseCase(repo: UserRepository) = ChangePasswordUseCase(repo)

}
