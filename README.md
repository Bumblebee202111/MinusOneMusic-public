# MinusOne Music

**MinusOne Cloud Music** (减一云音乐, app name _MinusOne Music_) is a lite, third-party NCM app for Android.

This project explores building complex apps by implementing core NCM features using modern Android approaches. Focus is on technical implementation rather than creativity.

Note: Early development stage. Historically, the existing code is not good. Current progress by screens:

- Implemented

  - 我的

    - **我的—音乐**

  - 播放

    - **迷你播放器**
    - **播放页**
    - **播放列表（即歌单）**

  - 发现
    - **每日推荐**

- Partial Implementation

  - **验证码登录**/**密码登录**[broken]/**退出登录**
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

> - 所列界面一般不含二级界面
> - Other UI elements, pages or functionalities are nowhere near completed although some drafts exist.

**Known issues:**

## Features

Provides a 99% pure music experience with basic support of ~~social features~~ (not implemented yet).

- **-1 (jiǎn yī)!** 

  - No ads/podcasts/square

  - Minimal VIP UI elements

  - Tailored UI content

  - Small APK size

  - Core functionalities and UI retained, but only on a best-effort basis
  - Non-additive or subtractive modifications (some UI/behavior changes will be togglable in Settings)
  - No extra features
> [Here](#intd) are important non-to-dos

- Music playback
- Tablet/landscape support without special adaptations
- Direct NCM API integration (new undiscovered endpoints)
- `View`-based system & Single Activity architecture
- **NOT** modified from the decompiled official app (despite visual similarities)
- Does NOT hack music resources

## Notices

It is strongly encouraged to use the official NCM app. If using this app, you agree to:

- Must coexist with official NCM app
- **Closed source** (reverse-engineer if needed)
  - Non-NCM code available on request
  - However, future selected public repo sync planned
- Documentation is in broken English

### Your do's and don'ts

- Use only for personal learning purposes
- Do not share without permission

## Screenshots

<img src="docs\screenshots\Screenshot_main.png" alt="Main" width=160 /><img src="docs\screenshots\Screenshot_discover.png" alt="Discover" width=160 /> <img src="docs\screenshots\Screenshot_top_lists.png" alt="Top lists" width=160 />
<img src="docs\screenshots\Screenshot_now_playing_main.png" alt="Now playing (main)" width=160 /> <img src="docs\screenshots\Screenshot_now_playing_lyrics.png" alt="Now playing (lyrics)" width=400/> <img src="docs\screenshots\Screenshot_comments_landscape.png" alt="Comments (landscape)" width=320 />
<img src="docs\screenshots\Screenshot_playlist.png" alt="Playlist" width=160 /> <img src="docs\screenshots\Screenshot_daily_recommend.png" alt="Daily recommend" width=160 /><img src="docs\screenshots\Screenshot_cloud_disk_and_player_playlist_dialog.png" alt="Cloud disk and player playlist dialog" width=240 /> <img src="docs\screenshots\Screenshot_local_music_and_player_playlist_dialog.png" alt="Local music and player playlist dialog" width=240 /> <img src="docs\screenshots\Screenshot_listen_rank.png" alt="Listen rank" width=160 /> <img src="docs\screenshots\Screenshot_search_landscape.png" alt="Search (landscape)" width=320 />

## :coffee:

:heart:

## To-dos

- Coil
- Code optimization
  - Handle common errors gracefully
- Introduce Compose for non-core player UI
  - Also migrate RecyclerView to LazyList
  - Implement official white/black themes
  - Basic drawer
- Network: More elegant
  - Track login stability & remove non EAPI services
  - My own CookieJar implementation
  - Custom param annotation
  - Consider removing the `NetworkDataSource` wrapper
  - Ktor?
- Request compressed images
- Play log (help needed)
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
    - Light status bar text
    - Follow
    - Volume control dialog for legacy systems
    - LinearLayout weight distribution for controls
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
  - More caching: Playlist, lyrics ...
- Update UI for latest NCM
  - Discover
  - Mini player bar
  - Player screen
  - “网易云听劝”
- Settings
  - About
  - Link of APK of latest NCM
- Login
  - UI
- Share
- The UI is generally not interactive enough
- Rearrange UI for landscape like NCM
- Download: advanced
- Login: SMS captcha login
- Firebase Crashlytics

### <span id="intd">Important non-to-dos</span>

- Additional search tabs/pagination
- Custom default audio quality (320 kbps is preferred when making requests)
- Advanced cover disk

## Tech Stack

- [Foundation][foundation]: [Android KTX][android-ktx]
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
  - Coil
  - [Kotlin Coroutines][kotlin-coroutines]
  - Moshi
  - PersistentCookieJar

[foundation]: https://developer.android.com/jetpack/components
[android-ktx]: https://developer.android.com/kotlin/ktx
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
[kotlin-coroutines]: https://kotlinlang.org/docs/reference/coroutines-overview.html

## Utilities used

- jadx-gui

- HTTPCanary

- Crypto tools

## References

- NCM app/service repos, including a well-known one
- Media3 references:
  - uamp-media3
  - horologist/media
  - socialite
  
## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for the full license text.

## Misc.

<details>
    <summary>Misc.</summary>
<p>
    - Initially, I named it as 'NaCl'. Letters extracted from that of the official NCM app, it indicated that this third-party variant would be a lite alternative to the official one, due to technical skill limitations and omission of useless features. However, I found that Salt Music, another existing well-designed music app had a similar name. Now my app has been renamed to MinusOne Cloud Music, matching both of the '-1' slogan and the name format of NCM. 
    </p>
<p>
    - Security and cryptography. We do use the very basic of NDK, which is apparently redundant in non-toy apps but worth for learning purposes.
    </p>
</details>
