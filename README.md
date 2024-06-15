# MinusOne Music

MinusOne Cloud Music (减一云音乐, also _MinusOne Music_ as app name) is a lite third-party NCM app for Android.

In developing this app, I will explore how to create a more complex app. The focus is not on creativity but on the technical aspects of implementing core features of NCM using relatively modern approaches.

Note: I am very inexperienced and this app is at a very early stage of development. Current progress by screens:

- Well Implemented

  - 我的

    - **我的—音乐**

  - 播放

    - **迷你播放器**
    - **播放页**
    - **播放列表（即歌单）**

  - 发现
    - **每日推荐**

- Partial Implementation

  - **密码登录/退出登录**
  - 我的
    - **最近播放—歌曲**
    - **云盘**
    - **本地音乐**
    - **听歌排行**
  - 发现
    - **发现首页（顶部入口、私人雷达、排行榜）**
    - **排行榜**
  - **搜索（“综合”Tab之“单曲”）**
  - **评论（Hottest Page 1）**

> - 所述界面一般不包括二级界面等
> - Other UI elements, pages or functionalities are nowhere near completed although some drafts exist.

_**The project is currently in [maintenance](#wtm) mode**, meaning no major new features will be implemented in the short term. However, its development doesn't stop and I'm open to considering user requests that are not overly complex._

> - Focus has now shifted a little bit away from mobile Android dev to other areas.
> - Occasionally, I will also code for [doubean](https://github.com/Bumblebee202111/doubean-public).

## Features

It will provide a 99% pure music experience with basic support of ~~social features~~ (not implemented yet).

- **-1 (jiǎn yī)!** No ads. No podcasts. No square. Fewer VIP UI elements. Tailored UI content. Incredibly small APK size.

  > [Here](#intd) are important non-to-dos

  - Core functionalities and UI retained, but only on a best-effort basis.
  - Non additive or subtractive modifications. Some convenient UI/behavior changes can be toggled in Settings (not implemented yet).
  - No extra features.

- Music playback.
- _Natural_ (without special adaptations) tablet/landscape support.
- Direct up-to-date NCM service provided. You will see a lot of new interfaces that haven't been discovered by any other developer.
- Uses `View`-based system & Single Activity architecture.
- Uses `deviceId` as the seed to _pseudo_-randomly generate device specs for privacy.
- Is NOT modified from the decompiled official NCM app although the two apps look very similar.
- Does NOT hack music resources.

## Notices

It is strongly encouraged to use the official NCM app rather than third-party ones like mine. If you insist in using my app, please agree on the following restrictions, which can change over time:

- The official app must be installed and co-exist with it.
- The docs are written in broken English.
- The app is almost fully **closed source**. (Maintaining an extra semi-open code base would lead to heavy burden!) hence the simple public repo. If you don't trust me, you can reverse-engineer the app. If interested in some non-NCM-bound code, feel free to request it.

### Your do's and don'ts

- Please use it only for personal learning purposes.
- Please don't share it on other websites/apps without permission.

## Screenshot

<img src="docs\screenshots\Screenshot_main.png" alt="Main" width=160 /><img src="docs\screenshots\Screenshot_discover.png" alt="Discover" width=160 /> <img src="docs\screenshots\Screenshot_top_lists.png" alt="Top lists" width=160 />
<img src="docs\screenshots\Screenshot_now_playing_main.png" alt="Now playing (main)" width=160 /> <img src="docs\screenshots\Screenshot_now_playing_lyrics.png" alt="Now playing (lyrics)" width=400/> <img src="docs\screenshots\Screenshot_comments_landscape.png" alt="Comments (landscape)" width=320 />
<img src="docs\screenshots\Screenshot_playlist.png" alt="Playlist" width=160 /> <img src="docs\screenshots\Screenshot_daily_recommend.png" alt="Daily recommend" width=160 /><img src="docs\screenshots\Screenshot_cloud_disk_and_player_playlist_dialog.png" alt="Cloud disk and player playlist dialog" width=240 /> <img src="docs\screenshots\Screenshot_local_music_and_player_playlist_dialog.png" alt="Local music and player playlist dialog" width=240 /> <img src="docs\screenshots\Screenshot_listen_rank.png" alt="Listen rank" width=160 /> <img src="docs\screenshots\Screenshot_search_landscape.png" alt="Search (landscape)" width=320 />

## :coffee:

:heart:

## To-dos

- <span id="wtm">What to maintain</span>
  - Minor adjustments
    - ...
  - Code optimizations
  - User requests are open
- Dark theme support
- Basic drawer
- Update UI for latest NCM
  - Discover
  - Mini player bar
  - Player screen
  - Should be delayed until at least NCM 9.1.10 when the official UI is more "stable"
- Release alpha on arrival of new star/issue
- Play log (need help)
- New screens / major features
  - Personal FMs
  - Friends
  - MVs
  - Playlist Square (uncertain if it's necessary)
  - Statusbar lyrics
- Player & playlist UI
  - Mini Player Bar
    - Swipe
    - Hide like `PlayControlView`
    - Fix top shadow
  - Transition between Mini Player Bar & Now Playing
  - Now Playing
    - Use light status bar text
    - Follow
    - Provide bottom volume bar dialog (instead of showing sytem volume toast) for systems of lower versions (where the built-in output switcher dialog is unavailable).
    - Use LinearLayout with "weight" things for control buttons
    - Disk rotation like https://music.163.com/m/song
  - Playlist UI items
    - Actions
    - Cloud/favorite/试听(trial)/unavailable UI state
  - Link local/downloaded songs with remote ones
    - Lyrics and cover matching
  - On-boarding
  - Show current audio qualities
- Player
  - Continue the work of moving Player to VM
  - Handle fetch error
- Search
  - Hint
- Mine
  - Optimize dragonballs
  - Add shadow for profile background
  - Top crop profile background
  - Fix top margin of user profile
- Arch
  - Use `Channel` to manage Toast-s
  - Sync user data with `WorkManager`
  - More caching when `RemoteMediator` becomes non-experimental
    - Playlist, lyrics ...
- Settings
  - About
  - Link of APK of latest NCM
- Login
  - UI
- Share
- Network: More elegant
  - My own CookieJar implementation
  - Custom param annotation
  - Consider removing the `NetworkDataSource` wrapper
  - Fully deprecate NCMB
- The UI is generally not interactive enough
- Rearrange UI for landscape like NCM
- Download: advanced
- Login: SMS captcha login
- Firebase Crashlytics
- Compose: Write non-NCM screens in Compose
- Qualification: Add quiz activity/app based on JetSurvey

### <span id="intd">Important non-to-dos</span>

- Search Tabs other than the default, search pagination
- Custom default audio quality (320 kbps is preferred when making requests)
- Support 32-bit systems or ones with android versions lower than 6.0 Marshmallow (API level 23)
- Advanced cover disk
- Add 底部导航自定义
  - Since it loses the official support

## Libraries used

- [Foundation][foundation]
  - [AppCompat][appcompat]
  - [Android KTX][android-ktx]
  - [Test][test] \(TODO\)
- [Architecture][arch]
  - [Data Binding][data-binding]
  - [Lifecycles][lifecycle]
  - [Navigation][navigation]
  - [Paging][paging]
  - [Room][room]
  - [ViewModel][viewmodel]
  - [WorkManager][workmanager] \(TODO\)
  - Data store & SP
- [UI][ui]
  - [Animations & Transitions][animation]
  - [Fragment][fragment]
  - [Layout][layout]
- Behavior
  - [Notifications][notifications]
  - Media 3
- Third party and miscellaneous libraries
  - javax.crypto
  - [Retrofit][retrofit]
  - [Glide][glide]
  - [Kotlin Coroutines][kotlin-coroutines]
  - Moshi
  - PersistentCookieJar

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
[workmanager]: https://developer.android.com/topic/libraries/architecture/workmanager
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

- Countless NCM app/service repos, including a well-known repo that has recently been shut down
- Media3 references:
  - uamp-media3
  - horologist/media
  - socialite

## Misc.

<details>
    <summary>Misc.</summary>
<p>
    - Initially, I named it as 'NaCl'. Letters extracted from that of the official NCE app, it indicated that this third-party variant would be a lite alternative to the official one, due to technical skill limitations and omission of useless features. However, I found that Salt Music, another existing well-designed music app had a similar name. Now my app has been renamed to MinusOne Cloud Music, matching both of the '-1'
    slogan and the name format of NCM. 
    </p>
<p>
    - Security and cryptography. We do use the very basic of NDK, which is apparently redundant in non-toy apps but worth for learning purposes.
    </p>
</details>
