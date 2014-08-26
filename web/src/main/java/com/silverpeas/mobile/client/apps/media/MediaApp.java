package com.silverpeas.mobile.client.apps.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.silverpeas.mobile.client.apps.documents.events.pages.navigation.GedItemsLoadedEvent;
import com.silverpeas.mobile.client.apps.media.events.app.AbstractMediaAppEvent;
import com.silverpeas.mobile.client.apps.media.events.app.MediaAppEventHandler;
import com.silverpeas.mobile.client.apps.media.events.app.MediasLoadMediaItemsEvent;
import com.silverpeas.mobile.client.apps.media.events.pages.navigation.MediaItemsLoadedEvent;
import com.silverpeas.mobile.client.apps.media.pages.MediaNavigationPage;
import com.silverpeas.mobile.client.apps.media.resources.GalleryMessages;
import com.silverpeas.mobile.client.apps.navigation.Apps;
import com.silverpeas.mobile.client.apps.navigation.NavigationApp;
import com.silverpeas.mobile.client.apps.navigation.events.app.external.AbstractNavigationEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.external.NavigationAppInstanceChangedEvent;
import com.silverpeas.mobile.client.apps.navigation.events.app.external.NavigationEventHandler;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.ServicesLocator;
import com.silverpeas.mobile.client.common.app.App;
import com.silverpeas.mobile.client.common.event.ErrorEvent;
import com.silverpeas.mobile.shared.dto.BaseDTO;

import java.util.List;

public class MediaApp extends App implements NavigationEventHandler, MediaAppEventHandler {
	
	private GalleryMessages msg;
	private NavigationApp navApp = new NavigationApp();
	
	public MediaApp() {
		super();
		msg = GWT.create(GalleryMessages.class);
		EventBus.getInstance().addHandler(AbstractMediaAppEvent.TYPE, this);
		EventBus.getInstance().addHandler(AbstractNavigationEvent.TYPE, this);
	}

	@Override
	public void start() {			
		navApp.setTypeApp(Apps.gallery.name());
		navApp.setTitle(msg.title());
		
		navApp.start();
		
		// app main is navigation app main page
		setMainPage(navApp.getMainPage());
		
		super.start();
	}

	@Override
	public void stop() {
		EventBus.getInstance().removeHandler(AbstractMediaAppEvent.TYPE, this);
		EventBus.getInstance().removeHandler(AbstractNavigationEvent.TYPE, this);
		navApp.stop();
		super.stop();		
	}

	@Override
	public void appInstanceChanged(NavigationAppInstanceChangedEvent event) {
		MediaNavigationPage page = new MediaNavigationPage();
		page.setPageTitle(msg.title());				
		page.setInstanceId(event.getInstance().getId());
		page.setAlbumId(null);
		page.show();	
		
	}

  @Override
  public void loadAlbums(final MediasLoadMediaItemsEvent event) {
    ServicesLocator.serviceGallery.getAlbumsAndPictures(event.getInstanceId(),
        event.getRootAlbumId(), new AsyncCallback<List<BaseDTO>>() {
          @Override
          public void onSuccess(List<BaseDTO> result) {
            EventBus.getInstance().fireEvent(new MediaItemsLoadedEvent(result));
          }

          @Override
          public void onFailure(Throwable caught) {
            EventBus.getInstance().fireEvent(new ErrorEvent(new Exception(caught)));
          }
        });
  }
}