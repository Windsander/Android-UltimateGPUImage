/*
 * Original work Copyright 2014 Google Inc.
 * Modified work Copyright 2017 willow Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.co.willow.android.ultimate.gpuimage.utils;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static cn.co.willow.android.ultimate.gpuimage.core_config.FilterConfig.NO_TEXTURE;

/**
 * OpenGl常用工具类
 * Some OpenGL utility functions.
 * <p>
 * Created by willow.li on 2016/10/20.
 */
public class GlUtil {

    /*关键常量======================================================================================*/
    public static final String TAG = "GlUtil";

    /** Identity matrix for general use.  Don't modify or life will get weird. */
    public static final float[] IDENTITY_MATRIX;

    static {
        IDENTITY_MATRIX = new float[16];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);
    }

    private static final int SIZEOF_FLOAT = 4;


    /*常用方法======================================================================================*/
    /**
     * 通过传入的顶点着色器和片段着色器，创建渲染处理流
     * Creates a new program from the supplied vertex and fragment shaders.
     *
     * @return A handle to the program, or 0 on failure.
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES30.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            LogUtil.e(TAG, "Could not create program");
        } else {
            GLES30.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES30.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES30.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES30.GL_TRUE) {
                LogUtil.e(TAG, "Could not link program: ");
                LogUtil.e(TAG, GLES30.glGetProgramInfoLog(program));
                GLES30.glDeleteProgram(program);
                program = 0;
            }
        }
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(pixelShader);
        return program;
    }

    /**
     * 预处理指定类型着色器
     * Compiles the provided shader source.
     *
     * @return A handle to the shader, or 0 on failure.
     */
    public static int loadShader(int shaderType, String source) {
        int shader = GLES30.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        if (shader == 0) {
            LogUtil.e(TAG, "Create shader fail");
        } else {
            GLES30.glShaderSource(shader, source);
            GLES30.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                LogUtil.e(TAG, "Could not compile shader " + shaderType + ":");
                LogUtil.e(TAG, " " + GLES30.glGetShaderInfoLog(shader));
                GLES30.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * 纹理坐标、顶点坐标、纹理等对应id的合理性校验方法
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     * <p>
     * Throws a RuntimeException if the location is invalid.
     */
    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }

    /**
     * 根据raw资源文件创建渲染纹理
     * Creates a texture from raw data.
     *
     * @param data   Image data, in a "direct" ByteBuffer.
     * @param width  Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    public static int createTexture(final ByteBuffer data, final int width, final int height, final int format) {
        int textures[] = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        GlUtil.checkGlError("glGenTextures");
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        GlUtil.checkGlError("glBindTexture");
        GLES30.glTexParameteri(
                /*texture-type*/ GLES30.GL_TEXTURE_2D,
                /*filter-type*/  GLES30.GL_TEXTURE_MIN_FILTER,
                /*algorithm*/    GLES30.GL_LINEAR);
        GLES30.glTexParameteri(
                /*texture-type*/ GLES30.GL_TEXTURE_2D,
                /*filter-type*/  GLES30.GL_TEXTURE_MAG_FILTER,
                /*algorithm*/    GLES30.GL_LINEAR);
        GlUtil.checkGlError("glTexParameteri");
        GLES30.glTexImage2D(
                /*aim*/          GLES30.GL_TEXTURE_2D,
                /*level*/        0,
                /*detail*/       format, width, height,
                /*border*/       0,
                /*pixel-format*/ format,
                /*pixel-type*/   GLES30.GL_UNSIGNED_BYTE,
                /*pixel-data*/   data);
        GlUtil.checkGlError("glTexImage2D");

        return textures[0];
    }

    /**
     * 根据Bitmap创建渲染纹理
     * Creates a texture from bitmap data.
     *
     * @param img       Image data, in Bitmap format.
     * @param usedTexId an exist texture id ,if u want to use in this place.
     * @param recycle   should method recycle the bitmap when done with it.
     * @return Handle to texture.
     */
    public static int createTexture(final Bitmap img, final int usedTexId, final boolean recycle) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GlUtil.checkGlError("glGenTextures");
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GlUtil.checkGlError("glBindTexture");
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR);
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER,
                    GLES30.GL_LINEAR);
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S,
                    GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T,
                    GLES30.GL_CLAMP_TO_EDGE);
            GlUtil.checkGlError("glTexParameteri");
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, img, 0);
            GlUtil.checkGlError("glTexImage2D");
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
            GlUtil.checkGlError("glBindTexture");
            GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, img);
            GlUtil.checkGlError("glTexImage2D");
            textures[0] = usedTexId;
        }
        if (recycle) {
            img.recycle();
        }
        return textures[0];
    }

    /**
     * 根据配置好的数据生成渲染纹理
     * Creates a texture from buffer data.
     *
     * @param data      Image data, in IntBuffer.
     * @param width     Texture width, in pixels (not bytes).
     * @param height    Texture height, in pixels.
     * @param usedTexId an exist texture id ,if u want to use in this place.
     * @return Handle to texture.
     */
    public static int createTexture(final IntBuffer data, final int width, final int height, final int usedTexId) {
        int textures[] = new int[1];
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0);
            GlUtil.checkGlError("glGenTextures");
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
            GlUtil.checkGlError("glBindTexture");
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER,
                    GLES30.GL_LINEAR);
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR);
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S,
                    GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(
                    GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T,
                    GLES30.GL_CLAMP_TO_EDGE);
            GlUtil.checkGlError("glTexParameterf");
            GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_RGBA, width, height,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    data);
            GlUtil.checkGlError("glTexImage2D");
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId);
            GlUtil.checkGlError("glBindTexture");
            GLES30.glTexSubImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    0, 0, width, height,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    data);
            GlUtil.checkGlError("glTexSubImage2D");
            textures[0] = usedTexId;
        }
        return textures[0];
    }

    /**
     * 申请浮点缓存，并向其中压入数据
     * Allocates a direct float buffer, and populates it with the float array data.
     */
    public static FloatBuffer createFloatBuffer(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }


    /*OpenGl日志监测逻辑============================================================================*/
    /**
     * OpenGL错误检测方法
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            //LogUtil.e(TAG, msg);
            // throw new RuntimeException(msg);
        }
    }

    /**
     * 打印当前OpenGl版本信息到日志
     * Writes GL version info to the log.
     */
    public static void logVersionInfo() {
        LogUtil.i(TAG, "vendor  : " + GLES30.glGetString(GLES30.GL_VENDOR));
        LogUtil.i(TAG, "renderer: " + GLES30.glGetString(GLES30.GL_RENDERER));
        LogUtil.i(TAG, "version : " + GLES30.glGetString(GLES30.GL_VERSION));

        if (false) {
            int[] values = new int[1];
            GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, values, 0);
            int majorVersion = values[0];
            GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, values, 0);
            int minorVersion = values[0];
            if (GLES30.glGetError() == GLES30.GL_NO_ERROR) {
                LogUtil.i(TAG, "iversion: " + majorVersion + "." + minorVersion);
            }
        }
    }

}