<h1 align="left">Firebase Kotlin SDK <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/gitliveapp/firebase-kotlin-sdk?style=flat-square"> <a href="https://git.live"><img src="https://img.shields.io/endpoint?style=flatsquare&url=https%3A%2F%2Fteamhub-dev.web.app%2Fbadge%3Forg%3DGitLiveApp%26repo%3Dfirebase-kotlin-sdk"></a></h1>
<img align="left" width="75px" src="https://avatars2.githubusercontent.com/u/42865805?s=200&v=4"> 
  <b>Built and maintained with 🧡 by <a href="https://git.live">GitLive</a></b><br/>
  <i>Development teams merge faster with GitLive</i><br/>
<br/>
<br/>
The Firebase Kotlin SDK is a Kotlin-first SDK for Firebase. It's API is similar to the 
<a href="https://firebase.github.io/firebase-android-sdk/reference/kotlin/firebase-ktx/">Firebase Android SDK Kotlin Extensions</a> 
but also supports multiplatform projects, enabling you to use Firebase directly from your common source targeting 
<strong>iOS</strong>, <strong>Android</strong>, <strong>Desktop</strong> or <strong>Web</strong>, enabling the use of 
Firebase as a backend for <a href="https://www.jetbrains.com/lp/compose-multiplatform/">Compose Multiplatform</a>, for example.

## Available libraries

The following libraries are available for the various Firebase products.

| Service or Product	                                                             | Gradle Dependency                                                                                                              | API Coverage                                                                                                                                                             |
|---------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Analytics](https://firebase.google.com/docs/analytics)                         | [`dev.gitlive:firebase-analytics:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-analytics/2.1.0/pom)                   | [![80%](https://img.shields.io/badge/-80%25-green?style=flat-square)](/firebase-auth/src/commonMain/kotlin/dev/gitlive/firebase/auth/auth.kt)                            |
| [Authentication](https://firebase.google.com/docs/auth)                         | [`dev.gitlive:firebase-auth:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-auth/2.1.0/pom)                   | [![80%](https://img.shields.io/badge/-80%25-green?style=flat-square)](/firebase-auth/src/commonMain/kotlin/dev/gitlive/firebase/auth/auth.kt)                            |
| [Realtime Database](https://firebase.google.com/docs/database)                  | [`dev.gitlive:firebase-database:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-database/2.1.0/pom)           | [![70%](https://img.shields.io/badge/-70%25-orange?style=flat-square)](/firebase-database/src/commonMain/kotlin/dev/gitlive/firebase/database/database.kt)               |
| [Cloud Firestore](https://firebase.google.com/docs/firestore)                   | [`dev.gitlive:firebase-firestore:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-firestore/2.1.0/pom)         | [![60%](https://img.shields.io/badge/-60%25-orange?style=flat-square)](/firebase-firestore/src/commonMain/kotlin/dev/gitlive/firebase/firestore/firestore.kt)            |
| [Cloud Functions](https://firebase.google.com/docs/functions)                   | [`dev.gitlive:firebase-functions:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-functions/2.1.0/pom)         | [![80%](https://img.shields.io/badge/-80%25-green?style=flat-square)](/firebase-functions/src/commonMain/kotlin/dev/gitlive/firebase/functions/functions.kt)             |
| [Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)             | [`dev.gitlive:firebase-messaging:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-messaging/2.1.0/pom)         | [![1%](https://img.shields.io/badge/-10%25-orange?style=flat-square)](/firebase-messaging/src/commonMain/kotlin/dev/gitlive/firebase/messaging/messaging.kt)           |
| [Cloud Storage](https://firebase.google.com/docs/storage)                       | [`dev.gitlive:firebase-storage:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-storage/2.1.0/pom)             | [![40%](https://img.shields.io/badge/-40%25-orange?style=flat-square)](/firebase-storage/src/commonMain/kotlin/dev/gitlive/firebase/storage/storage.kt)                  |
| [Installations](https://firebase.google.com/docs/projects/manage-installations) | [`dev.gitlive:firebase-installations:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-installations/2.1.0/pom) | [![90%](https://img.shields.io/badge/-90%25-green?style=flat-square)](/firebase-installations/src/commonMain/kotlin/dev/gitlive/firebase/installations/installations.kt) |
| [Remote Config](https://firebase.google.com/docs/remote-config)                 | [`dev.gitlive:firebase-config:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-config/2.1.0/pom)               | [![20%](https://img.shields.io/badge/-20%25-orange?style=flat-square)](/firebase-config/src/commonMain/kotlin/dev/gitlive/firebase/remoteconfig/FirebaseRemoteConfig.kt) |
| [Performance](https://firebase.google.com/docs/perf-mon)                        | [`dev.gitlive:firebase-perf:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-perf/2.1.0/pom)                   | [![1%](https://img.shields.io/badge/-10%25-orange?style=flat-square)](/firebase-perf/src/commonMain/kotlin/dev/gitlive/firebase/perf/performance.kt)                      |
| [Crashlytics](https://firebase.google.com/docs/crashlytics)                     | [`dev.gitlive:firebase-crashlytics:2.1.0`](https://search.maven.org/artifact/dev.gitlive/firebase-crashlytics/2.1.0/pom)     | [![80%](https://img.shields.io/badge/-10%25-orange?style=flat-square)](/firebase-crashlytics/src/commonMain/kotlin/dev/gitlive/firebase/crashlytics/crashlytics.kt)       |

Is the Firebase library or API you need missing? [Create an issue](https://github.com/GitLiveApp/firebase-kotlin-sdk/issues/new?labels=API+coverage&template=increase-api-coverage.md&title=Add+%5Bclass+name%5D.%5Bfunction+name%5D+to+%5Blibrary+name%5D+for+%5Bplatform+names%5D) to request additional API coverage or be awesome and [submit a PR](https://github.com/GitLiveApp/firebase-kotlin-sdk/fork)

## Kotlin-first design

Unlike the Kotlin Extensions for the Firebase Android SDK this project does not extend a Java based SDK so we get the full power of Kotlin including coroutines and serialization!

<h3><a href="https://kotlinlang.org/docs/tutorials/coroutines/async-programming.html#coroutines">Suspending functions</a></h3>

Asynchronous operations that return a single or no value are represented by suspending functions in the SDK instead of callbacks, listeners or OS specific types such as [Task](https://developer.android.com/reference/com/google/android/play/core/tasks/Task), for example:

```kotlin
suspend fun signInWithCustomToken(token: String): AuthResult
```

It is important to remember that unlike a callback based API, waiting for suspending functions to complete is implicit and so if you don't want to wait for the result you can `launch` a new coroutine:

```kotlin
//TODO don't use GlobalScope
GlobalScope.launch {
  Firebase.auth.signOut()
}
```

<h3><a href="https://kotlinlang.org/docs/reference/coroutines/flow.html">Flows</a></h3>

Asynchronous streams of values are represented by Flows in the SDK instead of repeatedly invoked callbacks or listeners, for example:

```kotlin
val snapshots: Flow<DocumentSnapshot>
```

The flows are cold, which means a new listener is added every time a terminal operator is applied to the resulting flow. A buffer with the [default size](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/-channel/-b-u-f-f-e-r-e-d.html) is used to buffer values received from the listener, use the [`buffer` operator](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/buffer.html) on the flow to specify a user-defined value and to control what happens when data is produced faster than consumed, i.e. to control the back-pressure behavior. Often you are only interested in the latest value received, in this case you can use the [`conflate` operator](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/conflate.html) to disable buffering.

The listener is removed once the flow [completes](https://kotlinlang.org/docs/reference/coroutines/flow.html#flow-completion) or is [cancelled](https://kotlinlang.org/docs/reference/coroutines/flow.html#flow-cancellation).

<h3><a href="https://github.com/Kotlin/kotlinx.serialization">Serialization</a></h3>

The official Firebase SDKs use different platform-specific ways to support writing data with and without custom classes in [Cloud Firestore](https://firebase.google.com/docs/firestore/manage-data/add-data#custom_objects), [Realtime Database](https://firebase.google.com/docs/database/android/read-and-write#basic_write) and [Functions](https://firebase.google.com/docs/functions/callable).

The Firebase Kotlin SDK uses Kotlin serialization to read and write custom classes to Firebase. To use Kotlin serialization in your project add the plugin to your gradle file:

```groovy
plugins {
    kotlin("multiplatform") version "1.9.20" // or kotlin("jvm") or any other kotlin plugin
    kotlin("plugin.serialization") version "1.9.20"
}
```

Then mark your custom classes `@Serializable`:

```kotlin
@Serializable
data class City(val name: String)
```

Instances of these classes can now be passed [along with their serializer](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#introduction-to-serializers) to the SDK:

```kotlin
db.collection("cities").document("LA").set(City.serializer(), city) { encodeDefaults = true }
```

The `buildSettings` closure is optional and allows for configuring serialization behaviour. 

Setting the `encodeDefaults` parameter is optional and defaults to `true`, set this to false to omit writing optional properties if they are equal to theirs default values.
Using [@EncodeDefault](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-core/kotlinx.serialization/-encode-default/) on properties is a recommended way to locally override the behavior set with `encodeDefaults`.

You can also omit the serializer if it can be inferred using `serializer<KType>()`. 
To support [contextual serialization](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md#contextual-serialization) or [open polymorphism](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#open-polymorphism) the `serializersModule` can be overridden in the `buildSettings` closure:

```kotlin
@Serializable
abstract class AbstractCity {
    abstract val name: String
}

@Serializable
@SerialName("capital")
data class Capital(override val name: String, val isSeatOfGovernment: Boolean) : AbstractCity()

val module = SerializersModule {
    polymorphic(AbstractCity::class, AbstractCity.serializer()) {
        subclass(Capital::class, Capital.serializer())
    }
}

val city = Capital("London", true)
db.collection("cities").document("UK").set(AbstractCity.serializer(), city) { 
    encodeDefaults = true
    serializersModule = module
    
}
val storedCity = db.collection("cities").document("UK").get().data(AbstractCity.serializer()) {
    serializersModule = module
}
```

<h4><a href="https://firebase.google.com/docs/firestore/manage-data/add-data#server_timestamp">Server Timestamp</a></h3>

[Firestore](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/firestore/FieldValue?hl=en#serverTimestamp()) and the [Realtime Database](https://firebase.google.com/docs/reference/android/com/google/firebase/database/ServerValue#TIMESTAMP) provide a sentinel value you can use to set a field in your document to a server timestamp. So you can use these values in custom classes:

```kotlin
@Serializable
data class Post(
    // In case using Realtime Database.
    val timestamp = ServerValue.TIMESTAMP,
    // In case using Cloud Firestore.
    val timestamp: Timestamp = Timestamp.ServerTimestamp,
    // or
    val alternativeTimestamp = FieldValue.serverTimestamp,
    // or
    @Serializable(with = DoubleAsTimestampSerializer::class),
    val doubleTimestamp: Double = DoubleAsTimestampSerializer.serverTimestamp
)
```

In addition `firebase-firestore` provides [GeoPoint] and [DocumentReference] classes which allow persisting
geo points and document references in a native way:

```kotlin
@Serializable
data class PointOfInterest(
    val reference: DocumentReference, 
    val location: GeoPoint
)
val document = PointOfInterest(
    reference = Firebase.firestore.collection("foo").document("bar"),
    location = GeoPoint(51.939, 4.506)
)
```

<h4>Polymorphic serialization (sealed classes)</h4>

This sdk will handle polymorphic serialization automatically if you have a sealed class and its children marked as `Serializable`. It will include a `type` property that will be used to discriminate which child class is the serialized.

You can change this `type` property by using the `@FirebaseClassDiscrminator` annotation in the parent sealed class:

```kotlin
@Serializable
@FirebaseClassDiscriminator("class")
sealed class Parent {
    @Serializable
    @SerialName("child")
    data class Child(
        val property: Boolean
    ) : Parent
}
```

In combination with a `SerialName` specified for the child class, you have full control over the serialized data. In this case it will be:

```json
{
  "class": "child",
  "property": true
}
```

<h4>Serialization of Updates</h4>
Firestore contains update methods that allow for multiple fields to be updated at the same time. 
This sdk offers special update methods that allow for applying custom serialization to each individual field though an update builder.
Where an `update` method exists, an `updateFields` method will also be available. In this, each value can have its serializer customized:

```kotlin
documentRef.updateFields {
    "field" to "value"
    // Set the value of otherField to "1" using a custom Serializer
    "otherField".to(IntAsStringSerializer(), 1)
    
    // Overwrite build settings. All fields added within this block will have these build settings applied
    withEncodeSettings {
        encodeDefaults = true
        serializersModule = module
        "city" to abstractCity
    }
}
```

Similarly, the `Query` methods `startAt`/`startAfter`/`endAt`/`endBefore` have an alternative method in `startAtFieldValues`/`startAfterFieldValues`/`endAtFieldValues`/`endBeforeFieldValues`

```kotlin
query.orderBy("field", "otherField", "city").startAtFieldValues { // similar syntax for startAfter/endAt/endBefore
    add("Value")

    // Starts at "1" for the otherField value
    add(1, IntAsStringSerializer())

    // Overwrite build settings. All field values added within this block will have these build settings applied
    withEncodeSettings {
        encodeDefaults = true
        serializersModule = module
        add(abstractCity)
    }
}
```

<h3><a href="https://kotlinlang.org/docs/reference/functions.html#default-arguments">Default arguments</a></h3>

To reduce boilerplate, default arguments are used in the places where the Firebase Android SDK employs the builder pattern:
```kotlin
UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
        .setDisplayName("Jane Q. User")
        .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
        .build()

user.updateProfile(profileUpdates)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User profile updated.")
                }
            }
        })

//...becomes...

user.updateProfile(displayName = "Jane Q. User", photoURL = "https://example.com/jane-q-user/profile.jpg")
```

<h3><a href="https://kotlinlang.org/docs/functions.html#infix-notation">Infix notation</a></h3>

To improve readability and reduce boilerplate for functions such as the Cloud Firestore query operators are built with infix notation:

```kotlin
citiesRef.whereEqualTo("state", "CA")
citiesRef.whereArrayContains("regions", "west_coast")
citiesRef.where(Filter.and(
    Filter.equalTo("state", "CA"),
    Filter.or(
        Filter.equalTo("capital", true),
        Filter.greaterThanOrEqualTo("population", 1000000)
    )
))

//...becomes...

citiesRef.where { "state" equalTo "CA" }
citiesRef.where { "regions" contains "west_coast" }
citiesRef.where {
    all(
        "state" equalTo "CA",
        any(
            "capital" equalTo true,
            "population" greaterThanOrEqualTo 1000000
        )
    )
}
```

<h3><a href="https://kotlinlang.org/docs/reference/operator-overloading.html">Operator overloading</a></h3>

In cases where it makes sense, such as Firebase Functions HTTPS Callable, operator overloading is used:

```kotlin
    val addMessage = functions.getHttpsCallable("addMessage")
    //In the official android Firebase SDK this would be addMessage.call(...)
    addMessage(mapOf("text" to text, "push" to true))
```

## Multiplatform

The Firebase Kotlin SDK provides a common API to access Firebase for projects targeting *iOS*, *Android*, *JVM* and *JS* meaning you can use Firebase directly in your common code. Under the hood, the SDK achieves this by binding to the respective official Firebase SDK for each supported platform.

It uses the <a href="https://github.com/GitLiveApp/firebase-java-sdk">Firebase Java SDK</a> to support the JVM target. The library requires [additional initialization](https://github.com/GitLiveApp/firebase-java-sdk?tab=readme-ov-file#initializing-the-sdk) compared to the official Firebase SDKs.

### Accessing the underlying Firebase SDK

In some cases you might want to access the underlying official Firebase SDK in platform specific code, for example when the common API is missing the functionality you need. For this purpose each class in the SDK has `android`, `ios` and `js` extension properties that hold the equivalent object of the underlying official Firebase SDK. For *JVM*, as the `firebase-java-sdk` is a direct port of the Firebase Android SDK, is it also accessed via the `android` property.

These properties are only accessible from the equivalent target's source set. For example to disable persistence in Cloud Firestore on Android you can write the following in your Android specific code (e.g. `androidMain` or `androidTest`):

```kotlin
  Firebase.firestore.android.firestoreSettings = FirebaseFirestoreSettings.Builder(Firebase.firestore.android.firestoreSettings)
          .setPersistenceEnabled(false)
          .build()
```

### Running on Android

On android, some modules (`config`) require you to enable [Core library desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring) if you have a *minSDK lower than API 26*.

### Running on iOS

On iOS the official [Firebase iOS SDK](https://github.com/firebase/firebase-ios-sdk) in not linked as a transitive dependency. Therefore, any project using this SDK needs to link the actual Firestore SDK as well. This can be done through your preferred installation method (Cocoapods/SPM).

Similarly, tests require linking as well. Make sure to add the required frameworks to the search path of your test targets. This can be done by specifying a `cocoapods` block in your `build.gradle`:
```kotlin
cocoapods {
   pod("FirebaseCore") // Repeat for Firebase pods required by your project, e.g FirebaseFirestore for the `firebase-firestore` module.
}
```

## Contributing
If you'd like to contribute to this project then you can fork this repository. 
You can build and test the project locally.
1. Open the project in IntelliJ IDEA.
2. Install cocoapods via `sudo gem install -n /usr/local/bin cocoapods`
3. Install the GitLive plugin into IntelliJ
4. After a gradle sync then run `publishToMavenLocal`

### Testing
To run the tests you can use the following gradle tasks:

`./gradlew connectedAndroidTest` (an emulator needs to be running)

`./gradlew iosSimulatorArm64Test` (On Apple Chip) `./gradlew iosX64Test` (On Intel Chip)

`./gradlew jsNodeTest`

`./gradlew jvmTest`

For some tests you need to have the firebase emulator suite running. After installing them you can run the following command inside the `test` directory:

`firebase emulators:start`

### Documentation
For every publicly available class or function there needs to be documentation written in the [KDoc syntax](https://kotlinlang.org/docs/kotlin-doc.html).
We publish a new version of the documentation after every release and can be found [here](https://gitliveapp.github.io/firebase-kotlin-sdk/)

### Validation
To ensure changes to the public API are well documented, this library validates its binary API. If you make changes to the API, make sure to run

`./gradlew apiDump`

### Code style
This library uses the [Intellij Kotlin code style](https://www.jetbrains.com/help/idea/code-style-kotlin.html). Run the linter to make sure you follow these styles.

`./gradlew formatKotlin` to format to the proper style
`./gradlew lintKotlin` to validate the correct style is used

### Compatibility with the official [Firebase Android SDK](https://github.com/firebase/firebase-android-sdk)

When this project began, the official Firebase Android SDK was a pure java library and the separate Kotlin extensions (KTX) module consisted of only a few extensions providing some syntactic sugar, for example `Firebase.firestore` instead of `Firebase.getInstance(),` which we naturally copied for the Firebase Kotlin SDK.

But with the majority of the Android SDK being designed for Java, the Kotlin SDK was primarily guided by our [Kotlin-first design](https://github.com/GitLiveApp/firebase-kotlin-sdk/?tab=readme-ov-file#kotlin-first-design) principles.

More recently, with the official SDK for Android providing better support for Kotlin and the inclusion of the new Kotlin-friendly features direct in the main modules, the API differences between the official SDK and this project are likely to start to blur. Therefore, in particular for developers porting android code to multiplatform, one of our goals going forward will be API compatibility with the Android SDK where possible.

For contributors this means following these points when adding new code to the public API of this project:
- **Match the [Android SDKs API](https://firebase.google.com/docs/reference/kotlin/packages).** When adding new API coverage use the Android SDK as the guide on what the public API should be in regard to naming, parameters etc. The goal here is *near binary compatibility*, meaning code consuming the Android SDK compiles *as is* with the Kotlin SDK after just changing the package imports from `com.google` to `dev.gitlive`.
- **Follow our [Kotlin-first design](https://github.com/GitLiveApp/firebase-kotlin-sdk/?tab=readme-ov-file#kotlin-first-design) principles when needed.** If the API you are adding coverage for is new, and it's Kotlin-first in the Android SDK, then you can simply just match the Android SDKs API as described in the first point, but if it's an older Java-first API then ideally we would include an identical API for API compatibility *plus* a Kotlin-first overload. A good example for this is where the Builder pattern is employed in the Android SDK, here we can follow [this Kotlin-first design principle](https://github.com/GitLiveApp/firebase-kotlin-sdk/?tab=readme-ov-file#default-arguments) and provide both methods, one taking the options created with the builder and an overload with default arguments to avoid the builder boilerplate for developers not porting an existing android code base.
