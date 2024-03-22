# MinusOne Music

MinusOne Cloud Music (减一云音乐, also *MinusOne Music* as app name) is a third-party NCM app for Android.

In developing this app, we (actually me alone) will explore how to develop a more complicated app. This project is not going to be very creative. Instead, we will focus on the technical details of how to implement core features of NCM with modern approaches. 

Meanwhile, we are attempting to make it competent for the daily use of not only our team members but also loyal NCM fans.

Note this app is at a very early stage of development and we are very inexperienced. Current progress: 

- 我的—音乐（界面与功能）
- 迷你播放器（界面与功能）
- 播放页—底部控制栏（仅功能）
- 密码登录/退出登录（仅功能）

Other UI elements/pages or functionalities are nowhere near completed even if some drafts can be found now.

## Features

It will bring you 99% pure music experience with basic support of social features.

- **-1 (jiǎn yī)!** No ads. No podcast. No square. Fewer VIP UI elements. Tailored UI content. Incredibly small APK size.
- What are kept. Core functionalities and UI kept but only on best effort
- (Non additive or subtractive) Modifications. Some limited number of convenient changes to the official UI/behaviors can be toggled in Settings.
- No additions.
- Stateful players with their stateless child views.
- Music playback.
- *Natural* (= without special adaptations) tablet/landscape support.
- Direct up-to-date NCM service provided.
- Uses the good old View-based system, e.g. XML-based.
- Single Activity. If not, two or three in the future.
- Uses `deviceId` as the seed to *pseudo*-randomly generate device specs. 
- Does NOT hack music resources.

## Notices

To strongly encourage the use of official NCM app, we decided that you must be "authorized" to use it. But you can easily gain access to it anyway.
Below are some factors that may hinder you from using it, which can change over time.

- The official app must be installed and co-exist with it.
- The docs are written in broken English.
- The app is almost fully **closed source**. (Maintaining an extra semi-open code base would lead to heavy burden!) So was the simple public repo born. If you don't trust me, simply reverse-engineer the app. If you are interested in some non-NCM-bound code, I can post it somewhere.

### Your do's and don'ts
- Please use it only for proper learning/personal/non-commercial purposes. 
- Please don't share it on other websites/apps without permission.

## To-dos

- Update AS

- Update jadx-gui

- Update and model on latest NCM app

- > To avoid refreshing the UI too often in a short period of time, it's generally better to listen to just `onEvents` and trigger UI updates from there

- [Insets] Better handle insets and stop using `fitsSystemWindows`

- [BNV\] Fix overlapping of icon and title on some devices such as my AVD

- [Now Playing] Volume control, lyrics

- [Now Playing] Song actions

- \[Mini Player\] Swipe

- [Playlist] https://github.com/google/ExoPlayer/issues/8607

- [Playlist] [Player\] Various playlist fragments/dialogs

- [Player] Figure out with what param combination can a song be played, handle fetch error

- [Mini Player\] Hide like `PlayControlView`

- \[Mini Player\] [Now Playing\] Transition

- [Player] Download

- [Player] Continue the work of moving Player to VM

- [Player] Support various audio qualities

- [Player] Playlist paging

- [Settings] Add 底部导航栏自定义

  - Delayed since: it's gone forever in our installed official app; it's not officially supported

- On-boarding

- [Nav] Continue the work of optimizing navigation code

- [Now Playing] UI

- [Search] Add

- [Network] More elegant

  - My own CookieJar implementation
  - Custom param annotation

- [Arch] Clarify the dividing line between network layer and repo layer and consider removing the `NetworkDataSource` wrapper

- [Login] SMS captcha login. Fully deprecate NCMA

- [Arch] For data mapping, should probably use `from`-s instead of `to-`s instead

- [Network] Add ApiResponse wrapper for more precise error tracking

- [Quiz] Add quiz activity/app based on JetSurvey, meanwhile try out a lot of new/Kotlin libraries such as Compose

- [Mine] Add shadow for profile background

- [Mine] Top crop profile background

- [Mine] Fix top margin of user profile

- [Music Player Bar] Fix top shadow

- [MinusTue] A minimum NCM app for exploring Vue3

## Libraries used

* [Foundation][foundation]
    * [AppCompat][appcompat]
    * [Android KTX][android-ktx]
    * [Test][test] \(TODO\)
* [Architecture][arch]
    * [Data Binding][data-binding]
    * [Lifecycles][lifecycle]
    * [Navigation][navigation]
    * [Paging][paging] \(TODO\)
    * [Room][room]
    * [ViewModel][viewmodel]
    * [WorkManager][workmanager] \(TODO\)
    * Data store & SP
* [UI][ui]
    * [Animations & Transitions][animation]
    * [Fragment][fragment]
    * [Layout][layout]
* Behavior
    * [Notifications][notifications]
    * Media 3
* Third party and miscellaneous libraries
    * javax.crypto
    * [Retrofit][retrofit]
    * [Glide][glide]
    * [Kotlin Coroutines][kotlin-coroutines]
    * Moshi
    * PersistentCookieJar

[foundation]: https://developer.android.com/jetpack/components

[appcompat]: https://developer.android.google.cn/jetpack/androidx/releases/appcompat

[android-ktx]: https://developer.android.com/kotlin/ktx

[test]: https://developer.android.com/training/testing/

[arch]: https://developer.android.com/jetpack/arch/

[data-binding]: https://developer.android.com/topic/libraries/data-binding/

[lifecycle]: https://developer.android.com/topic/libraries/architecture/lifecycle

[navigation]: https://developer.android.com/topic/libraries/architecture/navigation/

[paging]: https://developer.android.com/topic/libraries/architecture/paging/v3-overview

[room]: https://developer.android.com/topic/libraries/architecture/room

[viewmodel]: https://developer.android.com/topic/libraries/architecture/viewmodel

[workmanager]:https://developer.android.com/topic/libraries/architecture/workmanager

[ui]: https://developer.android.com/guide/topics/ui

[animation]: https://developer.android.com/training/animation/

[fragment]: https://developer.android.com/guide/components/fragments

[layout]: https://developer.android.com/guide/topics/ui/declaring-layout

[notifications]: https://developer.android.com/develop/ui/views/notifications

[retrofit]: https://square.github.io/retrofit/

[apache-commons]: https://commons.apache.org/

[glide]: https://bumptech.github.io/glide/

[kotlin-coroutines]: https://kotlinlang.org/docs/reference/coroutines-overview.html

## Utilities used

- jadx-gui

- HTTPCanary

- Various online crypto tools

## References

* Countless NCM app/service repos, including a well-known repo that has recently been shut down
* Media3 references:
  * uamp-media3
  * horologist/media
  * socialite

## Misc. 

<details>
    <summary>Misc.</summary>
    - Initially, I named it as 'NaCl'. Letters extracted from that of the official NCE app, it indicated that this third-party variant would be a lite alternative to the official one, due to technical limitations and omission of useless features. However, I found that Salt Music, another existing well-designed music app had a similar name. Now my app has been renamed to MinusOne Cloud Music, matching both of the '-1'
  slogan and the name format of NCM.
- Security and cryptography. We do use the very basic of NDK, which is apparently redundant in non-toy apps but worth for learning purposes.
</details>



