/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.co.willow.android.ultimate.gpuimage.core_render_filter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.PointF;
import android.opengl.GLES30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import cn.co.willow.android.ultimate.gpuimage.utils.GlUtil;

import static cn.co.willow.android.ultimate.gpuimage.core_config.FilterConfig.NO_TEXTURE;

/**
 * Created by willow.li on 2016/10/20.
 */

public class GPUImageFilter {

    /*着色器语言====================================================================================*/
    /** 无渲染的顶点着色器 vertex shader without filter */
    protected static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 vPosition;\n" +
            "attribute vec4 vTextureCoord;\n" +
            "varying vec2 textureCoordinate;\n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = vPosition;\n" +
            "    textureCoordinate = vTextureCoord.xy;\n" +
            "}";

    /** 无渲染的片段着色器 fragment shader without filter */
    protected static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D fSampler2D;\n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(fSampler2D, textureCoordinate);\n" +
            "}";


    /*关键变量======================================================================================*/
    private final LinkedList<Runnable> mRunOnDraw;
    private final String mVertexShader;
    private final String mFragmentShader;
    protected int mGLProgId;                         // 着色程序片
    protected int mVertexPosition;                  // 顶点着色器顶点坐标数据索引
    protected int mVertexTexture;                   // 顶点着色器顶点纹理坐标索引
    protected int mFrag2DSampler;                   // 片段着色器2D采样器索引
    protected int mOutputWidth;
    protected int mOutputHeight;
    private boolean mIsInitialized;

    public GPUImageFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public GPUImageFilter(final String vertexShader, final String fragmentShader) {
        mRunOnDraw = new LinkedList<Runnable>();
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }


    /*生命周期流程==================================================================================*/
    public final void init() {
        onInit();
        mIsInitialized = true;
        onInitialized();
    }

    public final void destroy() {
        mIsInitialized = false;
        GLES30.glDeleteProgram(mGLProgId);
        onDestroy();
    }


    /*生命周期======================================================================================*/
    public void onInit() {
        mGLProgId = GlUtil.createProgram(mVertexShader, mFragmentShader);
        mVertexPosition = GLES30.glGetAttribLocation(mGLProgId, "vPosition");
        mVertexTexture = GLES30.glGetAttribLocation(mGLProgId, "vTextureCoord");
        mFrag2DSampler = GLES30.glGetUniformLocation(mGLProgId, "fSampler2D");
        mIsInitialized = true;
    }

    public void onInitialized() {
    }

    public void onDestroy() {
    }

    public void onOutputSizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    public void onDraw(final int textureId, final FloatBuffer cubeBuffer, final FloatBuffer textureBuffer) {
        GLES30.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!mIsInitialized) {
            return;
        }

        cubeBuffer.position(0);
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mVertexPosition, 2, GLES30.GL_FLOAT, false, 0, cubeBuffer);
        GLES30.glVertexAttribPointer(mVertexTexture, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(mVertexPosition);
        GLES30.glEnableVertexAttribArray(mVertexTexture);
        if (textureId != NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            GLES30.glUniform1i(mFrag2DSampler, 0);
        }
        onDrawArraysPre();
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        GLES30.glDisableVertexAttribArray(mVertexPosition);
        GLES30.glDisableVertexAttribArray(mVertexTexture);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }

    protected void onDrawArraysPre() {
    }


    /*参数获取方法==================================================================================*/
    public boolean isInitialized() {
        return mIsInitialized;
    }

    public int getOutputWidth() {
        return mOutputWidth;
    }

    public int getOutputHeight() {
        return mOutputHeight;
    }

    public int getProgram() {
        return mGLProgId;
    }

    public int getAttribPosition() {
        return mVertexPosition;
    }

    public int getAttribTextureCoordinate() {
        return mVertexTexture;
    }

    public int getUniformTexture() {
        return mFrag2DSampler;
    }


    /*参数配置方法==================================================================================*/
    public void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform1i(location, intValue);
            }
        });
    }

    public void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform1f(location, floatValue);
            }
        });
    }

    public void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    public void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    public void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    public void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES30.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    public void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES30.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    public void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES30.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    public void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES30.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    public static String loadShader(String file, Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream ims = assetManager.open(file);
            String re = convertStreamToString(ims);
            ims.close();
            return re;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
