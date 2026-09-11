package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.util.AppClock;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

public abstract class AbstractPresenter<V extends Region> {
    protected StudentClockFacade facade;
    protected V view;
    protected Scene scene;
    protected AppClock clock;

    public AbstractPresenter(StudentClockFacade facade, V view, AppClock clock) {
        this.facade = facade;
        this.view = view;
        this.clock = clock;
        this.scene = new Scene(view, 500, 600);
    }

    public abstract String getTitle();
    public abstract void refresh();
    
    public Scene getScene() {
        return this.scene;
    }
}