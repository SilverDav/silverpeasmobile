package com.silverpeas.mobile.client.apps.navigation;

import com.silverpeas.mobile.client.apps.navigation.events.app.AbstractNavigationEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.NavigationAppInstanceChangedEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.NavigationEventHandler;
import com.silverpeas.mobile.client.apps.navigation.pages.NavigationPage;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.app.App;
import com.silverpeas.mobile.client.components.base.PageContent;

public class NavigationApp extends App implements NavigationEventHandler {

	private String type, title;
	
	public NavigationApp() {
		super();		
		EventBus.getInstance().addHandler(AbstractNavigationEvent.TYPE, this);
	}

	@Override
	public void start(PageContent lauchingPage) {
		setController(new NavigationController(type));
		NavigationPage mainPage = new NavigationPage();
		mainPage.setPageTitle(title);
		mainPage.setRootSpaceId(null);
		setMainPage(mainPage);		
		// no "super.start(lauchingPage);" this app is used in another app 
	}

	@Override
	public void stop() {		
		EventBus.getInstance().removeHandler(AbstractNavigationEvent.TYPE, this);
		super.stop();
	}
	
	public void setTypeApp(String type) {
		this.type = type;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void appInstanceChanged(NavigationAppInstanceChangedEvent event) {
		stop();
	}	
}
