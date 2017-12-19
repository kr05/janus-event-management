package com.example.ritziercard9.projectjanus;

import android.app.Activity;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;

/**
 * Created by ritziercard9 on 12/4/2017.
 */

public class CustomStepperBuilder extends VerticalStepperFormLayout.Builder {
    protected CustomStepperBuilder(VerticalStepperFormLayout stepperLayout, String[] steps, VerticalStepperForm stepperImplementation, Activity activity) {
        super(stepperLayout, steps, stepperImplementation, activity);
    }


}
