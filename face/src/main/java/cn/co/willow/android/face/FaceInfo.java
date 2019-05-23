package cn.co.willow.android.face;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description: 单张人脸数据
 * <p>
 * Created by Willow.Li on 2019/3/26
 */
public class FaceInfo implements Serializable {

    private final static String FACE_KEY_CHIN = "chin";
    private final static String FACE_KEY_LEFT_EYEBROW = "left_eyebrow";
    private final static String FACE_KEY_RIGHT_EYEBROW = "right_eyebrow";
    private final static String FACE_KEY_NOSE_BRIDGE = "nose_bridge";
    private final static String FACE_KEY_NOSE_TIP = "nose_tip";
    private final static String FACE_KEY_LEFT_EYE = "left_eye";
    private final static String FACE_KEY_RIGHT_EYE = "right_eye";
    private final static String FACE_KEY_TOP_LIP = "top_lip";
    private final static String FACE_KEY_BOTTOM_LIP = "bottom_lip";

    public final static String[] FACE_MARK_LIST_68 = {
            FACE_KEY_CHIN,
            FACE_KEY_LEFT_EYEBROW,
            FACE_KEY_RIGHT_EYEBROW,
            FACE_KEY_NOSE_BRIDGE,
            FACE_KEY_NOSE_TIP,
            FACE_KEY_LEFT_EYE,
            FACE_KEY_RIGHT_EYE,
            FACE_KEY_TOP_LIP,
            FACE_KEY_BOTTOM_LIP
    };

    public PointF[] mKeyPoints;        // 脸部特征点数据集

    public FaceInfo() {
    }

    /*关键参数获取=====================================================================================*/

    /**
     * 获取关键特征点集合
     *
     * @return 特征点集合
     */
    public PointF[] getKeyPoints() {
        return mKeyPoints;
    }

    /**
     * 68点关键参数检索方法
     *
     * @param facemark 检索脸部位置
     * @return 对应位置特征点数据集
     */
    public PointF[] getCertainFacemark68(String facemark) {
        if (null == mKeyPoints || TextUtils.isEmpty(facemark)) {
            return mKeyPoints;
        }
        List<PointF> result = new ArrayList<>();
        final List<PointF> pointArray = Arrays.asList(mKeyPoints);
        switch (facemark) {
            case FACE_KEY_CHIN:
                result = pointArray.subList(0, 17);
                break;
            case FACE_KEY_LEFT_EYEBROW:
                result = pointArray.subList(17, 22);
                break;
            case FACE_KEY_RIGHT_EYEBROW:
                result = pointArray.subList(22, 27);
                break;
            case FACE_KEY_NOSE_BRIDGE:
                result = pointArray.subList(27, 31);
                break;
            case FACE_KEY_NOSE_TIP:
                result = pointArray.subList(31, 36);
                break;
            case FACE_KEY_LEFT_EYE:
                result = pointArray.subList(36, 42);
                break;
            case FACE_KEY_RIGHT_EYE:
                result = pointArray.subList(42, 48);
                break;
            case FACE_KEY_TOP_LIP:
                List<PointF> posTopPoint = pointArray.subList(48, 55);
                List<PointF> negTopPoint = new ArrayList<PointF>() {
                    {
                        this.add(pointArray.get(64));
                        this.add(pointArray.get(63));
                        this.add(pointArray.get(62));
                        this.add(pointArray.get(61));
                        this.add(pointArray.get(60));
                    }
                };
                result.addAll(posTopPoint);
                result.addAll(negTopPoint);
                break;
            case FACE_KEY_BOTTOM_LIP:
                List<PointF> posBottomPoint = pointArray.subList(54, 60);
                List<PointF> negBottomPoint = new ArrayList<PointF>() {
                    {
                        this.add(pointArray.get(48));
                        this.add(pointArray.get(60));
                        this.add(pointArray.get(67));
                        this.add(pointArray.get(66));
                        this.add(pointArray.get(65));
                        this.add(pointArray.get(64));
                    }
                };
                result.addAll(posBottomPoint);
                result.addAll(negBottomPoint);
                break;
        }
        return (PointF[]) result.toArray();
    }

    @Override
    public String toString() {
        return (null == mKeyPoints) ?
                "null" :
                "FaceInfo{" +
                        "mKeyPoints_size=" + mKeyPoints.length +
                        "mKeyPoints=" + Arrays.toString(mKeyPoints) +
                        '}';
    }
}
