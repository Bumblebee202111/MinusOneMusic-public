Change Log
==========

## Version 1.0.0-beta01

- Possibly support captcha login
  - Password login does not work and is hidden
- Improve request headers
  - Update NCM parameters to 9.2.85
  - Replace pseudo-random device specs with real ones to standardize api requests
- Code optimizations (still terrible) & bug fixes
- Update dependencies


## Version 1.0.0-dev12 (baseline: NCM 9.1.00)

- Top Lists: Hide Look
- Introduce compressed images
  - This massively improves network image experience & saves a lot of network usage
- Update item icon of Listen Rank
- Daily Recommend: Fix list type regression
- Latest UI is not introduced in this version

## Version 1.0.0-dev11 (baseline: NCM 9.0.80)

- Add Listen Rank
- Set MediaSession Activity to allow Notification tapping
- Optimize toolbar navigation icons
  - Update back icons
  - Add missing listener for SearchFragment
- Optimize Discover Private Radars
  - Reduce cover size
  - Port main radar to radar block from pl rcmd block data (the latter block is not shown yet)
- Fix issues when playing empty playlist
- PlaylistFragment: Fix mask not fully masking when collapsed on some devices
- Fix choice of playlist api for my collected playlists
- Fix Recent Play music request param mistake

## Version 1.0.0-dev10 (baseline: NCM 9.0.80)

- Support browsing the first page of the hottest comments of a song

## Version 1.0.0-dev09 (baseline: NCM 9.0.80)

- Add basic remote song search support

## Version 1.0.0-dev08 (baseline: NCM 9.0.80)

- Improve Top Lists which was just draft
- Fix playlist info overlapping with toolbar
- Fix: Add missing click listeners for toolbar navigation icons

## Version 1.0.0-dev07 (baseline: NCM 9.0.80)

- Update DiscoverFragment from draft to basic one
- Fix bottom inset not being handled to correctly display BNV

## Version 1.0.0-dev06 (baseline: NCM 9.0.80)

- Last played playlist and song are restored when app is restarted
- Basic support for downloading ex-high music
  - Notifications
  - Dynamic button visibility
  - Downloaded songs are currently barely treated as normal local songs without any connection to remote songs
- Support 64-bit devices only
- Ensure that service is stopped after app is killed
- Fix playlist dialog crash by properly retrieving item height
- Fix playlist dialog songs don't play automatically when tapped
- Code optimizations

## Version 1.0.0-dev05 (baseline: NCM 9.0.6x)

- Fix crashes when you are trying to open player playlist dialog
- Fix size of navigationIcons
- Fix app crash when the guest user is trying to open cloud disk
- Fix logged-in user getting altered by guest on refresh
- Update dependencies

## Version 1.0.0-dev04 (baseline: NCM 9.0.6x)

- New: Support playing local music under NCM directories
- Optimize: Reduce boilerplate related to song models and adapters

## Version 1.0.0-dev03 (baseline: NCM 9.0.40)

This patch mainly contains various playlists-related development

- New: Updates on playlist fragments
  - Playing playlist dialog, my playlists, daily recommendation, cloud disk and recent play music
  - NCM-styled UI
  - Play all action
  - Minibar
  - Pagination with Paging 3

- Optimize: Introduce better ways of handling window insets
- Optimize: Minor optimizations and code cleanups
- Optimize: Guest user tokens need no refresh
- Fix: Fix action of notification next button, which is flipped by mistake

## Version 1.0.0-dev02 (baseline: NCM 9.0.40)

- New: Player screen: Lyrics with scroll animations
- New: Player screen: Dynamic background color
- New: Player screen: Show song metadata
- New: Player screen: Static album disk that will never distort with any screen size/ratio
- New: Player screen & playback notification: Previous/next control behaviors are now consistent to NCM's
- New: Player screen: Like and comment state & like actions
- New: Player screen: Toggleable UI modes
- New: Player screen: NCM-like icons and styles
- New: Player screen: Use system output switch dialog for volume control when possible (Android Q and above), or system volume toast for lower android versions. Change the device icon like NCM.
  - If not available in your system, show system volume toast.
- Optimize: General playback: Optimize player configurations for music
- Fix: Fix guest/refresh logic mistake

## Version 1.0.0-dev01 (baseline: NCM 9.0.10)

 * New: Mine page with the Music tab
 * New: Bottom controls of Now Playing screen (UI not started)
 * New: Mini music bar
 * New: Fundamental login management 
 * New: Drafts of some other UI elements
