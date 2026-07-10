# Agora chat demo

This repository will help you learn how to use Agora chat SDK to implement a simple android chat app, like whatsapp or wechat.

With this sample app, you can:

- Log in to the chat server
- Start a chat
- Manage the conversation list
- Add contacts
- Join group chats
- Add your contacts to your block list
- Send various types of messages, such as text, emoji, image, voice and file messages
- Log out of the chat server

## Prerequisites

- Make sure you have made the preparations mentioned in the [Agora Chat SDK quickstart](https://docs.agora.io/en/agora-chat/get-started/get-started-sdk?platform=android).
- Prepare the development environment:
  - Java Development Kit (JDK) 17 or higher
  - Android Studio Flamingo | 2025.2.1 or later

## Run the sample project

Follow these steps to run the sample project:

### 1. Clone the repository to your local device

```java
    git clone git@github.com:AgoraIO-Usecase/AgoraChat-android.git
```

### 2. Open the Android project with Android Studio

### 3. Configure the Chat App ID

Obtain your Chat App ID from the [Agora Console](https://console.agora.io/) and set it before calling `ChatClient#init()`.

```kotlin
val chatOptions = ChatOptions().apply {
    // Set your Chat App ID
    appId = "Your Chat App ID"
    ...
}
// Initialization
ChatClient.getInstance().init(context, chatOptions)
```

For details, see the [prerequisites](https://docs.agora.io/en/agora-chat/get-started/get-started-sdk?platform=android) in Agora Chat SDK Guide.

### 4. Configure `local.properties`

Add the properties required by the features you use to the `local.properties` file in the project root. You can copy the following template and replace the placeholder values:

```properties
# Required App IDs
AGORA_CHAT_APPID="Your Chat App ID"
AGORA_RTC_APPID="Your RTC App ID"

# Optional: RTC calls
APP_RTC_TOKEN_URL=/your/rtc/token/path
APP_RTC_CHANNEL_MAPPER_URL=/your/rtc/channel-mapper/path

# Optional: App Server
APP_SERVER_DOMAIN=your.app.server
APP_SERVER_LOGIN=/your/login/path
APP_SERVER_REGISTER=/your/register/path
APP_BASE_USER=/your/user/base/path
APP_UPLOAD_AVATAR=/your/avatar/upload/path
APP_BASE_GROUP=/your/group/base/path
APP_GROUP_AVATAR=/your/group/avatar/path

# Optional: FCM push notifications
FCM_SENDERID=123456789012

# Optional: APK signing
KEY_STORE_FILE_PATH=./keystore/sdkdemo.jks
KEY_STORE_PASSWORD=Your keystore password
KEY_ALIAS=Your key alias
KEY_PASSWORD=Your key password
```

`AGORA_CHAT_APPID` and `AGORA_RTC_APPID` are required by the current Gradle configuration and are passed directly to generated `BuildConfig` source code, so their values must include straight double quotation marks (`"`). If you do not use RTC calls, set `AGORA_RTC_APPID=""`. Do not commit `local.properties`, because it can contain credentials and machine-specific paths.

#### Required property

| Property | Description |
| --- | --- |
| `AGORA_CHAT_APPID` | Chat App ID used to initialize the Agora Chat SDK. |
| `AGORA_RTC_APPID` | Agora RTC App ID used by CallKit. Use `""` if RTC calls are disabled. |

#### Optional RTC properties

Configure these properties to enable audio and video calls.

| Property | Description |
| --- | --- |
| `APP_RTC_TOKEN_URL` | App Server path used to obtain an RTC token. Start the path with `/`. |
| `APP_RTC_CHANNEL_MAPPER_URL` | App Server path used to map Chat users to RTC accounts. Start the path with `/`. |

#### Optional App Server properties

The demo constructs App Server URLs as `https://<APP_SERVER_DOMAIN><path>`. Start each path with `/`.

| Property | Description |
| --- | --- |
| `APP_SERVER_DOMAIN` | App Server host name, without the protocol or a trailing slash. |
| `APP_SERVER_LOGIN` | Path of the login endpoint. |
| `APP_SERVER_REGISTER` | Path of the registration endpoint exposed through `BuildConfig`. |
| `APP_BASE_USER` | Base path for user profile APIs. |
| `APP_UPLOAD_AVATAR` | Path appended to the user API URL when uploading a user avatar. |
| `APP_BASE_GROUP` | Base path for group profile APIs. |
| `APP_GROUP_AVATAR` | Path appended to the group API URL when uploading a group avatar. |

#### Optional push property

| Property | Description |
| --- | --- |
| `FCM_SENDERID` | Firebase Cloud Messaging sender ID used to enable FCM push notifications. |

#### Optional signing properties

If omitted, the Gradle configuration uses the demo keystore defaults. Replace them when signing your own APK.

| Property | Description |
| --- | --- |
| `KEY_STORE_FILE_PATH` | Path to the keystore file, relative to the `app` module or absolute. |
| `KEY_STORE_PASSWORD` | Keystore password. |
| `KEY_ALIAS` | Signing key alias. |
| `KEY_PASSWORD` | Signing key password. |

## Contact Us

- You can find full API document at [Document Center](https://docs.agora.io/en/agora-chat/overview/product-overview?platform=android)
- You can file bugs about this demo at [issue](https://github.com/AgoraIO-Usecase/AgoraChat-android/issues)

## License

The MIT License (MIT).
