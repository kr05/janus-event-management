package com.example.ritziercard9.projectjanus;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;
import ernestoyaquello.com.verticalstepperform.utils.Animations;

/**
 * Created by ritziercard9 on 12/3/2017.
 */

public class CustomVerticalStepperFormLayout extends VerticalStepperFormLayout {
    public static final String TAG = "CustomStepperFormLayout";
    protected boolean confirmationStepEnabled;
    protected boolean defaultNextButtonEnabled;
    protected boolean finalStepNextButtonEnabled;
    protected int totalNumberOfSteps;

    public CustomVerticalStepperFormLayout(Context context) {
        super(context);
    }

    public CustomVerticalStepperFormLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVerticalStepperFormLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void setSteps(String[] steps, String[] stepsSubtitles) {
        this.steps = new ArrayList<>(Arrays.asList(steps));
        if(stepsSubtitles != null) {
            this.stepsSubtitles = new ArrayList<>(Arrays.asList(stepsSubtitles));
        } else {
            this.stepsSubtitles = null;
        }
        numberOfSteps = steps.length;
        totalNumberOfSteps = numberOfSteps + (confirmationStepEnabled ? 1 : 0);

        Log.d(TAG, "setSteps: TOTAL NUMBER OF STEPS:" + totalNumberOfSteps);

        if (confirmationStepEnabled) {
            addConfirmationStepToStepsList();
        }

        setAuxVars();

        if (confirmationStepEnabled) {
            addConfirmationStepToStepsList();
            totalNumberOfSteps = steps.length + 1;
            Log.d(TAG, "setSteps: TOTAL NUMBER OF STEPS:" + totalNumberOfSteps);
        }

    }

    @Override
    protected void setUpSteps() {
        stepLayouts = new ArrayList<>();
        // Set up normal steps
        for (int i = 0; i < numberOfSteps; i++) {
            setUpStep(i);
        }

        if (confirmationStepEnabled) {
            // Set up confirmation step
            setUpStep(numberOfSteps);
        }
    }

    @Override
    protected void setUpStep(int stepNumber) {
        Log.d(TAG, "setUpStep: CUSTOM STEPPER METHOD BEING CALLED");
        LinearLayout stepLayout = createStepLayout(stepNumber);
        if (stepNumber < numberOfSteps) {
            // The content of the step is the corresponding custom view previously created
            RelativeLayout stepContent = stepLayout.findViewById(R.id.step_content);
            // start new
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            stepContent.addView(stepContentViews.get(stepNumber), params);
            // end new
        } else {
            setUpStepLayoutAsConfirmationStepLayout(stepLayout);
        }
        addStepToContent(stepLayout);
    }

    @Override
    protected LinearLayout createStepLayout(int stepNumber) {
        LinearLayout stepLayout = generateStepLayout();

        LinearLayout circle = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.circle);
        Drawable bg = ContextCompat.getDrawable(context, ernestoyaquello.com.verticalstepperform.R.drawable.circle_step_done);
        bg.setColorFilter(new PorterDuffColorFilter(
                stepNumberBackgroundColor, PorterDuff.Mode.SRC_IN));
        circle.setBackground(bg);

        TextView stepTitle = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_title);
        stepTitle.setText(steps.get(stepNumber));
        stepTitle.setTextColor(stepTitleTextColor);
        stepsTitlesViews.add(stepNumber, stepTitle);

        TextView stepSubtitle = null;
        if(stepsSubtitles != null && stepNumber < stepsSubtitles.size()) {
            String subtitle = stepsSubtitles.get(stepNumber);
            if(subtitle != null && !subtitle.equals("")) {
                stepSubtitle = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_subtitle);
                stepSubtitle.setText(subtitle);
                stepSubtitle.setTextColor(stepSubtitleTextColor);
                stepSubtitle.setVisibility(View.VISIBLE);
            }
        }
        stepsSubtitlesViews.add(stepNumber, stepSubtitle);

        TextView stepNumberTextView = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_number);
        stepNumberTextView.setText(String.valueOf(stepNumber + 1));
        stepNumberTextView.setTextColor(stepNumberTextColor);

        ImageView stepDoneImageView = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_done);
        stepDoneImageView.setColorFilter(stepNumberTextColor);

        TextView errorMessage = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.error_message);
        ImageView errorIcon = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.error_icon);
        errorMessage.setTextColor(errorMessageTextColor);
        errorIcon.setColorFilter(errorMessageTextColor);

        RelativeLayout stepHeader = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_header);
        Log.d(TAG, "------------------------------------");
        Log.d(TAG, "createStepLayout: STEP NUMBER:" + stepNumber);
        Log.d(TAG, "createStepLayout: TOTAL STEPS:" + totalNumberOfSteps);

        stepHeader.setOnClickListener(v -> goToStep(stepNumber, false));

        AppCompatButton nextButton = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.next_step);

        nextButton.setVisibility(defaultNextButtonEnabled ? VISIBLE : INVISIBLE);

        if (defaultNextButtonEnabled) {
            setButtonColor(nextButton,
                    buttonBackgroundColor, buttonTextColor, buttonPressedBackgroundColor, buttonPressedTextColor);
            if (stepNumber < totalNumberOfSteps - 1) {
                nextButton.setOnClickListener(v -> goToStep((stepNumber + 1), false));
            } else if (finalStepNextButtonEnabled){
                nextButton.setOnClickListener(view -> prepareSendingAndSend());
            } else {
                nextButton.setVisibility(INVISIBLE);
            }
        }
        stepLayouts.add(stepLayout);

        return stepLayout;
    }



    @Override
    protected void openStep(int stepNumber, boolean restoration) {
        Log.d(TAG, "---------------------------------");
        Log.d(TAG, "openStep: STEP NUMBER:" + stepNumber);
        Log.d(TAG, "openStep: TOTAL STEPS:" + totalNumberOfSteps);
        Log.d(TAG, "openStep: COMPLETED STEPS:" + completedSteps.length);
        if (stepNumber >= 0 && stepNumber < totalNumberOfSteps) {
            activeStep = stepNumber;
            Log.d(TAG, "openStep: ACTIVE STEP:" + activeStep);

            if (stepNumber == 0) {
                disablePreviousButtonInBottomNavigationLayout();
            } else {
                enablePreviousButtonInBottomNavigationLayout();
            }

            if (totalNumberOfSteps > 0 && activeStep != totalNumberOfSteps - 1 && completedSteps[stepNumber] ) {
                enableNextButtonInBottomNavigationLayout();
            } else {
                disableNextButtonInBottomNavigationLayout();
            }

            for(int i = 0; i < totalNumberOfSteps; i++) {
                if(i != stepNumber) {
                    disableStepLayout(i, !restoration);
                } else {
                    enableStepLayout(i, !restoration);
                }
            }

            scrollToActiveStep(!restoration);

            if (totalNumberOfSteps > 0 && stepNumber == totalNumberOfSteps - 1) {
                setStepAsCompleted(stepNumber);
            }

            verticalStepperFormImplementation.onStepOpening(stepNumber);
        }
    }


    @Override
    public void setActiveStepAsUncompleted(String errorMessage) {
        Log.d(TAG, "-----------------------------");
        Log.d(TAG, "setActiveStepAsUncompleted: ACTIVE STEP:" + activeStep);
        Log.d(TAG, "setActiveStepAsUncompleted: COMPLETED STEPS:" + completedSteps.length);
        setStepAsUncompleted(activeStep, errorMessage);
    }

    @Override
    public void setStepAsUncompleted(int stepNumber, String errorMessage) {
        Log.d(TAG, "------------------------");
        Log.d(TAG, "setStepAsUncompleted: STEP NUMBER:" + stepNumber);
        Log.d(TAG, "setStepAsUncompleted: COMPLETED STEPS:" + completedSteps.length);
        completedSteps[stepNumber] = false;

        LinearLayout stepLayout = stepLayouts.get(stepNumber);
        RelativeLayout stepHeader = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_header);
        ImageView stepDone = stepHeader.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_done);
        TextView stepNumberTextView = stepHeader.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_number);
        AppCompatButton nextButton = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.next_step);

        stepDone.setVisibility(View.INVISIBLE);
        stepNumberTextView.setVisibility(View.VISIBLE);

        nextButton.setEnabled(false);
        nextButton.setAlpha(alphaOfDisabledElements);

        if (stepNumber == activeStep) {
            disableNextButtonInBottomNavigationLayout();
        } else {
            disableStepHeader(stepLayout);
        }

//        if (stepNumber < numberOfSteps) {
//            setStepAsUncompleted(numberOfSteps, null);
//        }

        if (errorMessage != null && !errorMessage.equals("")) {
            LinearLayout errorContainer = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.error_container);
            TextView errorTextView = errorContainer.findViewById(ernestoyaquello.com.verticalstepperform.R.id.error_message);

            errorTextView.setText(errorMessage);
            //errorContainer.setVisibility(View.VISIBLE);
            Animations.slideDown(errorContainer);
        }

        displayCurrentProgress();
    }

    @Override
    protected void disableConfirmationButton() {
        if (confirmationStepEnabled) {
            confirmationButton.setEnabled(false);
            confirmationButton.setAlpha(alphaOfDisabledElements);
        }
    }

    @Override
    protected void scrollToActiveStep(boolean smoothScroll) {
        if (totalNumberOfSteps > 0) {
            scrollToStep(activeStep, smoothScroll);
        }
    }

    @Override
    protected void enableStepLayout(int stepNumber, boolean smoothieEnabling) {
        LinearLayout stepLayout = stepLayouts.get(stepNumber);
        RelativeLayout stepContent = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_content);
        RelativeLayout stepHeader = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_header);
        ImageView stepDone = stepHeader.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_done);
        TextView stepNumberTextView = stepHeader.findViewById(ernestoyaquello.com.verticalstepperform.R.id.step_number);
        LinearLayout button = stepLayout.findViewById(ernestoyaquello.com.verticalstepperform.R.id.next_step_button_container);

        enableStepHeader(stepLayout);

        if (smoothieEnabling) {
            Animations.slideDown(stepContent);
            Animations.slideDown(button);
        } else {
            stepContent.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
        }

        if (totalNumberOfSteps > 0 && completedSteps[stepNumber] && activeStep != stepNumber) {
            stepDone.setVisibility(View.VISIBLE);
            stepNumberTextView.setVisibility(View.INVISIBLE);
        } else {
            stepDone.setVisibility(View.INVISIBLE);
            stepNumberTextView.setVisibility(View.VISIBLE);
        }

        hideVerticalLineInCollapsedStepIfNecessary(stepLayout);
    }

    @Override
    protected void displayMaxProgress() {
        setProgress(totalNumberOfSteps);
    }

    @Override
    protected void setAuxVars() {
        completedSteps = new boolean[totalNumberOfSteps];
        for (int i = 0; i < totalNumberOfSteps; i++) {
            completedSteps[i] = false;
        }
        progressBar.setMax(totalNumberOfSteps);
    }

    @Override
    protected void setProgress(int progress) {
        if (progress > 0 && progress <= (totalNumberOfSteps)) {
            progressBar.setProgress(progress);
        }
    }


    public static class CustomBuilder {

        // Required parameters
        String[] steps;
        VerticalStepperForm verticalStepperFormImplementation;
        CustomVerticalStepperFormLayout verticalStepperFormLayout;
        Activity activity;

        String[] stepsSubtitles = null;
        float alphaOfDisabledElements = 0.25f;
        // Optional parameters
        int stepNumberBackgroundColor = Color.rgb(63, 81, 181);
        int buttonBackgroundColor = Color.rgb(63, 81, 181);
        int buttonPressedBackgroundColor = Color.rgb(48, 63, 159);
        int stepNumberTextColor = Color.rgb(255, 255, 255);
        int stepTitleTextColor = Color.rgb(33, 33, 33);
        int stepSubtitleTextColor = Color.rgb(162, 162, 162);
        int buttonTextColor = Color.rgb(255, 255, 255);
        int buttonPressedTextColor = Color.rgb(255, 255, 255);
        int errorMessageTextColor = Color.rgb(255, 148, 136);
        boolean displayBottomNavigation = true;
        boolean materialDesignInDisabledSteps = false;
        boolean hideKeyboard = true;
        boolean showVerticalLineWhenStepsAreCollapsed = false;

        //New parameters
        boolean confirmationStepEnabled = true;
        boolean defaultNextButtonEnabled = true;
        protected boolean finalStepNextButtonEnabled = true;

        CustomBuilder(CustomVerticalStepperFormLayout stepperLayout,
                          String[] steps,
                          VerticalStepperForm stepperImplementation,
                          Activity activity) {

            this.verticalStepperFormLayout = stepperLayout;
            this.steps = steps;
            this.verticalStepperFormImplementation = stepperImplementation;
            this.activity = activity;
        }

        /**
         * Generates an instance of the builder that will set up and initialize the form (after
         * setting up the form it is mandatory to initialize it calling init())
         * @param stepperLayout the form layout
         * @param stepTitles a String array with the names of the steps
         * @param stepperImplementation The instance that implements "VerticalStepperForm" interface
         * @param activity The activity where the form is
         * @return an instance of the builder
         */
        static CustomBuilder newInstance(CustomVerticalStepperFormLayout stepperLayout,
                                         String[] stepTitles,
                                         VerticalStepperForm stepperImplementation,
                                         Activity activity) {

            return new CustomBuilder(stepperLayout, stepTitles, stepperImplementation, activity);
        }

        /**
         * Set the subtitles of the steps
         * @param stepsSubtitles a String array with the subtitles of the steps
         * @return the builder instance
         */
        CustomBuilder stepsSubtitles(String[] stepsSubtitles) {
            this.stepsSubtitles = stepsSubtitles;
            return this;
        }

        /**
         * Set the primary color (background color of the left circles and buttons)
         * @param colorPrimary primary color
         * @return the builder instance
         */
        CustomBuilder primaryColor(int colorPrimary) {
            this.stepNumberBackgroundColor = colorPrimary;
            this.buttonBackgroundColor = colorPrimary;
            return this;
        }

        /**
         * Set the dark primary color (background color of the buttons when clicked)
         * @param colorPrimaryDark primary color (dark)
         * @return the builder instance
         */
        CustomBuilder primaryDarkColor(int colorPrimaryDark) {
            this.buttonPressedBackgroundColor = colorPrimaryDark;
            return this;
        }

        /**
         * Set the background color of the left circles
         * @param stepNumberBackgroundColor background color of the left circles
         * @return the builder instance
         */
        CustomBuilder stepNumberBackgroundColor(int stepNumberBackgroundColor) {
            this.stepNumberBackgroundColor = stepNumberBackgroundColor;
            return this;
        }

        /**
         * Set the background colour of the buttons
         * @param buttonBackgroundColor background color of the buttons
         * @return the builder instance
         */
        CustomBuilder buttonBackgroundColor(int buttonBackgroundColor) {
            this.buttonBackgroundColor = buttonBackgroundColor;
            return this;
        }

        /**
         * Set the background color of the buttons when clicked
         * @param buttonPressedBackgroundColor background color of the buttons when clicked
         * @return the builder instance
         */
        CustomBuilder buttonPressedBackgroundColor(int buttonPressedBackgroundColor) {
            this.buttonPressedBackgroundColor = buttonPressedBackgroundColor;
            return this;
        }

        /**
         * Set the text color of the left circles
         * @param stepNumberTextColor text color of the left circles
         * @return the builder instance
         */
        CustomBuilder stepNumberTextColor(int stepNumberTextColor) {
            this.stepNumberTextColor = stepNumberTextColor;
            return this;
        }

        /**
         * Set the text color of the step title
         * @param stepTitleTextColor the color of the step title
         * @return this builder instance
         */
        CustomBuilder stepTitleTextColor(int stepTitleTextColor) {
            this.stepTitleTextColor = stepTitleTextColor;
            return this;
        }

        /**
         * Set the text color of the step subtitle
         * @param stepSubtitleTextColor the color of the step title
         * @return this builder instance
         */
        CustomBuilder stepSubtitleTextColor(int stepSubtitleTextColor) {
            this.stepSubtitleTextColor = stepSubtitleTextColor;
            return this;
        }

        /**
         * Set the text color of the buttons
         * @param buttonTextColor text color of the buttons
         * @return the builder instance
         */
        CustomBuilder buttonTextColor(int buttonTextColor) {
            this.buttonTextColor = buttonTextColor;
            return this;
        }

        /**
         * Set the text color of the buttons when clicked
         * @param buttonPressedTextColor text color of the buttons when clicked
         * @return the builder instance
         */
        CustomBuilder buttonPressedTextColor(int buttonPressedTextColor) {
            this.buttonPressedTextColor = buttonPressedTextColor;
            return this;
        }

        /**
         * Set the error message color
         * @param errorMessageTextColor error message color
         * @return the builder instance
         */
        CustomBuilder errorMessageTextColor(int errorMessageTextColor) {
            this.errorMessageTextColor = errorMessageTextColor;
            return this;
        }

        /**
         * Set whether or not the bottom navigation bar will be displayed
         * @param displayBottomNavigationBar true to display it; false otherwise
         * @return the builder instance
         */
        CustomBuilder displayBottomNavigation(boolean displayBottomNavigationBar) {
            this.displayBottomNavigation = displayBottomNavigationBar;
            return this;
        }

        /**
         * Set whether or not the disabled steps will have a Material Design look
         * @param materialDesignInDisabledSteps true to use Material Design for disabled steps; false otherwise
         * @return the builder instance
         */
        CustomBuilder materialDesignInDisabledSteps(boolean materialDesignInDisabledSteps) {
            this.materialDesignInDisabledSteps = materialDesignInDisabledSteps;
            return this;
        }

        /**
         * Specify whether or not the keyboard should be hidden at the beginning
         * @param hideKeyboard true to hide the keyboard; false to not hide it
         * @return the builder instance
         */
        CustomBuilder hideKeyboard(boolean hideKeyboard) {
            this.hideKeyboard = hideKeyboard;
            return this;
        }

        /**
         * Specify whether or not the vertical lines should be displayed when steps are collapsed
         * @param showVerticalLineWhenStepsAreCollapsed true to show the lines; false to not
         * @return the builder instance
         */
        CustomBuilder showVerticalLineWhenStepsAreCollapsed(boolean showVerticalLineWhenStepsAreCollapsed) {
            this.showVerticalLineWhenStepsAreCollapsed = showVerticalLineWhenStepsAreCollapsed;
            return this;
        }

        /**
         * Set the alpha level of disabled elements
         * @param alpha alpha level of disabled elements (between 0 and 1)
         * @return the builder instance
         */
        public CustomBuilder alphaOfDisabledElements(float alpha) {
            this.alphaOfDisabledElements = alpha;
            return this;
        }

        /**
         * Specify whether an additional, last step to confirm and send data should be rendered.
         * Defaults to true
         *
         * @param enabled true to add the confirmation step
         * @return the builder instance
         */
        CustomBuilder confirmationStepEnabled(boolean enabled) {
            this.confirmationStepEnabled = enabled;
            return this;
        }

        /**
         * Specify whether to include a button with each step to navigate to the next step
         *
         * Defaults to true. Can be set to false if step views have their own next step navigation
         * controls
         *
         * @param enabled true to include the next button
         * @return the builder instance
         */
        CustomBuilder defaultNextButtonEnabled(boolean enabled) {
            this.defaultNextButtonEnabled = enabled;
            return this;
        }

        CustomBuilder finalStepNextButtonEnabled(boolean enabled) {
            this.finalStepNextButtonEnabled = enabled;
            return this;
        }

        /**
         * Set up the form and initialize it
         */
        void init() {
            verticalStepperFormLayout.initialiseVerticalStepperForm(this);
        }

    }

    protected void initialiseVerticalStepperForm(CustomBuilder builder) {
        this.verticalStepperFormImplementation = builder.verticalStepperFormImplementation;
        this.activity = builder.activity;

        this.alphaOfDisabledElements = builder.alphaOfDisabledElements;
        this.stepNumberBackgroundColor = builder.stepNumberBackgroundColor;
        this.buttonBackgroundColor = builder.buttonBackgroundColor;
        this.buttonPressedBackgroundColor = builder.buttonPressedBackgroundColor;
        this.stepNumberTextColor = builder.stepNumberTextColor;
        this.stepTitleTextColor = builder.stepTitleTextColor;
        this.stepSubtitleTextColor = builder.stepSubtitleTextColor;
        this.buttonTextColor = builder.buttonTextColor;
        this.buttonPressedTextColor = builder.buttonPressedTextColor;
        this.errorMessageTextColor = builder.errorMessageTextColor;
        this.displayBottomNavigation = builder.displayBottomNavigation;
        this.materialDesignInDisabledSteps = builder.materialDesignInDisabledSteps;
        this.hideKeyboard = builder.hideKeyboard;
        this.showVerticalLineWhenStepsAreCollapsed = builder.showVerticalLineWhenStepsAreCollapsed;
        this.defaultNextButtonEnabled = builder.defaultNextButtonEnabled;
        this.confirmationStepEnabled = builder.confirmationStepEnabled;
        this.finalStepNextButtonEnabled = builder.finalStepNextButtonEnabled;

        initStepperForm(builder.steps, builder.stepsSubtitles);
    }
}
