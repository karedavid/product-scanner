# Product Scanner

This app lets you scan QR codes containing product codes and manage the stock of those products. 

## Introduction

This is a stock management app which allows users to scan QR codes containing item identifiers then update the stock for that product. The database is in the Firebase cloud, which require you to register an account first. 

- Scan QR codes to identify an item
- Use buttons to add to or substract from the quantity
- Take a photo of the item and store it in the databse
- Search the database

## Screenshots

| Successful read | Invalid code | List of items | Settings |
|:---:|:---:|:---:|:---:|
| ![](/screenshots/1.png) | ![](/screenshots/2.png) | ![](/screenshots/3.png) | ![](/screenshots/4.png) |

## Technical details

The application is a native Android app written in Kotlin. Firebase is used as cloud to store database. Firebase Realtime Database keeps track of the items, and Firebase Storage gives home for the photos taken of the products.

## Getting Started

You can open the project in Android Studio and build it for Android. 
