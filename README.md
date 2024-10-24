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
  - Java Development Kit (JDK)
  - Android Studio Flamingo | 2022.2.1 or later

## Run the sample project

Follow these steps to run the sample project:

### 1. Clone the repository to your local device

```java
    git clone git@github.com:AgoraIO-Usecase/AgoraChat-android.git
```

### 2. Open the Android project with Android Studio

### 3. Configure keys

Set your appkey obtained from the [Agora Console](https://console.agora.io/) before calling ChatClient#init().

```kotlin
val chatOptions = ChatOptions().apply {
    // Set your appkey
    appKey = "Your appkey"
    ...
}
// initialization
ChatClient.getInstance().init(context, chatOptions)
```

For details, see the [prerequisites](https://docs.agora.io/en/agora-chat/get-started/get-started-sdk?platform=android) in Agora Chat SDK Guide.

## Contact Us

- You can find full API document at [Document Center](https://docs.agora.io/en/agora-chat/overview/product-overview?platform=android)
- You can file bugs about this demo at [issue](https://github.com/AgoraIO-Usecase/AgoraChat-android/issues)

## License

The MIT License (MIT).
