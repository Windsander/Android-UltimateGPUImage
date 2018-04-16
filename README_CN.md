
# Android-UltimateGPUImage
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/windsander/UltimateGPUImage/library/images/download.svg) ](https://bintray.com/windsander/UltimateGPUImage/library/_latestVersion)

本库提供 短视频编辑、视频流处理、渲染滤镜、自定义滤镜 等图形功能。

想法来自于: Android 开源库 [android-gpuimage](https://github.com/CyberAgent/android-gpuimage)，一个类似于 [iOS GPUImage framework](https://github.com/BradLarson/GPUImage) 的开源滤镜库。

The Elder said:
>Goal is to have something as similar to GPUImage as possible. Vertex and fragment shaders are exactly the same. That way it makes it easier to port filters from GPUImage iOS to Android.

相对来说，android-gpuimage 这个库已经好一段时间没有更新了。我觉老版本库中的一些特性，已经不能很好的满足当前的实际需求。因此，本项目的目的是为了在原有的 android-gpuimage 的出发点下，重新对框架进行扩展性设计和整合，解决已知bug并增加新的短视频相关功能。</p>
新框架有以下几个优点：
- 架构上进行了重新设计、重构
- 新增加视频录制相关功能（功能占库 51%，请自行决定是否使用）
- 对已有的可用的滤镜进行了整合归纳
- 修改了部分 android-gpuimage 中有 bug 的无法使用的滤镜
- 优化了部分滤镜效果
- 采用模块化的设计思路

愿 GPUImage 与你我同在。


<ul class="toc">
  <li>
    <a href="#滤镜目录">滤镜目录</a>
  </li>
  <li>
    <a href="#环境要求">环境要求</a>
  </li>
  <li>
    <a href="#使用方法">使用方法</a>
    <ul>
      <li>
        <a href="#gradle依赖配置">Gradle依赖配置</a>
      </li>
      <li>
        <a href="#简单示例">简单示例</a>
      </li>
      <li>
        <a href="#gradle">Gradle</a>
      </li>
    </ul>
  </li>
  <li>
    <a href="#license">License</a>
  </li>
</ul>


## 滤镜目录

想要看一看本库提供的滤镜都有哪些？</p>
你需要的是一份详细的滤镜列表:
- [EN_Version](/Index_of_Filters.md) </p>
- [CN_Version](/Index_of_Filters.md)  (还未准备好) </p>

## 环境要求
* Android 4.3.1 or higher (OpenGL ES 3.0)

## 使用方法

### Gradle依赖配置

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'cn.co.willow.android.ultimate.gpuimage:library:1.0.3'
}
```

### 简单示例

如何快速的使用本库的录制模块，请看下面的例子。

如果你想渲染一张图片的话：

```java
@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    // prepare camera preview, and bind to recorder manager.
    mRecorderViews = (FilterRecoderView) findViewById(R.id.vp_video_recorder_gl);
    mRecordManager = new VideoRecordManager(context, mRecorderViews);

    // u can use this method to set ur own config rather than default one.(this is not necessary)
    mRecordManager.setAVConfig(videoConfig, audioConfig);

    // and now, we can start the camera
    mRecordManager.openCamera();

    // if u want use filter, this method can be called in any place
    mRecordManager.setFilter(/*filter*/);

    // when record start
    mRecordManager.startRecord(/*videoSaveFile*/);

    // when record finish
    mRecordManager.stopRecord();

    // if u don't want to use camera anymore.
    mRecordManager.releaseCamera();
}
```


如果你想操作视频流的话:

```java
@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    // prepare surface.
    mRecorderViews = (FilterRecoderView) findViewById(R.id.vp_video_recorder_gl);

    // prepare the filter you want to use
    final GPUImageContrastFilter contrastFilter = new GPUImageContrastFilter(1.0f);
    contrastFilter.setContrast(contrastValue);

    // bind to PureImageManager and render
    PureImageManager.init(context)
                    .setGLSurfaceView(mRecorderViews)
                    .setScaleType(GPUImage.ScaleType.CENTER_INSIDE)
                    .setImage(mUri)
                    .setFilter(contrastFilter)
                    .requestRender();
}
```


### Gradle
保证你在下载资源的时候，清空gradle缓存.

```groovy
gradle clean assemble
```

## License
    Copyright 2017-2020 Willow.li

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
