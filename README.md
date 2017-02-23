# ListenerMusicPlayer

[![license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/hefuyicoder/ListenerMusicPlayer#license)
[![platform](https://img.shields.io/badge/platform-Android-yellow.svg)](https://www.android.com)
[![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)

### Introdution

一款优雅的遵循 Material Design 的开源音乐播放器，UI参考 腾讯轻听 音乐播放器,使用 Lastfm Api 与 酷狗歌词Api。项目架构采用 mvp-clean，基于 Retrofit2 + Dagger2 + Rxjava + RxBus + Glide。

A grace open source music player which following the google material design. Using lastfm api and kugou lyric api.App UI base on tencent qingting music player. 

### Screenshots

![screenshots](materials/screenshot.png)

### Gif Preview

![gif](materials/2017-02-10%2018_14_47.gif)

### Features

- 遵循 Material Design 规范，界面清新，交互优雅。
- 基于 MVP-CLEAN + Retrofit2 + Dagger2 + Rxjava + Glide
- 功能模块： 我的歌曲、我的歌单、文件夹、我喜欢、最近播放、最近添加、播放排行、本地搜索等。
- 支持显示歌词及缓存
- 支持耳机线控播放，耳机拔出自动暂停
- 动态刷新媒体库，及时获知媒体文件变更
- 日夜间模式切换，支持动态换肤

### Thanks

Thanks to these projects and libraries:

- Reference Project : [Timber](https://github.com/naman14/Timber) 、 [轻听](https://play.google.com/store/apps/details?id=com.tencent.qqmusiclocalplayer)
- Pictures : [Material design icon](https://github.com/google/material-design-icons) 、 腾讯轻听App
- Api : [LastFM](http://www.last.fm/zh/api) 、 [酷狗音乐](http://119.29.39.252/index.php/2016/10/20/1-2/)
- Library : [RxJava](https://github.com/ReactiveX/RxJava) 、 [Retrofit](https://github.com/square/retrofit) 、 [Glide](https://design.google.com/icons/) 、 [AndroidSlidingUpPanel](https://github.com/umano/AndroidSlidingUpPanel)等等

### Statement

感谢[轻听](https://play.google.com/store/apps/details?id=com.tencent.qqmusiclocalplayer)提供参考，轻听是一款十分良心的音乐播放器，本人也非常喜欢，欢迎大家前往下载以获得更好的使用体验。本项目部分数据来自于干LastFM和酷狗歌词Api，一切数据解释权都归LastFM和酷狗所有。

### End

> 注意：此开源项目仅做学习交流使用，如果你觉得不错，对你有帮助，欢迎点个fork，star，follow，也可以帮忙分享给你更多的朋友，这是给我们最大的动力与支持。

### Contact Me

- Github: github.com/hefuyicoder
- Email: hefuyicoder@gmail.com

### License

```
MIT License

Copyright (c) 2017 Hefuyi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

```