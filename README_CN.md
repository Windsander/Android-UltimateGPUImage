
# Android-UltimateGPUImage
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/windsander/UltimateGPUImage/library/images/download.svg) ](https://bintray.com/windsander/UltimateGPUImage/library/_latestVersion)

本库提供 短视频编辑、视频流处理、渲染滤镜、自定义滤镜 等图形功能。

想法来自于: Android 开源库 [android-gpuimage](https://github.com/CyberAgent/android-gpuimage)，一个类似于 [iOS GPUImage framework](https://github.com/BradLarson/GPUImage) 的开源滤镜库。

The Elder said:
>Goal is to have something as similar to GPUImage as possible. Vertex and fragment shaders are exactly the same. That way it makes it easier to port filters from GPUImage iOS to Android.

相对来说，android-gpuimage 只提供了一些基础的滤镜操作，且已经好一段时间没有更新了。我觉老版本库已经不能很好的满足当前的实际需求。因此，本项目的目的是在保证原有的 android-gpuimage 为 Android开发者们提供简易、强大的图像操作功能 的出发点的前提下，重新对框架进行整体量级的设计和整合，解决已知bug，增强扩展性，并增加新的短视频相关功能。</p>
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
- [CN_Version](/Index_of_Filters_CN.md) </p>

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

如果你想操作视频流的话:

```java
@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    // 准备预览界面，绑定管理器.
    mRecorderViews = (FilterRecoderView) findViewById(R.id.vp_video_recorder_gl);
    mRecordManager = new VideoRecordManager(context, mRecorderViews);

    // 设置录制视频的音视频参数，不设置则为默认值.(并不必须)
    mRecordManager.setAVConfig(videoConfig, audioConfig);

    // 现在，我们就可以开始录制了
    mRecordManager.openCamera();

    // 如果你需要配置滤镜，可以调用这个方法（调用方法将替换之前设置，方法可以在任何位置调用）
    mRecordManager.setFilter(/*filter*/);

    // 开始视频录制
    mRecordManager.startRecord(/*videoSaveFile*/);

    // 结束视频录制
    mRecordManager.stopRecord();

    // 如果不再使用，调用此方法，彻底释放资源
    mRecordManager.releaseCamera();
}
```

如果你想渲染一张图片的话：

```java
@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    // 准备预览层.
    mRecorderViews = (FilterRecoderView) findViewById(R.id.vp_video_recorder_gl);

    // 配置打算使用的效果滤镜
    final GPUImageContrastFilter contrastFilter = new GPUImageContrastFilter(1.0f);
    contrastFilter.setContrast(contrastValue);

    // 开始纯图片处理
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
