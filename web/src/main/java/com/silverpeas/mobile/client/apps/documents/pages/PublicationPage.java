package com.silverpeas.mobile.client.apps.documents.pages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.silverpeas.mobile.client.apps.comments.pages.widgets.CommentsButton;
import com.silverpeas.mobile.client.apps.documents.events.app.DocumentsLoadPublicationEvent;
import com.silverpeas.mobile.client.apps.documents.events.pages.publication.AbstractPublicationPagesEvent;
import com.silverpeas.mobile.client.apps.documents.events.pages.publication.PublicationLoadedEvent;
import com.silverpeas.mobile.client.apps.documents.events.pages.publication.PublicationNavigationPagesEventHandler;
import com.silverpeas.mobile.client.apps.documents.pages.widgets.Attachment;
import com.silverpeas.mobile.client.apps.documents.resources.DocumentsMessages;
import com.silverpeas.mobile.client.common.EventBus;
import com.silverpeas.mobile.client.common.Notification;
import com.silverpeas.mobile.client.common.app.View;
import com.silverpeas.mobile.client.components.UnorderedList;
import com.silverpeas.mobile.client.components.base.PageContent;
import com.silverpeas.mobile.shared.dto.comments.CommentDTO;
import com.silverpeas.mobile.shared.dto.documents.AttachmentDTO;
import com.silverpeas.mobile.shared.dto.documents.PublicationDTO;

public class PublicationPage extends PageContent implements View, PublicationNavigationPagesEventHandler {

  private static PublicationPageUiBinder uiBinder = GWT.create(PublicationPageUiBinder.class);

  protected DocumentsMessages msg = null;
  private PublicationDTO publication;

  @UiField HeadingElement title;
  @UiField HTMLPanel container;
  @UiField UnorderedList attachments;
  @UiField ParagraphElement desc, lastUpdate;
  @UiField CommentsButton comments;

  interface PublicationPageUiBinder extends UiBinder<Widget, PublicationPage> {
  }

  public PublicationPage() {
    msg = GWT.create(DocumentsMessages.class);
    initWidget(uiBinder.createAndBindUi(this));
    container.getElement().setId("publication");
    attachments.getElement().setId("attachments");
    EventBus.getInstance().addHandler(AbstractPublicationPagesEvent.TYPE, this);
  }

  @Override
  public void stop() {
    super.stop();
    comments.stop();
    EventBus.getInstance().removeHandler(AbstractPublicationPagesEvent.TYPE, this);
  }

  public void setPublicationId(String id) {
    // send event to controler for retrieve pub infos
    Notification.activityStart();
    EventBus.getInstance().fireEvent(new DocumentsLoadPublicationEvent(id));
  }

  @Override
  public void onLoadedPublication(PublicationLoadedEvent event) {
    Notification.activityStop();
    this.publication = event.getPublication();
    display(event.isCommentable());
  }

  /**
   * Refesh view informations.
   */
  private void display(boolean commentable) {
    title.setInnerHTML(publication.getName());
    desc.setInnerHTML(publication.getDescription());
    lastUpdate.setInnerHTML(msg.lastUpdate(publication.getUpdateDate(), publication.getUpdater()));

    for (AttachmentDTO attachment : publication.getAttachments()) {
      Attachment a = new Attachment();
      a.setAttachment(attachment);
      attachments.add(a);
    }
    if (commentable) {
      comments.init(publication.getId(), publication.getInstanceId(), CommentDTO.TYPE_PUBLICATION,
          getPageTitle(), publication.getName(), publication.getCommentsNumber());
      comments.getElement().getStyle().clearDisplay();
    } else {
      comments.getElement().getStyle().setDisplay(Style.Display.NONE);
    }
  }
}
