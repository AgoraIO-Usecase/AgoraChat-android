# Agora chat demo

This repository will help you learn how to use Agora chat SDK to implement a simple chat android application, like whatsapp or wechat.

With this sample app, you can:

- Login chat server
- Start a chat
- Manage conversation list
- Add contacts
- Join group chats
- Join chat rooms
- Add your contacts to your blacklist
- Send various types of messages, Such as: text, expression, picture, voice, file and so on
- Logout

## Prerequisites
* Make sure you have made the preparations mentioned in the [Agora Chat SDK quickstart](https://docs.agora.io/en/agora-chat/get-started/get-started-sdk?platform=android).
* Prepare the development environment:
    * Java Development Kit (JDK)
    * Android Studio 3.6 or later
## Run the sample project

Follow these steps to run the sample project:\
### 1. Clone the repository to your local machine.
```java
    git clone git@github.com:AgoraIO-Usecase/AgoraChat-android.git
```

### 2. Open the Android project with Android Studio.

### 3. Configure keys.
Set your appkey applied from [Agora Developer Console](https://console.agora.io/) before calling ChatClient#init().
```java
ChatOptions options = new ChatOptions();
// Set your appkey
options.setAppKey("Your appkey");
...
//initialization
ChatClient.getInstance().init(applicationContext, options);
```
For details, see the [prerequisites](https://docs.agora.io/en/agora-chat/get-started/get-started-sdk?platform=android) in Agora Chat SDK Guide.

## Contact Us
- You can find full API document at [Document Center](https://docs.agora.io/en/agora-chat/overview/product-overview?platform=android)
- You can file bugs about this demo at [issue](https://github.com/AgoraIO-Usecase/AgoraChat-android/issues)

## License
The MIT License (MIT).
