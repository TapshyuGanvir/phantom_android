package com.sixsimplex.phantom.revelocore.phaseDetails.view;

public interface IPhaseSelection {
    void onPhaseSelected(String surveyName, String phaseName);
    void onPhaseSelectionCancelled();
}
