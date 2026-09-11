package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.service.BigPictureDataBuilder;
import edu.ucsd.studentclock.util.AppClock;
import edu.ucsd.studentclock.view.BigPictureView;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> {
    private final AppClock clock;
    private Runnable onNavigateBack;

    public BigPicturePresenter(StudentClockFacade facade, BigPictureView view, AppClock clock) {
        super(facade, view, clock);
        this.clock = clock;
        
        view.getBackButton().setOnAction(e -> {
            if (onNavigateBack != null) onNavigateBack.run();
        });
        
        refresh();
    }

    public void setOnNavigateBack(Runnable action) {
        this.onNavigateBack = action;
    }

    @Override
    public String getTitle() {
        return "Big Picture";
    }

    @Override
    public void refresh() {
        facade.updateAllActive(clock.now());
        BigPictureDataBuilder.Result result = BigPictureDataBuilder.build(facade, clock.now());
        view.render(result);
    }
}