package cn.co.willow.android.face;

import android.graphics.PointF;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 单张人脸数据
 * <p>
 * Created by Willow.Li on 2019/3/26
 */
public class FaceInfo {

    public final static String FACE_KEY_CHIN = "chin";
    public final static String FACE_KEY_LEFT_EYEBROW = "left_eyebrow";
    public final static String FACE_KEY_RIGHT_EYEBROW = "right_eyebrow";
    public final static String FACE_KEY_NOSE_BRIDGE = "nose_bridge";
    public final static String FACE_KEY_NOSE_TIP = "nose_tip";
    public final static String FACE_KEY_LEFT_EYE = "left_eye";
    public final static String FACE_KEY_RIGHT_EYE = "right_eye";
    public final static String FACE_KEY_TOP_LIP = "top_lip";
    public final static String FACE_KEY_BOTTOM_LIP = "bottom_lip";

    private List<PointF> mKeyPoints;        // 脸部特征点数据集

    public FaceInfo(List<PointF> mKeyPoints) {
        this.mKeyPoints = mKeyPoints;
    }

    /**
     * 68点关键参数检索方法
     *
     * @param facemark 检索脸部位置
     * @return 对应位置特征点数据集
     */
    public List<PointF> getCertainFacemark68(String facemark) {
        if (null == mKeyPoints || TextUtils.isEmpty(facemark)) {
            return mKeyPoints;
        }
        List<PointF> result = new ArrayList<>();
        switch (facemark) {
            case FACE_KEY_CHIN:
                result = mKeyPoints.subList(0, 17);
                break;
            case FACE_KEY_LEFT_EYEBROW:
                result = mKeyPoints.subList(17, 22);
                break;
            case FACE_KEY_RIGHT_EYEBROW:
                result = mKeyPoints.subList(22, 27);
                break;
            case FACE_KEY_NOSE_BRIDGE:
                result = mKeyPoints.subList(27, 31);
                break;
            case FACE_KEY_NOSE_TIP:
                result = mKeyPoints.subList(31, 36);
                break;
            case FACE_KEY_LEFT_EYE:
                result = mKeyPoints.subList(36, 42);
                break;
            case FACE_KEY_RIGHT_EYE:
                result = mKeyPoints.subList(42, 48);
                break;
            case FACE_KEY_TOP_LIP:
                List<PointF> posTopPoint = mKeyPoints.subList(48, 55);
                List<PointF> negTopPoint = new ArrayList<PointF>() {
                    {
                        this.add(mKeyPoints.get(64));
                        this.add(mKeyPoints.get(63));
                        this.add(mKeyPoints.get(62));
                        this.add(mKeyPoints.get(61));
                        this.add(mKeyPoints.get(60));
                    }
                };
                result.addAll(posTopPoint);
                result.addAll(negTopPoint);
                break;
            case FACE_KEY_BOTTOM_LIP:
                List<PointF> posBottomPoint = mKeyPoints.subList(54, 60);
                List<PointF> negBottomPoint = new ArrayList<PointF>() {
                    {
                        this.add(mKeyPoints.get(48));
                        this.add(mKeyPoints.get(60));
                        this.add(mKeyPoints.get(67));
                        this.add(mKeyPoints.get(66));
                        this.add(mKeyPoints.get(65));
                        this.add(mKeyPoints.get(64));
                    }
                };
                result.addAll(posBottomPoint);
                result.addAll(negBottomPoint);
                break;
        }
        return result;
    }

}
