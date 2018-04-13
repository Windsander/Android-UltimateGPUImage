
# First of all

**Every single filter should base on GPUImageFilter**</p>
**Every filter group should base on GPUImageFilterGroup**</p>
They are the foundation of all filter we see here. So, you need to extend directly form this two or their subclass, if you want to design your own filters.


# Index

<ul class="toc">
  <li>
    <a href="#base-filter">Base filter</a>
  </li>
  <li>
    <a href="#color-enhance-filter">Color Enhance Filter</a>
  </li>
  <li>
    <a href="#conversion-filter">Conversion Filter</a>
  </li>
  <li>
    <a href="#image-enhance-filter">Image Enhance Filter</a>
    <ul>
      <li>
        <a href="#blend-mode-color-mapping-blend">Blend Mode: Color Mapping Blend</a>
      </li>
      <li>
        <a href="#blend-mode-mix-control-blend">Blend Mode: Mix Control Blend</a>
      </li>
      <li>
        <a href="#blend-mode-filter">Blend Mode Filter</a>
      </li>
    </ul>
  </li>
  <li>
    <a href="#dual-sampling-filter">Dual Sampling Filter</a>
  </li>
  <li>
    <a href="#3x3-sampling-filter">3x3 Sampling Filter</a>
  </li>
</ul>


# Base filter

This kind of filter provide base function of stream render operations.</p>
Such as: RGB, Gamma, Alpha, HUE etc.

Filter | Usage
---|---
GPUImageBrightnessFilter        |Control Brightness of input stream
GPUImageContrastFilter          |Control Contrast of input stream
GPUImageExposureFilter          |Control Exposure of input stream
GPUImageGammaFilter             |Control Gamma of imput stream
GPUImageGrayscaleFilter         |Control Grayscale of input stream
GPUImageHighlightShadowFilter   |Adjust the shadows and highlights of stream
GPUImageHueFilter               |Adjust HUE of input stream
GPUImageLevelsFilter            |Operate color gradation of stream
GPUImageOpacityFilter           |Adjust alpha of stream
GPUImagePosterizeFilter         |Posterize stream by settings (1-256 , def=10)
GPUImageRGBFilter               |Adjusts the individual RGB channels of stream
GPUImageSaturationFilter        |Adjusts the Saturation of stream
GPUImageSharpenFilter           |Sharpen frame of stream from input
GPUImageToneCurveFilter         |Define curves to deal with input
GPUImageTransformFilter         |Define matrix to shape image

# Color Enhance Filter

This kind of filter use to enchance stream color preference by many way.</p>
Not like Base-Filter use frames own property to change its view. Color Enchance Filter often take frames color by prefabricated config or config-map.

Filter | Usage
---|---
GPUImageBilateralFilter         |Bilateral, use to enhance image to make sure every pixels is weighted average whit surround.
GPUImageCGAColorspaceFilter     |Use CGA Colorspace to replace original color of image, pxiel effect.
GPUImageColorBalanceFilter      |Use to balance color of input stream, soft what it looks
GPUImageColorInvertFilter       |Invert all the colors of stream from input
GPUImageColorMatrixFilter       |Applies a ColorMatrix to the stream.
GPUImageFalseColorFilter        |Replace color in the light and dark areas
GPUImageMonochromeFilter        |Converts the image to a single-color version, based on the luminance of each pixel
GPUImageWhiteBalanceFilter      |Adjusts the white balance of incoming frame. To insure image adjust humans vision

# Conversion Filter

This kind of filter use to converse frame looks to another.

Filter | Usage
---|---
GPUImageBulgeDistortionFilter   |This conversion filter provide effect, which looks like fish eyes.
GPUImageCrosshatchFilter        |Crosshatch the input, makes it looks like cover by a net with black and white color
GPUImageGlassSphereFilter       |This conversion filter provide effect, which looks like class-sphere.
GPUImageSphereRefractionFilter  |Make a sphere based on image. sphere will show refelection of selecting region
GPUImageSwirlFilter             |Creates a swirl distortion on the image.
GPUImageVignetteFilter          |Performs a vignetting effect, fading out the image at the edges

# Image Enhance Filter

Based on ==GPUImageTwoInputFilter==, which deal with 2 input texture.</p>
You should prepare fragment-shader by youself.
And this kind of filter use below method:
```java
setBitmap(Bitmap second)
```
to set their second texture as assistant.</p>
The second texture is used by shader to create wanting effect.

## Blend Mode: Color Mapping Blend
this is a perticular special filter, which use png as input params to filte stream

Filter | Usage
---|---
GPUImageLookupFilter            |According to giving mapping to render stream

## Blend Mode: Mix Control Blend
this bunch of filter, use to control the situation, which have two texture to mix as one.

0.0f only show texture-1, 1.0f only show texture-2

Filter | Usage
---|---
GPUImageAlphaBlendFilter        |Blend two textures and control alpha-ratio
GPUImageDissolveBlendFilter     |Blend two textures and control dissolve-ratio
GPUImageMixBlendFilter          |Blend two textures and control mix-ratio

## Blend Mode Filter

Filter | Usage
---|---
GPUImageAddBlendFilter          |Always use to create animation, which do lighten and blur between 2 images
GPUImageChromaKeyBlendFilter    |Selectively replaces a color in the first texture with the second texture's major color
GPUImageColorBlendFilter        |Rendering the first texture with second texture as a color layer
GPUImageColorBurnBlendFilter    |Rendering the first texture with second texture as a skin. Can use to develop some text-effect.
GPUImageColorDodgeBlendFilter   |Decrease the first texture's color level, by using second texture, at certain place
GPUImageDarkenBlendFilter       |Increase the first texture's color level, by using second texture, at certain place
GPUImageDifferenceBlendFilter   |Variantly Mix two textures to create fluctuant effect
GPUImageDivideBlendFilter       |Do draken and blur between 2 images.Always use to create animation.
GPUImageExclusionBlendFilter    |Blend two texture exclude same color
GPUImageHardLightBlendFilter    |Blend two texture, use second texture as a light source to create shadow effect
GPUImageHueBlendFilter          |Combine two texture and their HUE
GPUImageLightenBlendFilter      |Soft lighten mode, use to create overlap effect
GPUImageLinearBurnBlendFilter   |Simple linear blend
GPUImageLuminosityBlendFilter   |Illuminate the first texture with second texture as render layer (not spot light), can use to "beautify" image
GPUImageMultiplyBlendFilter     |Multi-blend with second texture, use to create shadow & deep-scale effect
GPUImageNormalBlendFilter       |Like System Blend Mode
GPUImageOverlayBlendFilter      |Overlay two inputs together, use to create shadow
GPUImageScreenBlendFilter       |Wrap up first texture with second
GPUImageSoftLightBlendFilter    |Create shoft light effect with second texture
GPUImageSourceOverBlendFilter   |Directly mixed 2 inputs together
GPUImageSubtractBlendFilter     |Subtractly mixed 2 inputs together

# Dual Sampling Filter

Based on **GPUImageTwoPassTextureSamplingFilter**.

This kind of filters is two-pass, whitch is always use in some filter that needs x and y two way to deal in a single task</p>
The shader used in this kind have to cooperate in a single task to get what we want, always two in a filter.

Filter | Usage
---|---
GPUImageBoxBlurFilter           |A hardware-accelerated 9-hit box blur filter
GPUImageDilationFilter          |Pure Extended edge blur
GPUImageGaussianBlurFilter      |9x9 Gaussian blur filter
GPUImageRGBDilationFilter       |RGB extension edge blur.

# 3x3 Sampling Filter

Based on **GPUImage3x3TextureSamplingFilter**.

This kind of filters is use 3x3 matrix to convert color pixels, or compare single pixels surrounding to extract frame edges properties.

Filter | Usage
---|---
GPUImage3x3ConvolutionFilter    |Convolute pixels, enlight the pixel in center by taking down its surroundings, if they are at edge.
GPUImageDirectionalSobelEdgeDetectionFilter |Sobel operator edge detection filter
GPUImageLaplacianFilter         |Laplacian filter use simple sharpening method, which produce the effect of the Laplace transform and retain the background information.
GPUImageNonMaximumSuppressionFilter |Use Non-Maximum Suppression(NMS) method to check current pixel is the truely edge. Will get feature points as output of current frames.
GPUImageSobelThresholdFilter    |Strict Sobel operator edge detection filter.
GPUImageWeakPixelInclusionFilter    |Weak Pixel Inclusion Filter.
