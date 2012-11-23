package by.buziuk.di.views;

import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.*;

import by.buziuk.di.logic.DIClass;
import by.buziuk.di.logic.Implementation;

public class SampleView extends ViewPart {
	
	private DIClass clazz;

	private Label label;
	
	public static final String ID = "by.buziuk.di.views.SampleView";
    
	@Override
	public void createPartControl(Composite parent) {
		label = new Label(parent, SWT.NONE);
		IInjector injector = InjectorFactory.getDefault();
		injector.addBinding(Implementation.class);//.implementedBy(Implementation.class);
		clazz = injector.make(DIClass.class, null);
		label.setText(clazz.getImplementation().doSomething());
	}

	@Override
	public void setFocus() {
		label.setFocus();
	}

	public DIClass getClazz() {
		return clazz;
	}

	public void setClazz(DIClass clazz) {
		this.clazz = clazz;
	}
}