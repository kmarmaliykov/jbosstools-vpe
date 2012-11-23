package by.buziuk.di.logic;
import javax.inject.Inject;

public class DIClass {
	
	@Inject
	private Implementation implementation;
	
	public Implementation getImplementation() {
		return implementation;
	}

	public void setImplementation(Implementation implementation) {
		this.implementation = implementation;
	}
}
