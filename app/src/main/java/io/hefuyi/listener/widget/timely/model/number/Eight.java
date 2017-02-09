package io.hefuyi.listener.widget.timely.model.number;


import io.hefuyi.listener.widget.timely.model.core.Figure;

public class Eight extends Figure {
    private static final float[][] POINTS = {
            {0.558011049723757f, 0.530386740331492f}, {0.243093922651934f, 0.524861878453039f}, {0.243093922651934f, 0.104972375690608f},
            {0.558011049723757f, 0.104972375690608f}, {0.850828729281768f, 0.104972375690608f}, {0.850828729281768f, 0.530386740331492f},
            {0.558011049723757f, 0.530386740331492f}, {0.243093922651934f, 0.530386740331492f}, {0.198895027624309f, 0.988950276243094f},
            {0.558011049723757f, 0.988950276243094f}, {0.850828729281768f, 0.988950276243094f}, {0.850828729281768f, 0.530386740331492f},
            {0.558011049723757f, 0.530386740331492f}
    };

    private static Eight INSTANCE = new Eight();

    private Eight() {
        super(POINTS);
    }

    public static Eight getInstance() {
        return INSTANCE;
    }
}