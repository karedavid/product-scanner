package com.kdapps.productscanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Products")
data class Product(

    @ColumnInfo(name = "product_code") @PrimaryKey(autoGenerate = false)
    var product_code: String = "Hello",

    @ColumnInfo(name = "quantity")
    var quantity: Long = 1,

    @ColumnInfo(name = "image")
    var image: String? = null

)