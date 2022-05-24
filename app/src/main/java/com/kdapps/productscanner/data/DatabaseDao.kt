package com.kdapps.productscanner.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DatabaseDao {

    @Query("SELECT * FROM Products ORDER BY product_code")
    fun getAllProduct(): LiveData<List<Product>>

    @Query("SELECT * FROM Products WHERE product_code LIKE :product_code")
    fun getProductByCode(product_code : String): List<Product>

    @Query("SELECT quantity FROM Products WHERE product_code LIKE :product_code")
    fun getQuantity(product_code : String): LiveData<Long>

    @Query("SELECT * FROM Products WHERE product_code LIKE :filter ORDER BY product_code")
    fun getMatchingProduct(filter : String) : LiveData<List<Product>>

    @Query("SELECT COUNT(product_code) FROM Products")
    fun getDatabaseSize() : LiveData<Int>

    @Insert
    fun insertProduct(product : Product) : Long

    @Update
    fun updateProduct(product : Product)

    @Delete
    fun deleteProduct(product : Product)

    @Query("DELETE FROM Products")
    fun purgeDatabase()
}