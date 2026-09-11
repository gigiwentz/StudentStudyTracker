package edu.ucsd.studentclock;

import edu.ucsd.studentclock.datasource.ITermDataSource;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.facade.StudentClockFacade;
import edu.ucsd.studentclock.facade.StudentClockFacadeImpl;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Term;
import edu.ucsd.studentclock.presenter.*;
import edu.ucsd.studentclock.repository.ExampleRepository;
import edu.ucsd.studentclock.util.AppClock;
import edu.ucsd.studentclock.view.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("APP STARTED");
        
        ITermDataSource dataSource = new SqlDataSource();
        ExampleRepository repository = new ExampleRepository(dataSource);
        Term term = repository.loadTerm();
        
        if (term == null) {
            term = Term.createDefault();
            repository.saveTerm(term);
        }
        
        Model sharedModel = new Model(repository, term);
        AppClock clock = new AppClock();
        
        StudentClockFacade facade = new StudentClockFacadeImpl(sharedModel, clock);

        TermView termView = new TermView();
        DashboardView dashboardView = new DashboardView();
        BigPictureView bigPictureView = new BigPictureView();

        TermPagePresenter termPresenter = new TermPagePresenter(facade, termView, clock);
        DashboardPresenter dashboardPresenter = new DashboardPresenter(facade, dashboardView, clock);
        BigPicturePresenter bigPicturePresenter = new BigPicturePresenter(facade, bigPictureView, clock);

        PresenterManager manager = new PresenterManager();
        manager.defineInteractions(primaryStage, "Student Clock", 
                                  termPresenter, dashboardPresenter, bigPicturePresenter);
    }

    public static void main(String[] args) {
        launch(args);
    }
}