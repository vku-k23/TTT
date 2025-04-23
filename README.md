# CineVibe - Movie Explorer App

CineVibe is a modern Android app built with Kotlin that allows users to browse popular movies, search for specific titles, and view movie details. The app follows Clean Architecture principles and is built with the latest Android technologies.

## Features

- **Authentication Flow**: Login and registration screens
- **Movie Discovery**: Browse popular movies from TheMovieDB API
- **Search**: Search for movies by title
- **Movie Details**: View detailed information about movies
- **Modern UI**: Built with Jetpack Compose with a clean, modern design

## Architecture

The application follows Clean Architecture principles, separating concerns into distinct layers:

- **Presentation Layer**: UI components built with Jetpack Compose, ViewModels
- **Domain Layer**: Use cases, business logic, repository interfaces
- **Data Layer**: Repository implementations, API services, database

## Technologies Used

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit for building native UI
- **Coroutines & Flow**: Asynchronous programming
- **Hilt**: Dependency injection
- **Retrofit**: Network requests
- **Kotlin Serialization**: JSON parsing
- **Coil**: Image loading
- **Navigation Compose**: Screen navigation
- **Room**: Local database storage
- **DataStore**: Preferences storage
- **Material 3**: UI design system

## Getting Started

### Prerequisites

- Android Studio Giraffe or newer
- JDK 17 or newer
- Android SDK 26+

### Setup

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/cinevibe.git
   ```

2. Open the project in Android Studio.

3. Create a `local.properties` file in the root directory and add your TheMovieDB API key:
   ```
   TMDB_API_KEY=your_api_key_here
   ```

4. Build and run the project on an emulator or physical device.

## Project Structure

```
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/ttt/cinevibe
│   │   │   │   ├── data            # Data layer - API, repositories, etc.
│   │   │   │   ├── di             # Dependency Injection modules
│   │   │   │   ├── domain         # Domain layer - use cases, models, etc.
│   │   │   │   ├── presentation   # Presentation layer - UI, ViewModels
│   │   │   │   └── ui             # UI components - themes, etc.
│   │   │   └── res                # Android resources
```

## API

This app uses TheMovieDB API for movie data. You'll need to obtain an API key from [TheMovieDB](https://www.themoviedb.org/documentation/api) to run the app.
