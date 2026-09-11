package edu.ucsd.studentclock.presenter;

import javafx.stage.Stage;

public class PresenterManager {
    
    public void defineInteractions(Stage stage, String appName,
                                  TermPagePresenter termPresenter,
                                  DashboardPresenter dashboardPresenter,
                                  BigPicturePresenter bigPicturePresenter) {
        
        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        termPresenter.setOnNavigateToDashboard(() -> {
            dashboardPresenter.refresh();
            switcher.switchTo(dashboardPresenter);
        });

        dashboardPresenter.setOnNavigateBack(() -> {
            termPresenter.refresh();
            switcher.switchTo(termPresenter);
        });

        Runnable refreshBigPicture = () -> bigPicturePresenter.refresh();

        dashboardPresenter.setOnNavigateToBigPicture(() -> {
            refreshBigPicture.run();
            switcher.switchTo(bigPicturePresenter);
        });

        dashboardPresenter.setOnBigPictureRefresh(refreshBigPicture);
        termPresenter.setOnBigPictureRefresh(refreshBigPicture);

        bigPicturePresenter.setOnNavigateBack(() -> {
            dashboardPresenter.refresh();
            switcher.switchTo(dashboardPresenter);
        });

        switcher.switchTo(termPresenter);
    }
}