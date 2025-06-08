# Revisit2 Android App

## Workflow
- opens the app
- in the left nav view enters the url in the url bar
- webview.load(that url)
- shouldIntercepRequest the url request in MyWebViewClient.java

### Activities

#### MainActivity.java
The primary activity that implements dual navigation drawer pattern with WebView integration. Features include:
- Left navigation drawer with:
  - URL input field with Google as default homepage
  - Network controls (internet access toggle and update settings)
  - Navigation menu with links to Settings, About, Test, and Log activities
- Right navigation drawer with:
  - Real-time URL monitoring system
  - Resource request tracking
  - Loading status indicators
  - File size information
- Custom back button handling for both drawers and WebView navigation
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
- URL monitoring integration

#### MyWebViewClient.java
Custom WebViewClient implementation that:
- Intercepts and manages web resource requests
- Integrates with WebResourceManager for resource handling
- Supports progress bar integration
- Handles resource caching and offline access
- Tracks and reports URL loading status
- Monitors resource sizes and loading progress

#### MyWebChromeClient.java
Custom WebChromeClient that provides:
- Progress bar integration for page loading
- Automatic progress bar visibility management
- Progress percentage display (0-100%)

#### WebResourceDownloader.java
Handles downloading and caching of web resources for offline access.

#### WebResourceDownloader2.java
Enhanced resource downloader implementation with features:
- Asynchronous resource downloading using ExecutorService
- Configurable timeout settings (default 15 seconds)
- Efficient buffer-based file writing
- Comprehensive error handling and logging
- Automatic cleanup of partial downloads
- Response metadata management (MIME types, encoding, headers)
- Null safety checks and validation
- Resource update control through MyUtils.shouldUpdate flag

Key features:
- Concurrent download support
- Configurable write buffer size
- Proper resource cleanup on failure
- Detailed error logging
- Response metadata preservation
- Header management
- MIME type and encoding tracking

### Models

#### UrlItem.java
Data class for tracking web resource requests with:
- URL and HTTP method tracking
- Resource size monitoring
- Loading status (IGNORED, LOADING, LOADED_LOCAL, LOADED_REMOTE, ERROR)
- Download progress tracking
- Getters and setters for all properties

### Adapters

#### UrlAdapter.java
RecyclerView adapter for displaying URL monitoring information:
- Manages list of UrlItems
- Adds new URLs at the top of the list
- Updates URL status and progress in real-time
- Formats file sizes in human-readable format
- Handles progress bar visibility based on loading status

### Layouts

#### item_url.xml
Layout for individual URL monitoring items:
- URL display with ellipsis for long URLs
- HTTP method display
- File size display
- Progress bar for loading status
- ConstraintLayout-based design for efficient layout

#### nav_header_main.xml
Left navigation drawer header layout with:
- URL input field
- Network control switches
- Refresh button

#### nav_header_url_monitor.xml
Right navigation drawer header layout with:
- Title section
- URL monitoring RecyclerView
- Swipe-to-refresh functionality
- Clear monitoring data button

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
- Resource update control through shouldUpdate flag

---

## Requirements

- Android API level 21 or higher
- Internet permission for online functionality
- Storage permission for caching

## License

This project is licensed under the GPL-v3 License. See the [LICENSE](LICENSE) file for details.