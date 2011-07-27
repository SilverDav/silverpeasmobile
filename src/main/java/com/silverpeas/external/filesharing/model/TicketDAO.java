/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.external.filesharing.model;


import com.silverpeas.external.filesharing.model.DownloadDetail;
import com.silverpeas.external.filesharing.model.TicketDetail;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TicketDAO {

  public List<TicketDetail> getTicketsByUser(Connection con, String userId) throws SQLException {
    // récupérer toutes les tickets d'un utilisateur
    ArrayList<TicketDetail> tickets = null;

    String query = "select * from SB_fileSharing_ticket where creatorId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, userId);
      rs = prepStmt.executeQuery();
      tickets = new ArrayList<TicketDetail>();
      while (rs.next()) {
        TicketDetail ticket = recupTicket(rs);
        tickets.add(ticket);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return tickets;
  }

  public List<TicketDetail> getTicketsByComponentIds(Connection con, List<String> componentIds)
      throws SQLException {
    // récupérer toutes les tickets d'un utilisateur
    ArrayList<TicketDetail> tickets = null;

    StringBuilder query = new StringBuilder("SELECT * FROM sb_fileSharing_ticket");
    query.append(" where componentId IN (");
    for (int c = 0; c < componentIds.size(); c++) {
      if (c != 0) {
        query.append(",");
      }
      query.append(componentIds.get(c));
    }
    query.append(")");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query.toString());
      rs = prepStmt.executeQuery();
      tickets = new ArrayList<TicketDetail>();
      while (rs.next()) {
        TicketDetail ticket = recupTicket(rs);
        tickets.add(ticket);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return tickets;
  }

  public TicketDetail getTicket(Connection con, String key) throws SQLException {
    // récupérer un ticket
    TicketDetail ticket = null;
    String query = "select * from SB_fileSharing_ticket where keyFile = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // recuperation des colonnes du resulSet et construction de l'objet photo
        ticket = recupTicket(rs);
      }
      // récupérer la liste des téléchargements
      ticket.setDownloads(getAllDownloads(con, key));
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return ticket;
  }

  public void deleteTicketsByFile(Connection con, String fileId, boolean versioning)
      throws SQLException {
    String query = "select keyFile from SB_fileSharing_ticket where fileId = ? and versioning = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(fileId));
      if (versioning) {
        prepStmt.setString(2, "1");
      } else {
        prepStmt.setString(2, "0");
      }
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String key = rs.getString("keyFile");
        deleteTicket(con, key);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
  }

  public List<DownloadDetail> getAllDownloads(Connection con, String key) throws SQLException {
    // récupérer tous les downloads sur un ticket
    List<DownloadDetail> downloads = new ArrayList<DownloadDetail>();

    String query = "select * from SB_fileSharing_history where keyFile = ?";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        DownloadDetail download = recupDownload(rs);
        downloads.add(download);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return downloads;
  }

  public String createTicket(Connection con, TicketDetail ticket) throws SQLException {
    // Création d'une commande
    String key = createUniKey();
    PreparedStatement prepStmt = null;
    try {
      Date today = new Date();
      // création de la requête
      String query =
          "insert into SB_fileSharing_ticket (fileId, componentId, versioning, creatorId, creationDate, endDate, nbAccessMax, keyFile)"
          + " values (?,?,?,?,?,?,?,?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, ticket.getFileId());
      prepStmt.setString(2, ticket.getComponentId());
      prepStmt.setString(3, "0");
      if (ticket.isVersioned()) {
        prepStmt.setString(3, "1");
      }
      prepStmt.setString(4, ticket.getCreator().getId());
      prepStmt.setString(5, "" + today.getTime());
      Date endDate = ticket.getEndDate();
      if (endDate != null) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        prepStmt.setString(6, java.lang.Long.toString(calendar.getTime().getTime()));
      } else {
        prepStmt.setString(6, null);
      }
      prepStmt.setInt(7, ticket.getNbAccessMax());
      prepStmt.setString(8, key);
      prepStmt.executeUpdate();
    } catch (Exception e) {
      SilverTrace.error("fileSharing", getClass().getSimpleName() + ".createTicket",
          "root.EX_NO_MESSAGE", e);
      return null;
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
    return key;
  }

  public void updateTicket(Connection con, TicketDetail ticket) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      Date today = new Date();
      String query =
          "update SB_fileSharing_ticket set fileId = ? , componentId = ? , updateId = ? , updateDate = ? , "
          + "endDate = ? , nbAccessMax = ? , nbAccess = ? where keyfile = ? ";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, ticket.getFileId());
      prepStmt.setString(2, ticket.getComponentId());
      if (ticket.getLastModifier() != null) {
        prepStmt.setString(3, ticket.getLastModifier().getId());
        prepStmt.setString(4, "" + today.getTime());
      } else {
        prepStmt.setString(3, null);
        prepStmt.setString(4, null);
      }
      Date endDate = ticket.getEndDate();
      if (endDate != null) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        prepStmt.setString(5, java.lang.Long.toString(calendar.getTime().getTime()));
      } else {
        prepStmt.setString(5, null);
      }
      prepStmt.setInt(6, ticket.getNbAccessMax());
      prepStmt.setInt(7, ticket.getNbAccess());
      prepStmt.setString(8, ticket.getKeyFile());

      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public void addDownload(Connection con, DownloadDetail download) throws SQLException,
      UtilException {
    // Ajout d'un téléchargement
    PreparedStatement prepStmt = null;
    try {
      // création de la requête
      String query = "insert into SB_fileSharing_history (id, keyfile, downloadDate, downloadIp)"
          + " values (?,?,?,?)";
      // initialisation des paramètres
      int id = DBUtil.getNextId("SB_fileSharing_history", "id");
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, download.getKeyFile());
      prepStmt.setString(3, "" + download.getDownloadDate().getTime());
      prepStmt.setString(4, download.getUserIP());
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  private void deleteDownloads(Connection con, String key) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // création de la requête
      String query = "delete from SB_fileSharing_history where keyfile = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public void deleteTicket(Connection con, String key) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // delete according history
      deleteDownloads(con, key);

      String query = "delete from SB_fileSharing_ticket where keyFile = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, key);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static String createUniKey() {
    return UUID.randomUUID().toString().substring(0, 32);
  }

  /**
   * Recuperation des colonnes du resulSet et construction de l'objet ticket
   * @param rs
   * @return
   * @throws SQLException
   */
  protected TicketDetail recupTicket(ResultSet rs) throws SQLException {

    int fileId = rs.getInt("fileId");
    String componentId = rs.getString("componentId");
    boolean versioning = "1".equals(rs.getString("versioning"));
    String creatorId = rs.getString("creatorId");
    String updateId = rs.getString("updateId");
    Date creationDate = null;
    if (StringUtil.isDefined(rs.getString("creationDate"))) {
      creationDate = new Date(Long.parseLong(rs.getString("creationDate")));
    }
    Date endDate = null;
    String dateInMillis = rs.getString("endDate");
    if (StringUtil.isDefined(dateInMillis)) {
      endDate = new Date(Long.parseLong(dateInMillis));
    }
    int nbMaxAccess = rs.getInt("NbAccessMax");

    UserDetail creator = new UserDetail();
    creator.setId(creatorId);
    TicketDetail ticket = TicketDetail.aTicket(fileId, componentId, versioning, creator,
        creationDate, endDate, nbMaxAccess);

    if (StringUtil.isDefined(updateId)) {
      UserDetail updater = new UserDetail();
      updater.setId(updateId);
      ticket.setLastModifier(updater);
    }
    String updateDate = null;
    if (StringUtil.isDefined(rs.getString("updateDate"))) {
      updateDate = rs.getString("updateDate");
      ticket.setUpdateDate(new Date(Long.parseLong(updateDate)));
    }
    ticket.setNbAccess(rs.getInt("NbAccess"));
    ticket.setKeyFile(rs.getString("KeyFile"));

    return ticket;
  }

  /**
   * Recuperation des colonnes du resulSet et construction de l'objet download
   * @param rs
   * @return
   * @throws SQLException
   */
  protected DownloadDetail recupDownload(ResultSet rs) throws SQLException {
    DownloadDetail download = new DownloadDetail();
    download.setId(rs.getInt("id"));
    download.setKeyFile(rs.getString("keyFile"));
    String downloadDate = rs.getString("downloadDate");
    if (StringUtil.isDefined(downloadDate)) {
      try {
        download.setDownloadDate(new Date(Long.parseLong(downloadDate)));
      } catch (NumberFormatException e) {
        download.setDownloadDate(new Date());
      }

    } else {
      download.setDownloadDate(new Date());
    }
    download.setUserIP(rs.getString("downloadIp"));

    return download;
  }
}
