
# Android-UltimateGPUImage

README：
[中文版](/README_CN.md)</p>

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/windsander/UltimateGPUImage/library/images/download.svg) ](https://bintray.com/windsander/UltimateGPUImage/library/_latestVersion)

provide video record function with filter and other cool staff.

Idea from: [android-gpuimage](https://github.com/CyberAgent/android-gpuimage),which is aim to be [iOS GPUImage framework](https://github.com/BradLarson/GPUImage) in Android device.

The Elder said:
>Goal is to have something as similar to GPUImage as possible. Vertex and fragment shaders are exactly the same. That way it makes it easier to port filters from GPUImage iOS to Android.

Because android-gpuimage is deprecated so long, that cause the original project is not adjust today's quest. This project deal some bug and make more effective than the elder one. Add additional powerful recorder module, which is based on design idea of Module Partition.
So, may the GPUImage be with us =p.


<ul class="toc">
  <li>
    <a href="#filter-index">Filter Index</a>
  </li>
  <li>
    <a href="#requirements">Requirements</a>
  </li>
  <li>
    <a href="#usage">Usage</a>
    <ul>
      <li>
        <a href="#gradle">Gradle</a>
      </li>
      <li>
        <a href="#gradle-dependency">Gradle dependency</a>
      </li>
      <li>
        <a href="#sample-code">Sample Code</a>
      </li>
    </ul>
  </li>
  <li>
    <a href="#license">License</a>
  </li>
</ul>


## Filter Index

Want to see what filter the library already provide? </p>
You should take a look at filter index with:
- [EN_Version](/Index_of_Filters.md) </p>
- [CN_Version](/Index_of_Filters_CN.md) </p>

## Requirements
* Android 4.3.1 or higher (OpenGL ES 3.0)

## Usage

### Gradle
Make sure that you run the clean target when using maven.

```groovy
gradle clean assemble
```

### Gradle dependency

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'cn.co.willow.android.ultimate.gpuimage:library:1.0.3'
}
```

### Sample Code
how to use recorder module, in a simple way.

if you want to use this lib to record or take picture:
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


if you just want to operate an image, you can do like this:
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
