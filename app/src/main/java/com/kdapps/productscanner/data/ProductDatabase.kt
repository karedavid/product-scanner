package com.kdapps.productscanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class], version = 1)
abstract class ProductDatabase : RoomDatabase() {

    abstract fun databaseDao(): DatabaseDao

    companion object {
        private var instance: ProductDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): ProductDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    context.applicationContext, ProductDatabase::class.java,
                    "product-database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            return instance!!
        }
    }
}