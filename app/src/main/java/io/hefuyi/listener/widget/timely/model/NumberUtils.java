package io.hefuyi.listener.widget.timely.model;


import java.security.InvalidParameterException;

import io.hefuyi.listener.widget.timely.model.number.Eight;
import io.hefuyi.listener.widget.timely.model.number.Five;
import io.hefuyi.listener.widget.timely.model.number.Four;
import io.hefuyi.listener.widget.timely.model.number.Nine;
import io.hefuyi.listener.widget.timely.model.number.Null;
import io.hefuyi.listener.widget.timely.model.number.One;
import io.hefuyi.listener.widget.timely.model.number.Seven;
import io.hefuyi.listener.widget.timely.model.number.Six;
import io.hefuyi.listener.widget.timely.model.number.Three;
import io.hefuyi.listener.widget.timely.model.number.Two;
import io.hefuyi.listener.widget.timely.model.number.Zero;

public class NumberUtils {

    public static float[][] getControlPointsFor(int start) {
        switch (start) {
            case (-1):
                return Null.getInstance().getControlPoints();
            case 0:
                return Zero.getInstance().getControlPoints();
            case 1:
                return One.getInstance().getControlPoints();
            case 2:
                return Two.getInstance().getControlPoints();
            case 3:
                return Three.getInstance().getControlPoints();
            case 4:
                return Four.getInstance().getControlPoints();
            case 5:
                return Five.getInstance().getControlPoints();
            case 6:
                return Six.getInstance().getControlPoints();
            case 7:
                return Seven.getInstance().getControlPoints();
            case 8:
                return Eight.getInstance().getControlPoints();
            case 9:
                return Nine.getInstance().getControlPoints();
            default:
                throw new InvalidParameterException("Unsupported number requested");
        }
    }
}
