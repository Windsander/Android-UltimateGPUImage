# Android-UltimateGPUImage
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/windsander/UltimateGPUImage/library/images/download.svg) ](https://bintray.com/windsander/UltimateGPUImage/library/_latestVersion)

provide video record function with filter and other cool staff. 

Idea from: [android-gpuimage](https://github.com/CyberAgent/android-gpuimage),which is aim to be [iOS GPUImage framework](https://github.com/BradLarson/GPUImage) in Android device.

The Elder said:
>Goal is to have something as similar to GPUImage as possible. Vertex and fragment shaders are exactly the same. That way it makes it easier to port filters from GPUImage iOS to Android.

Because android-gpuimage is deprecated so long, that cause the original project is not adjust today's quest. This project deal some bug and make more effective than the elder one. Add additional powerful recorder module, which is based on design idea of Module Partition. 
So, may the GPUImage be with us =p.  


## Requirements
* Android 4.3.1 or higher (OpenGL ES 3.0)

## Usage

### Gradle dependency

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'cn.co.willow.android.ultimate.gpuimage:library:1.0'
}
```

### Sample Code
how to use recorder module, in a simple way. 

```java
@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity);

    // prepare camera preview, and bind to recorder manager.
    mRecorderViews = (FilterRecoderView) findViewById(R.id.vp_video_recorder_gl);
    mRecordManager = new VideoRecordManager(context, mRecorderViews);
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


### Gradle
Make sure that you run the clean target when using maven.

```groovy
gradle clean assemble
```

## License
    Copyright 2017 Winsander, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
