package com.silverpeas.mobile.shared.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.silverpeas.mobile.shared.dto.BaseDTO;
import com.silverpeas.mobile.shared.dto.media.PhotoDTO;
import com.silverpeas.mobile.shared.dto.navigation.ApplicationInstanceDTO;
import com.silverpeas.mobile.shared.exceptions.AuthenticationException;
import com.silverpeas.mobile.shared.exceptions.MediaException;

@RemoteServiceRelativePath("Media")
public interface ServiceMedia extends RemoteService {
	public void uploadPicture(String name, String data, String idGallery, String idAlbum) throws
                                                                                        MediaException, AuthenticationException;
	public List<ApplicationInstanceDTO> getAllGalleries() throws MediaException, AuthenticationException;
	public List<BaseDTO> getAlbumsAndPictures(String instanceId, String albumId) throws
                                                                               MediaException, AuthenticationException;
	public PhotoDTO getOriginalPicture(String instanceId, String pictureId) throws MediaException, AuthenticationException;
	public PhotoDTO getPreviewPicture(String instanceId, String pictureId) throws MediaException, AuthenticationException;
}
