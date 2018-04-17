
# 前言

**所有 单一滤镜 都是基于 GPUImageFilter**</p>
**所有 组合滤镜 都是基于 GPUImageFilterGroup**</p>
这两个滤镜是本库中所提供的所有的滤镜的基类。所以，如果你有自定义滤镜的需求，请确保你所定义的滤镜，有直接或间接的继承自这两个类别。具体根据所定义的滤镜是单一滤镜，还是组合滤镜来作区分。


# 目录

<ul class="toc">
  <li>
    <a href="#基础滤镜">基础滤镜</a>
  </li>
  <li>
    <a href="#色彩增强">色彩增强</a>
  </li>
  <li>
    <a href="#图形变换滤镜">图形变换滤镜</a>
  </li>
  <li>
    <a href="#渲染模式滤镜">渲染模式滤镜</a>
    <ul>
      <li>
        <a href="#映射渲染">映射渲染</a>
      </li>
      <li>
        <a href="#混合渲染">混合渲染</a>
      </li>
      <li>
        <a href="#通用渲染">通用渲染</a>
      </li>
    </ul>
  </li>
  <li>
    <a href="#双通滤镜">双通滤镜</a>
  </li>
  <li>
    <a href="#3x3-采样滤镜">3x3 采样滤镜</a>
  </li>
</ul>


# 基础滤镜

基础滤镜组，是提供有关输入数据的基本色彩属性操作的滤镜。</p>
例如： RGB, Gamma, Alpha, HUE 等

Filter | Usage
---|---
GPUImageBrightnessFilter        |控制 亮度
GPUImageContrastFilter          |控制 对比度
GPUImageExposureFilter          |控制 曝光度
GPUImageGammaFilter             |控制 Gamma值
GPUImageGrayscaleFilter         |控制 灰度
GPUImageHighlightShadowFilter   |控制 光影敏感
GPUImageHueFilter               |控制 HUE
GPUImageLevelsFilter            |控制 色阶
GPUImageOpacityFilter           |控制 透明度
GPUImagePosterizeFilter         |控制 色调分离 （1-255，默认 10）
GPUImageRGBFilter               |控制 RGB
GPUImageSaturationFilter        |控制 饱和度
GPUImageSharpenFilter           |控制 锐化
GPUImageToneCurveFilter         |控制 色调曲线
GPUImageTransformFilter         |控制 图形变换矩阵

# 色彩增强

色彩增强滤镜组，用于加强输入流单一色调（多色调）的变化、增强、减弱等处理。</p>
相对与基础滤镜组对输入流的基本属性修改，色彩增强滤镜通过配置文件和映射表来实现更强的颜色控制。

Filter | Usage
---|---
GPUImageBilateralFilter         |双边模糊，滤波后图像中的每个像素点都是由其原图像中该点临域内多个像素点值的加权平均.
GPUImageCGAColorspaceFilter     |CGA色域着色效果，原有色泽被黑、浅蓝、紫色块替代，像素效果
GPUImageColorBalanceFilter      |色泽均衡，均匀色泽效果
GPUImageColorInvertFilter       |反色滤镜，所有颜色取反色
GPUImageColorMatrixFilter       |用指定的颜色矩阵来渲染图片，增强图片偏向色泽
GPUImageFalseColorFilter        |色彩替换（替换亮部和暗部色彩）
GPUImageMonochromeFilter        |单色滤镜，依据像素值，替换图片为指定单色
GPUImageWhiteBalanceFilter      |白平衡滤镜，保证其他景物的影像就会接近人眼的色彩视觉习惯的一种处理滤镜

# 图形变换滤镜

图形变换滤镜，提供图形变化效果。

Filter | Usage
---|---
GPUImageBulgeDistortionFilter   |鱼眼特效
GPUImageCrosshatchFilter        |交叉阴影特效
GPUImageGlassSphereFilter       |水晶球特效
GPUImageSphereRefractionFilter  |球面反射特效
GPUImageSwirlFilter             |漩涡特效
GPUImageVignetteFilter          |边缘晕影特效

# 渲染模式滤镜

渲染模式滤镜，针对两个输入源的混合处理，基类是 **GPUImageTwoInputFilter**。</p>
你需要通过下面的方法来自己准备 第二个输入源：
```java
setBitmap(Bitmap second)
```
目前第二输入源只支持图片，但是你可以自己设定方法来往里添加输入流</p>
第二个输入源用来和第一个输入源进行联合处理，以达到最终效果。

## 映射渲染

映射渲染滤镜是一个特殊的滤镜，我们可以通过传入特殊的png渲染配置文件，来快速达到诸如：简易美白、明信片等效果。

Filter | Usage
---|---
GPUImageLookupFilter            |颜色映射表渲染滤镜基类，通过设置好的颜色映射表来进行图片渲染

## 混合渲染

混合渲染滤镜是一组特殊的滤镜，通常用来平滑糅合两个输入源的图层资源为一个单一输出

0.0f 只显示第一个图层, 1.0f 只显示第二个图层

Filter | Usage
---|---
GPUImageAlphaBlendFilter        |混合两层，控制 透明度
GPUImageDissolveBlendFilter     |混合两层，控制 溶解度
GPUImageMixBlendFilter          |混合两层，控制 混合度

## 通用渲染

通用混合模式滤镜组

Filter | Usage
---|---
GPUImageAddBlendFilter          |图片混合滤镜，通常用于创建两个图像之间的动画变亮模糊效果
GPUImageChromaKeyBlendFilter    |色度键着色滤镜，用第二张图片的主要颜色来替换第一张图片的指定颜色，可选择替换浮动的范围（敏感度）
GPUImageColorBlendFilter        |配色滤镜，将第二张作为着色层的图片渲染到第一张图片上，替换对应颜色
GPUImageColorBurnBlendFilter    |色彩加深混合滤镜，效果是第一张图将会以类似蒙皮的形式覆盖在第二张图纸上，形成组合。经常用于创造一些文字特效
GPUImageColorDodgeBlendFilter   |色彩减淡混合滤镜，会按照第二张图来抵消同位置的第一张图的对应位置颜色效果
GPUImageDarkenBlendFilter       |加深混合，通常用于重叠类型,可以类推Proter&Duff 的 darken 渲染模式
GPUImageDifferenceBlendFilter   |差异混合，通常用于创建更多变动的颜色
GPUImageDivideBlendFilter       |消减混合，用于创建两个图像之间的动画变暗模糊效果
GPUImageExclusionBlendFilter    |排除混合，排除同色
GPUImageHardLightBlendFilter    |强光混合，通常用于创建阴影效果
GPUImageHueBlendFilter          |色调混合，输入源HUE混合
GPUImageLightenBlendFilter      |减淡混合，通常用于重叠效果
GPUImageLinearBurnBlendFilter   |线性加深混合
GPUImageLuminosityBlendFilter   |图片增亮渲染，用第二层来对第一层进行“美白”渲染
GPUImageMultiplyBlendFilter     |多重混合滤镜，通常用于创建阴影和深度效果
GPUImageSimpleOverlayBlendFilter|算法简化覆盖混合，第二层位于被渲染层的下层，效果较SrcOver偏淡
GPUImageOverlayBlendFilter      |重叠绘制，两层图层叠加在一起。通常用于创建阴影效果
GPUImageScreenBlendFilter       |滤色混合，保留两个图层中较白的部分，较暗的部分被遮盖
GPUImageSoftLightBlendFilter    |柔光混合
GPUImageSourceOverBlendFilter   |源混合，两个图片源直接混合
GPUImageSubtractBlendFilter     |差值混合,通常用于创建两个图像之间的动画变暗模糊效果

# 双通滤镜

双通混合滤镜基类为 **GPUImageTwoPassTextureSamplingFilter**.

这类滤镜通常需要在 x、y 两个方向上对传入的数据源进行不同的操作，单一的一组着色器已经无法满足当前要求。因此，需要一个特殊的滤镜组，来分步处理。</p>

Filter | Usage
---|---
GPUImageBoxBlurFilter           |盒状模糊滤镜，一种经过硬件加速后的快速图片模糊算法，有可能产生马赛克，如果像素太低的话
GPUImageDilationFilter          |扩展边缘模糊，通过设置模糊半径（Radius）来控制边缘模糊级别，黑白图片
GPUImageGaussianBlurFilter      |高斯模糊，在 9*9 像素区域内实现的依据高斯密度公式实现的模糊算法
GPUImageRGBDilationFilter       |RGB扩展边缘模糊，通过设置模糊半径（Radius）来控制边缘模糊级别，彩色图片

# 3x3 采样滤镜

3x3 采样滤镜基类为 **GPUImage3x3TextureSamplingFilter**.

此类滤镜在操作过程中对像素点周围 3x3 范围的临接像素点进行了比较、转换、变化等一个或多个操作，通常是边界检测等滤镜的特点

Filter | Usage
---|---
GPUImage3x3ConvolutionFilter    |3x3卷积，通过降低轮廓线上的像素点周边像素颜色，来实现轮廓线增亮
GPUImageDirectionalSobelEdgeDetectionFilter |直接索贝尔算子边缘检测滤镜
GPUImageLaplacianFilter         |拉普拉斯算子滤镜，噪音敏感。 通常的分割算法都是把 Laplacian算子和平滑算子结合起来生成一个新的模板 information.
GPUImageNonMaximumSuppressionFilter |非极大值抑制（NMS）边缘检测
GPUImageSobelThresholdFilter    |严格索贝尔算子边缘检测滤镜
GPUImageWeakPixelInclusionFilter    |像素收缩滤镜
