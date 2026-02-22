# SpideryBook

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue?logo=kotlin)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![License](https://img.shields.io/badge/License-Proprietary-red)

**SpideryBook** is a modern, high-performance Android application built as a thesis project to demonstrate advanced mobile development capabilities, clean architecture, and dynamic video scraping techniques. The application serves as an advanced content aggregator and video player interface.

## 🚀 Features

*   **Advanced Content Aggregation:** Dynamically scrapes and parses metadata from multiple web providers (e.g., AnimeFLV, PelisPlus).
*   **Intelligent Video Extraction:** Utilizes Reverse Engineering techniques to bypass basic obfuscation on video hosts like Streamtape and Vidhide, resolving direct `.mp4` and `.m3u8` streams.
*   **Native ExoPlayer Integration:** Seamless hardware-accelerated streaming without relying on external web browsers.
*   **Local Download Manager:** Background downloading capabilities leveraging Android's native `DownloadManager` for offline viewing.
*   **Modern UI/UX:** Built with Android Views and Material Design components, offering Dark/Light themes, horizontal carousels, and an immersive viewing experience.
*   **Clean Architecture (MVVM):** Strictly adheres to the Model-View-ViewModel pattern, utilizing Kotlin Coroutines, Flow, Room Database for local storage (History/Favorites), and Hilt for Dependency Injection.
*   **Smart Fallbacks:** Implements fallback mechanisms to handle heavily obfuscated servers using WebView raw rendering.

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Networking:** Retrofit2, OkHttp3
*   **HTML Parsing:** Jsoup
*   **Dependency Injection:** Dagger-Hilt
*   **Local Storage:** Room Database, DataStore (Preferences)
*   **Media Player:** Media3 ExoPlayer
*   **Image Loading:** Coil

## ⚖️ Legal Disclaimer & Liability

This application is strictly an personal project designed to demonstrate software engineering patterns, HTTP networking, and DOM parsing in Android.

*   **No Content Hosting:** SpideryBook does not host, store, or distribute any audiovisual content or copyrighted media on its own servers. 
*   **Client-Side Execution:** The application acts merely as a client-side web browser and link indexer. All content is streamed directly from third-party services accessed by the user over the public internet.
*   **Responsibility:** The author of this codebase is not responsible for the content, quality, or legal status of the media accessed by the end-user through this tool. Any copyright infringement derived from the use of third-party websites falls under the jurisdiction and responsibility of those specific website operators and the end-user.

## 🔒 License

**Copyright © 2025-2026 Cristian Huerta. All Rights Reserved.**

This repository and its contents are closed-source and proprietary. You may not copy, modify, distribute, sell, or use this code for any personal or commercial purposes without explicit, written permission from the author. See the `LICENSE` file for more details.
