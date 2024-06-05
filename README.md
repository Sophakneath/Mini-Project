# CrunchGuard: Empowering Food Safety through Allergen-Aware Packaged Food Product Classification on Android Platform using Machine Learning

## Overview

The Food Allergen Alert App leverages machine learning for image classification to detect packaged food products and provide allergen alerts based on user-inputted allergen information. This mobile application aims to help users with food allergies make safer food choices by recognizing potential allergens in food products.

## Features

- **Image Classification**: Utilizes machine learning to identify packaged food products from images.
- **Allergen Alerts**: Provides real-time allergen warnings based on user-specified allergens.
- **User-Friendly Interface**: Designed for a seamless user experience on Android devices.

## Android Configuration

The app is configured to run on Android devices with the following specifications:

```groovy
android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```

## Colab Folder

The `colab` folder contains essential files for model training and data collection:

- **Model Training Notebook**: A Jupyter notebook to train the machine learning model.
- **Coupang Scrapper Notebook**: A notebook to scrape datasets from Coupang for training purposes.
- **Dataset**: https://drive.google.com/file/d/14NJrFBxM7iB5iQxL6D1KTLJCwrFdHTgk/view?usp=sharing

## Usage

1. **Launch the App**: Install the app on your Android device and launch it.
2. **Capture or Select an Image**: Use the app to capture or select an image of a packaged food product.
3. **Input Allergen Information**: Specify your allergens in the app settings.
4. **Get Alerts**: The app will process the image and alert you if any of your allergens are detected in the product.

## Contact

For any inquiries or support, please reach out to us at [sophakneath08@snu.ac.kr](mailto:sophakneath08@snu.ac.kr).
