package com.example.ritziercard9.projectjanus;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Created by ritziercard9 on 12/2/2017.
 */

public class CustomHorizontalLinearLayoutManager extends LinearLayoutManager {
    public CustomHorizontalLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }
}

