# Revisit2 Android App

### Activities

#### MainActivity.java
The primary activity that implements a navigation drawer pattern with WebView integration. Features include:
- URL input field with Google as default homepage
- Network controls (internet access toggle and update settings)
- Navigation menu with links to Settings, About, Test, and Log activities
- Custom back button handling for drawer and WebView navigation
- Progress bar integration for web page loading
- Uses ViewBinding for UI components

#### AboutActivity.java
Simple activity that displays application information using ViewBinding. Currently implements basic layout inflation without additional functionality.

#### LogActivity.java
Advanced logging activity that provides real-time system log monitoring with features:
- Real-time logcat output display using RecyclerView
- Log priority filtering (Verbose, Debug, Info, Warning, Error)
- Swipe-to-refresh functionality
- Color-coded log entries based on priority
- Maximum log limit of 1000 entries
- Detailed log information including timestamp, PID, TID, priority, tag, and message
- Clear logs functionality

#### TestActivity.java
Development activity with edge-to-edge display support and system bar insets handling. Currently serves as a template for testing new features with ViewBinding integration.

#### PreferenceActivity.java
Settings activity that implements Android's PreferenceFragmentCompat to manage web settings. Uses a custom preferences XML resource (websettings.xml) for configuration options.

### WebView Components

#### MyWebView.java
Custom WebView implementation that extends Android's WebView with:
- JavaScript and content access configuration
- Progress bar integration
- Custom WebViewClient and WebChromeClient integration
- Preference management for web settings
- File access and universal access settings
- Wide viewport and offscreen pre-raster support

#### MyWebViewClient.java
Custom WebViewClient implementation that:
- Intercepts and manages web resource requests
- Integrates with WebResourceManager for resource handling
- Supports progress bar integration
- Handles resource caching and offline access

#### MyWebChromeClient.java
Custom WebChromeClient that provides:
- Progress bar integration for page loading
- Automatic progress bar visibility management
- Progress percentage display (0-100%)

#### WebResourceDownloader.java
Handles downloading and caching of web resources for offline access.

#### WebResourceDownloader2.java
Enhanced version of WebResourceDownloader with additional features for resource management.

### Utilities

#### MyUtils.java
Core utility class that provides:
- Resource downloading and caching functionality
- File path management and normalization
- MIME type and encoding handling
- HTTP header management
- Thread pool management for concurrent operations
- Logging functionality
- Root path management for offline storage
- URL encoding and hashing utilities