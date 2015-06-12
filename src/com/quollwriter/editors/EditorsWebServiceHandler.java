package com.quollwriter.editors;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.activation.*;

import org.jdom.*;

import org.bouncycastle.openpgp.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.data.editors.*;

public class EditorsWebServiceHandler
{
    
    public static final int MAX_IMAGE_SIZE = 300 * 1024;
    
    private boolean operationInProgress = false;
    private boolean inited = false;
    private java.util.Timer invitesListener = null;
    private DefaultEditorsWebServiceErrorAction defaultCallErrorAction = null;
    
    public class FieldNames
    {
        
        public static final String genres = "genres";
        public static final String email = "email";
        public static final String id = "id";
        public static final String password = "password";
        public static final String lastModified = "lastModified";
        public static final String avatarImage = "avatarImage";
        public static final String avatarImageFileType = "avatarImageFileType";
        public static final String about = "about";
        public static final String wordCountLengths = "wordCountLengths";
        public static final String wordCountLength = "wordCountLength";
        public static final String name = "name";
        public static final String description = "description";
        public static final String expectations = "expectations";
        public static final String status = "status";
        public static final String received = "received";
        public static final String publicKey = "publicKey";
        public static final String beforeAccountCreation = "beforeAccountCreation";
        
    }
    
    public enum Service
    {
        
        accounts ("accounts", Environment.getProperty (Constants.ACCOUNTS_WEBSERVICE_URL_PROPERTY_NAME)),
        authors ("authors", Environment.getProperty (Constants.AUTHORS_WEBSERVICE_URL_PROPERTY_NAME)),
        projects ("projects", Environment.getProperty (Constants.PROJECTS_WEBSERVICE_URL_PROPERTY_NAME)),
        login ("login", Environment.getProperty (Constants.LOGIN_WEBSERVICE_URL_PROPERTY_NAME)),
        editors ("editors", Environment.getProperty (Constants.EDITORS_WEBSERVICE_URL_PROPERTY_NAME)),
        sessions ("sessions", Environment.getProperty (Constants.SESSIONS_WEBSERVICE_URL_PROPERTY_NAME)),
        invites ("invites", Environment.getProperty (Constants.INVITES_WEBSERVICE_URL_PROPERTY_NAME));
        
        private final String type;
        private final String url;
        
        Service (String type,
                 String url)
        {
            
            this.type = type;
            this.url = url;
            
        }
        
        public String getURL ()
        {
            
            return this.url;
            
        }
        
    }
    
    public EditorsWebServiceHandler ()
    {
         
        this.defaultCallErrorAction = new DefaultEditorsWebServiceErrorAction (this);
                        
    }
        
    public EditorsWebServiceAction getDefaultEditorsWebServiceErrorAction ()
    {
     
        return this.defaultCallErrorAction;
        
    }        
        
    public void init ()
    {
        
        if (this.inited)
        {
            
            return;
            
        }
        
        if (this.operationInProgress)
        {
            
            return;
            
        }
        
        final EditorsWebServiceHandler _this = this;
        
/*
            new Thread (new Runnable ()
            {
                
                public void run ()
                {
                    
                    _this.operationInProgress = true;
                                
                    // Login and get the author information.
                    try
                    {
                    
                        _this.loadAuthor ();
                                                
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to load author information",
                                              e);
                        
                    }
                    
                    try
                    {
                                            
                        _this.loadEditor ();
                                                
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to load editor information",
                                              e);
                        
                    }

                    _this.inited = true;
                    
                    _this.operationInProgress = false;
                    
                }
                
            }).start ();
            */
                            
    }
        
    private void showInviteFromEditor (final Invite invite)
    {
        
        final EditorEditor ed = EditorsEnvironment.getEditorByEmail (invite.getFromEmail ());
        
        if (ed == null)
        {
            
            Environment.logError ("Unable to find editor for email: " +
                                  invite.getFromEmail ());
            
            return;
            
        }
        
        EditorsUIUtils.showInviteFromEditor (invite.getFromEmail (),
                                             new ActionListener ()
        {
           // The user wants to accept the invite.
           public void actionPerformed (ActionEvent ev)
           {
               
                InviteResponseMessage rm = new InviteResponseMessage (true,
                                                                      EditorsEnvironment.getUserAccount ());
                rm.setEditor (ed);
                
                EditorsEnvironment.acceptInvite (ed,
                                                 rm,
                                                 null);
               /*
                EditorsEnvironment.updateInvite (invite.getFromEmail (),
                                                 Invite.Status.accepted,
                new ActionListener ()
                {
                   
                   public void actionPerformed (ActionEvent ev)
                   {
                       
                        EditorEditor ed = EditorsEnvironment.getEditorByEmail (invite.getFromEmail ());
                        
                        if (ed == null)
                        {
                            
                            Environment.logError ("How did we get here, no editor for: " +
                                                  invite.getFromEmail ());
                            
                            return;
                            
                        }
                       
                        ed.setTheirPublicKey (invite.getFromPublicKey ());
                        
                        try
                        {
                        
                            EditorsEnvironment.updateEditor (ed);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to update editor: " +
                                                  ed,
                                                  e);

                            return;                                                                    
                            
                        }
                                                  
                        EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed);
                        
                        EditorMessage m = new InviteResponseMessage (true,
                                                                     EditorsEnvironment.getUserAccount ());
                        m.setEditor (ed);
                       
                        EditorsEnvironment.sendMessageToEditor (m,
                                                                null,
                                                                null,
                                                                null);
                                              
                   }
                   
                });
                */                                               
           }
           
        },
        // User wants to reject the invite.
        new ActionListener ()
        {
           
           public void actionPerformed (ActionEvent ev)
           {
               
                InviteResponseMessage rm = new InviteResponseMessage (false,
                                                                      EditorsEnvironment.getUserAccount ());
                rm.setEditor (ed);
                
                EditorsEnvironment.rejectInvite (ed,
                                                 rm,
                                                 null);
               
/*
                    // Update the invite in the web service to rejected.
                    EditorsEnvironment.updateInvite (invite.getFromEmail (),
                                                     Invite.Status.rejected,
                                                     new ActionListener ()
                                                     {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            EditorEditor ed = EditorsEnvironment.getEditorByEmail (invite.getFromEmail ());
                            
                                                            if (ed == null)
                                                            {
                                                                
                                                                Environment.logError ("How did we get here, no editor for: " +
                                                                                      invite.getFromEmail ());
                                                                
                                                                return;
                                                                
                                                            }

                                                            InviteResponseMessage res = new InviteResponseMessage (true,
                                                                                                                   null);
                                                            
                                                            EditorsEnvironment.sendMessageToEditor (res,
                                                                                                    null,
                                                                                                    null,
                                                                                                    null);

                                                        }
                                                        
                                                     });
*/
           }
           
        });
  
    }
    
    public void checkPendingInvites ()
    {
        
        final EditorsWebServiceHandler _this = this;
        
        // Check for pending invites.
        this.getPendingInvites (null,
                                new EditorsWebServiceAction ()
        {
                                
            public void processResult (EditorsWebServiceResult res)
            {

                List invites = (List) res.getReturnObject ();

                _this.displayPendingInvites (invites);
                                            
            }            

        },
        null);                                    
    
    }
    
    private void displayPendingInvites (List invites)
    {
            
        if ((invites == null)
            ||
            (invites.size () == 0)
           )
        {
                                        
            return;
            
        }
    
        AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();

        for (int i = 0; i < invites.size (); i++)
        {
        
            Map m = (Map) invites.get (i);
        
            Invite invite = null;
            
            try
            {
                
                invite = Invite.createFrom (m);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to create invite from: " +
                                      m,
                                      e);
                
                continue;
                
            }
        
            final String fromEmail = invite.getFromEmail ();
        
            // Check to see if we have already rejected/accepted this invite.
            EditorEditor ed = EditorsEnvironment.getEditorByEmail (fromEmail);
            
            if ((ed != null)
                &&
                (ed.getEditorStatus () != EditorEditor.EditorStatus.pending)
               )
            {
                
                // Already have this editor (somewhere).
                Environment.logError ("State error: got pending invite from: " +
                                      fromEmail +
                                      " to: " +
                                      EditorsEnvironment.getUserAccount ().getEmail () +
                                      " but local data states editor has status: " +
                                      ed.getEditorStatus ().getType ());
                
                return;
                
            }
        
            if (ed != null)
            {
                
                Environment.logError ("State error: editor should not exist when an invite is pending: " +
                                      fromEmail);
                
                return;
                
            }
        
            // Add the editor.
            ed = new EditorEditor ();
            ed.setTheirPublicKey (invite.getFromPublicKey ());
            ed.setEmail (invite.getFromEmail ());
            ed.setMessagingUsername (invite.getToMessagingUsername ());
            ed.setServiceName (invite.getToServiceName ());
            
            try
            {
            
                EditorsEnvironment.addNewEditor (ed);
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to add new editor: " +
                                      invite.getFromEmail (),
                                      e);
                
                return;
                
            }
            
            if (!EditorsEnvironment.isShowPopupWhenNewMessageReceived ())
            {

                return;
            
            }            
        
            this.showInviteFromEditor (invite);

        }
        
    }
        
        /*
    public void initInvitesListener ()
    {
        
        final EditorsWebServiceHandler _this = this;
        
        this.invitesListener = new java.util.Timer (true);
        
        if (true)
        {
            
            return;
            
        }
        
        this.invitesListener.schedule (new TimerTask ()
        {
            
            public void run ()
            {
                
                // Only do the check if we have one or more editors with a pending state.
                if (EditorsEnvironment.getPendingEditorsCount () == 0)
                {
                    
                    return;
                    
                }
                
                _this.getMyInvites (new EditorsWebServiceAction ()
                {
                   
                    public void processResult (EditorsWebServiceResult res)
                    {
                        
                        List invites = (List) res.getReturnObject ();

                        final List<EditorEditor> rejections = new ArrayList ();   
                        final List<EditorEditor> acceptances = new ArrayList ();   
                    
                        for (int i = 0; i < invites.size (); i++)
                        {
                    
                            Map m = (Map) invites.get (0);
                        
                            Invite invite = null;
                            
                            try
                            {
                                
                                invite = Invite.createFrom (m);
                                
                            } catch (Exception e) {
                                
                                Environment.logError ("Unable to create invite from: " +
                                                      m,
                                                      e);
                                
                                continue;
                                
                            }

                            final String toEmail = invite.getToEmail ();
                            
                            // Check to see if we have already rejected/accepted this invite.
                            final EditorEditor ed = EditorsEnvironment.getEditorByEmail (toEmail);
                        
                            // Check the status, if accepted then we
                            // 1. send a subscribe to the editor.
                            // 2. send the editor the public key message.
                            // 3. set them as current if we have their public key.
                            if ((ed != null)
                                &&
                                (ed.getEditorStatus () == EditorEditor.EditorStatus.pending)
                               )
                            {
                                
                                if (invite.getStatus () == Invite.Status.accepted)
                                {
    
                                    EditorsEnvironment.getMessageHandler ().subscribeToEditor (ed);
                                
                                    acceptances.add (ed);
                                
                                    if (ed.getTheirPublicKey () != null)
                                    {
                                        
                                        // Set them as current.
                                        ed.setEditorStatus (EditorEditor.EditorStatus.current);
                                                                                                                                                                
                                    }
                                
                                }
                            
                                if (invite.getStatus () == Invite.Status.rejected)
                                {
    
                                    ed.setEditorStatus (EditorEditor.EditorStatus.rejected);
    
                                    rejections.add (ed);
    
                                }
                                
                                try
                                {
                                
                                    EditorsEnvironment.updateEditor (ed);
                                    
                                    // Show that they have accepted.
                                    //EditorsUIUtils.showAcceptance (ed);                                        
                                    
                                } catch (Exception e) {
                                    
                                    Environment.logError ("Unable to update editor: " +
                                                          ed,
                                                          e);
                                    
                                    ed.setEditorStatus (EditorEditor.EditorStatus.pending);
                                                                                
                                }

                            }
                            
                        }

                        AbstractProjectViewer viewer = Environment.getFocusedProjectViewer ();
                        
                        EditorsUIUtils.showInviteAcceptances (viewer,
                                                              acceptances,
                                                              null);

                        EditorsUIUtils.showInviteRejections (viewer,
                                                             rejections,
                                                             null);
                        
                    }
                    
                },
                null);

            }
            
        },
        5000,
        10 * 60 * 1000);
        
        this.invitesListener.schedule (new TimerTask ()
        {
            
            public void run ()
            {
                                
                // Get the currently focused project viewer.                
                _this.getPendingInvites ("To check for pending invites you must first login.",
                                         new EditorsWebServiceAction ()
                {
                                        
                    public void processResult (EditorsWebServiceResult res)
                    {
        
                        List invites = (List) res.getReturnObject ();

                        _this.displayPendingInvites (invites);
                                                    
                    }            
        
                },
                null);
                
            }
            
        },
        10000,
        // Every 10 minutes.
        10 * 60 * 1000);
        
    }
    */
    private void callService (final Service                 service,
                              final String                  id,
                              final Object                  data,
                              final String                  method,
                              final EditorsWebServiceAction onComplete,
                              final EditorsWebServiceAction onError)
    {
        
        this.callService (service,
                          id,
                          data,
                          method,
                          null,
                          onComplete,
                          onError);
        
                              
    }

    public void logout ()
    {
        
        EditorsEnvironment.getUserAccount ().setWebServiceSessionId (null);
        
    }
    
    public void login (final ActionListener onLogin,
                       final ActionListener onError)
    {
        
        final EditorAccount acc = EditorsEnvironment.getUserAccount ();

        if (acc.getWebServiceSessionId () != null)
        {
            
            if (onLogin != null)
            {
                
                onLogin.actionPerformed (new ActionEvent ("login", 1, "login"));
                
            }
            
            return;
            
        }
        
        Map data = new HashMap ();
        data.put ("email",
                  acc.getEmail ());
        data.put ("password",
                  acc.getPassword ());
        
        final EditorsWebServiceHandler _this = this;
        
        this.callService (Service.sessions,
                          null,
                          data,
                          "POST",
                          null,
                          new EditorsWebServiceAction ()
                          {
                            
                              public void processResult (EditorsWebServiceResult res)
                              {
                                                                    
                                    String sessionId = res.getReturnObjectAsString ();
                                    
                                    acc.setWebServiceSessionId (sessionId);
                                    
                                    if (sessionId == null)
                                    {
                                        
                                        // Oops?
                                        Environment.logError ("No session id returned from sessions service");
                                        
                                        EditorsUIUtils.showLoginError ("Sorry an unexpected error has occurred with<br />the Editors service, please try again later.");
                                        
                                        return;
                                        
                                    }
                                                                        
                                    if (onLogin != null)
                                    {
                                        
                                        onLogin.actionPerformed (new ActionEvent ("login", 1, "login"));
                                        
                                    }
                                
                              }
                            
                          },
                          new EditorsWebServiceAction ()
                          {
                            
                              public void processResult (EditorsWebServiceResult res)
                              {
                                
                                  Environment.logError ("Unable to login to editors service (qw website): " + res);
                                
                                  if (onError != null)
                                  {                                                                
                                  
                                     UIUtils.doLater (onError);
                                     
                                  } else {
                                  
                                    EditorsUIUtils.showLoginError (res);
                                    
                                  }
                                
                              }

                          });
        
    }
    
    private void callService (final Service                 service,
                              final String                  id,
                              final Object                  data,
                              final String                  method,
                              final Map<String, String>     headers,
                              final EditorsWebServiceAction onComplete,
                              final EditorsWebServiceAction onError)
    {
        
        final EditorAccount acc = EditorsEnvironment.getUserAccount ();
                        
        new EditorsWebServiceCall ()
            .service (service)
            .id (id)
            .data (data)
            .method (method)
            .headers (headers)
            .onComplete (onComplete)
            .onError ((onError != null ? onError : this.defaultCallErrorAction))
            .sessionId ((acc != null ? acc.getWebServiceSessionId () : null))
            .call ();
                                        
    }

    private void isModified (final Service                 service,
                             final String                  id,
                             final long                    lastModified,
                             final EditorsWebServiceAction onComplete,
                             final EditorsWebServiceAction onError)
                     throws  Exception
    {
    
        Map<String, String> headers = new HashMap ();
        
        if (lastModified > -1)
        {
            
            SimpleDateFormat format = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss zzz");

            String d = format.format (lastModified);
            
            Environment.logDebugMessage ("Performing is modified check with date: " + lastModified);
            
            headers.put ("If-modified-since",
                         d);

        }
        
        this.callService (service,
                          id,
                          null,
                          "HEAD",
                          headers,
                          onComplete,
                          onError);
        
    }
/*
    public void saveProject (final String                        name,
                             final String                        desc,
                             final Set<String>                   genres,
                             final String                        expectations,
                             final EditorProject.WordCountLength wordCountLength,
                             final EditorsWebServiceAction       action)
                      throws Exception
    {

        EditorProject ep = null;//viewer.getProject ().getEditorProject ();
        
        if (ep == null)
        {
            
            this.createProject (name,
                                desc,
                                genres,
                                expectations,
                                wordCountLength,
                                action);
            
        } else {
                        
            this.updateProject (name,
                                desc,
                                genres,
                                expectations,
                                wordCountLength,
                                action);
            
        }
                
    }
*/
    private Map getProjectData (String                        name,
                                String                        desc,
                                Set<String>                   genres,
                                String                        expectations,
                                EditorProject.WordCountLength wordCountLength)
    {

        Map data = new HashMap ();
        data.put (FieldNames.name,
                  name);
        data.put (FieldNames.description,
                  desc);
        data.put (FieldNames.genres,
                  genres);
        data.put (FieldNames.expectations,
                  expectations);
        data.put (FieldNames.wordCountLength,
                  wordCountLength.getType ());

        return data;
        
    }
    /*
    public void updateProject (final AbstractProjectViewer         viewer,
                               final String                        name,
                               final String                        desc,
                               final Set<String>                   genres,
                               final String                        expectations,
                               final EditorProject.WordCountLength wordCountLength,
                               final EditorsWebServiceAction       action)
                        throws Exception
    {

        final EditorsWebServiceHandler _this = this;
    
        final EditorProject ep = viewer.getProject ().getEditorProject ();

        if (ep == null)
        {
            
            throw new IllegalStateException ("Unable to update project, no project available.");
            
        }

        if (ep.getId () == null)
        {
            
            throw new IllegalStateException ("Unable to update project, project has no id.");
            
        }
        
        final Map data = this.getProjectData (name,
                                              desc,
                                              genres,
                                              expectations,
                                              wordCountLength);

        this.doLogin (viewer,
                      "To update your {project} you must first login to the Editors service.",
                      new ActionListener ()
                      {
                        
                            public void actionPerformed (ActionEvent ev)
                            {
                                
                                if (EditorsEnvironment.getUserAccount ().getAuthor () == null)
                                {
                                    
                                    throw new IllegalStateException ("Unable to update project, no author available.");
                                    
                                }                                
                                
                                _this.callService (viewer,
                                                   Service.projects,
                                                   ep.getId (),
                                                   data,
                                                   "PUT",
                                                   new EditorsWebServiceAction ()
                                                   {
                            
                                                        public void onComplete (EditorsWebServiceResult res)
                                                        {
                                    
                                                            _this.fillProject (ep,
                                                                               name,
                                                                               desc,
                                                                               genres,
                                                                               expectations,
                                                                               wordCountLength);
                                                            
                                                            try
                                                            {
                                                            
                                                                _this.saveProjectToLocal (ep,
                                                                                          viewer);

                                                            } catch (Exception e) {
                                                                
                                                                Environment.logError ("Cant save project to local",
                                                                                      e);
                                                                
                                                            }
                                                            
                                                        }
                                                        
                                                        public void onError (EditorsWebServiceResult res)
                                                        {}
                                                        
                                                   });
                                
                            }
                            
                          },
                          null);
                                        
    }
    */
    private void fillProject (EditorProject                 ep,
                              String                        name,
                              String                        desc,
                              Set<String>                   genres,
                              String                        expectations,
                              EditorProject.WordCountLength wordCountLength)
    {

        ep.setName (name);
        ep.setDescription (desc);
        ep.setExpectations (expectations);
        ep.setGenres (genres);
        ep.setWordCountLength (wordCountLength);
        
    }
    
    private void saveProjectToLocal (EditorProject         ep,
                                     AbstractProjectViewer pv)
                                     throws                Exception
    {
        
        pv.saveObject (ep,
                       true);

        //pv.getProject ().setEditorProject (ep);
        
    }
    
    public void deleteProject (EditorProject proj)
    {
        
    }
    
    private void createProject (final String                        name,
                                final String                        desc,
                                final Set<String>                   genres,
                                final String                        expectations,
                                final EditorProject.WordCountLength wordCountLength,
                                final ActionListener                onComplete)
                         throws Exception
    {

        final EditorsWebServiceHandler _this = this;
        final EditorProject ep = null; //viewer.getProject ().getEditorProject ();

        if (ep != null)
        {
            
            throw new IllegalStateException ("Already have a project.");
            
        }
        
        final Map data = this.getProjectData (name,
                                              desc,
                                              genres,
                                              expectations,
                                              wordCountLength);
        
        this.doLogin ("Before creating a {project} you must login to the Editors service.",
                      new ActionListener ()
                      {
                        
                            public void actionPerformed (ActionEvent ev)
                            {
                                
                                if (EditorsEnvironment.getUserAccount ().getAuthor () == null)
                                {
                                    
                                    throw new IllegalStateException ("Unable to create project, no author available.");
                                    
                                }                                
                                
                                _this.callService (Service.projects,
                                                   null,
                                                   data,
                                                   "POST",
                                                   new EditorsWebServiceAction ()
                                                   {
                            
                                                        public void processResult (EditorsWebServiceResult res)
                                                        {

                                                            ep.setId (res.getReturnObjectAsString ());
                                                                                                                                                            
                                                            _this.fillProject (ep,
                                                                               name,
                                                                               desc,
                                                                               genres,
                                                                               expectations,
                                                                               wordCountLength);
                                                            
                                                            try
                                                            {
/*
                                                                _this.saveProjectToLocal (ep,
                                                                                          viewer);
*/
                                                            } catch (Exception e) {
                                                                
                                                                Environment.logError ("Unable to save project to local",
                                                                                      e);
                                                                
                                                            }
                                                            
                                                            //viewer.getProject ().setEditorProject (ep);
                                                            
                                                            if (onComplete != null)
                                                            {
                                                                
                                                                onComplete.actionPerformed (new ActionEvent ("done", 1, "done"));
                                                                
                                                            }
                                                            
                                                        }
                                                        
                                                   },
                                                   new EditorsWebServiceAction ()
                                                   {
                                                                                                            
                                                        public void processResult (EditorsWebServiceResult res)
                                                        {}
                                                        
                                                   });
                                
                            }
                            
                          },
                          null);
                
    }
    
    public List<EditorEditor> findEditors ()
                                           throws Exception
    {
        
        Map parms = new HashMap ();
        List<EditorEditor> ret = new ArrayList ();

        /*
        Object d = this.doGet (Service.editors,
                               null,
                               parms);        
        
        
        if (d instanceof List)
        {
            
            List l = (List) d;
            
            for (int i = 0; i < l.size (); i++)
            {
                
                Map m = (Map) l.get (i);

                ret.add (this.createEditorFromData (m));
                
            }
            
        }
*/
        return ret;

    }
    
    public List<EditorProject> findProjects (String title)
                                             throws Exception
    {
        
        Map parms = new HashMap ();
        parms.put ("title",
                   title);
        List<EditorProject> ret = new ArrayList ();
/*        
        Object d = this.doGet (Service.projects,
                               null,
                               parms);        
                
        if (d instanceof List)
        {
            
            List l = (List) d;
            
            for (int i = 0; i < l.size (); i++)
            {
                
                Map m = (Map) l.get (i);
                
                ret.add (this.createProjectFromData (m));
                
            }
            
        }
*/
        return ret;
        
    }

    private EditorProject createProjectFromData (Map data)
    {
        
        EditorProject ep = new EditorProject ();
        
        ep.setId ((String) data.get (FieldNames.id));
        ep.setName ((String) data.get (FieldNames.name));
        ep.setDescription ((String) data.get (FieldNames.description));
        ep.setExpectations ((String) data.get (FieldNames.expectations));
        ep.setLastModified (new Date ((Long) data.get (FieldNames.lastModified)));

        return ep;        
        
    }
    
    /*
    private EditorEditor createEditorFromData (Map data)
    {
        
        EditorEditor e = new EditorEditor ();
        
        e.setId ((String) data.get (FieldNames.id));
        e.setName ((String) data.get (FieldNames.name));
        e.setAbout ((String) data.get (FieldNames.about));
        e.setLastModified (new Date ((Long) data.get (FieldNames.lastModified)));

        String avIm = (String) data.get (FieldNames.avatarImage);
        
        if (avIm != null)
        {
            
            e.setAvatar (new ByteArrayDataSource (Base64.decode (avIm)));
            e.setAvatarImageFileType ((String) data.get (FieldNames.avatarImageFileType));
            
        } else {
            
            e.setAvatar (new javax.activation.URLDataSource (Environment.getIconURL (Constants.NO_AVATAR_IMAGE_FILE_NAME,
                                                                                     -1)));
            
        }
        
        return e;        
        
    }
*/
    public void deleteEditor ()
    {}
    
    public void updateEditor (String                             name,
                              String                             about,
                              File                               avatarImage,
                              Set<String>                        genres,
                              Set<EditorProject.WordCountLength> wordCountLengths)
                              throws                             Exception
    {
        
        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to update editor, no account available.");
            
        }
        
        EditorEditor ed = EditorsEnvironment.getUserAccount ().getEditor ();
        
        if (ed == null)
        {
            
            throw new IllegalStateException ("Unable to update editor, no editor found.");
            
        }

        if (ed.getId () == null)
        {
            
            throw new IllegalStateException ("Have an editor but it doesn't have an id.");
            
        }
        /*
        Map data = this.getEditorData (name,
                                       about,
                                       avatarImage,
                                       genres,
                                       wordCountLengths);
                
        Object d = this.doPut (Service.editors,
                               ed.getId (),
                               data);        
        
        ed.setName (name);
        ed.setAbout (about);
        ed.setGenres (genres);
        ed.setWordCountLengths (wordCountLengths);

        // Save the editor away.
        this.saveEditorToLocal (avatarImage);                
        */
    }
        
    public void createEditor (String                             name,
                              String                             about,
                              File                               avatarImage,
                              Set<String>                        genres,
                              Set<EditorProject.WordCountLength> wordCountLengths)
                              throws                             Exception
    {

        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to create editor, no account available.");
            
        }
        
        if (EditorsEnvironment.getUserAccount ().getEditor () != null)
        {
            
            throw new IllegalStateException ("Unable to create editor, already have an editor.");
            
        }
        /*
        Map data = this.getEditorData (name,
                                       about,
                                       avatarImage,
                                       genres,
                                       wordCountLengths);
        
        Object d = this.doPost (Service.editors,
                                null,
                                data);        

        EditorEditor ed = new EditorEditor ();
        ed.setName (name);
        ed.setAbout (about);                                
        ed.setId (d.toString ());
        ed.setGenres (genres);
        ed.setWordCountLengths (wordCountLengths);
        
        this.acc.setEditor (ed);
        
        // Save the author away.
        this.saveEditorToLocal (avatarImage);
        */
    }
/*
    private void saveEditorToLocal (File   avatarImage)
                                    throws Exception
    {
                
        if (EditorsEnvironment.getUserAccount () == null)
        {

            return;
            
        }

        EditorEditor ed = EditorsEnvironment.getUserAccount ().getEditor ();
        
        if (ed == null)
        {
            
            return;
            
        }
        
        // Save the author away.
        File f = EditorsEnvironment.getEditorsEditorFile ();

        Element root = ed.getAsJDOMElement ();

        JDOMUtils.writeElementToFile (root,
                                      f,
                                      true);
        
        Environment.setUserProperty (Constants.QW_EDITORS_EDITOR_LAST_MODIFIED_PROPERTY_NAME,
                                     "" + ed.getLastModified ().getTime ());        
                                     
        // Save the avatar image file.
        if ((avatarImage != null)
            &&
            ((EditorsEnvironment.getEditorsEditorAvatarImageFile () == null)
             ||
             (avatarImage.getPath () != EditorsEnvironment.getEditorsEditorAvatarImageFile ().getPath ())
            )
           )
        {
            
            String ft = Utils.getFileType (avatarImage);
            
            if (ft != null)
            {
                
                // Save the file to the right location.
                IOUtils.copyFile (avatarImage,
                                  EditorsEnvironment.getEditorsEditorAvatarImageFile (ft),
                                  8192);            
    
                Environment.setUserProperty (Constants.EDITORS_EDITOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME,
                                             ft);
                                             
            }
            
        }

        ed.setAvatar (new FileDataSource (EditorsEnvironment.getEditorsEditorAvatarImageFile ()));                                     
        
    }
*/
    private Map getEditorData (String                             name,
                               String                             about,
                               File                               avatarImage,
                               Set<String>                        genres,
                               Set<EditorProject.WordCountLength> wordCountLengths)
                               throws                             Exception
    {
        
        Map data = new HashMap ();
        data.put (FieldNames.name,
                  name);
        data.put (FieldNames.about,
                  about);
        data.put (FieldNames.genres,
                  genres);
        data.put (FieldNames.wordCountLengths,
                  EditorProject.WordCountLength.getTypes (wordCountLengths));
        
        if ((avatarImage != null)
            &&
            (avatarImage.exists ())
           )
        {

            BufferedImage im = UIUtils.getScaledImage (avatarImage,
                                                       300);

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();
            
            ImageIO.write (im,
                           Utils.getFileType (avatarImage),
                           bout);
            
            String s = Base64.encodeBytes (bout.toByteArray ());
            
            if (s.length () > MAX_IMAGE_SIZE)
            {
                
                throw new GeneralException ("Unable to convert image: " + avatarImage.getPath () + " to a 300x300 image that is " + MAX_IMAGE_SIZE + " or lower in length.");
                
            }
            
            data.put (FieldNames.avatarImage,
                      s);
            
            data.put (FieldNames.avatarImageFileType,
                      Utils.getFileType (avatarImage));
        }

        return data;
        
    }
    
    public void saveEditor (String                             name,
                            String                             about,
                            File                               avatarImage,
                            Set<String>                        genres,
                            Set<EditorProject.WordCountLength> wordCountLengths)
                            throws                             Exception
    {

        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }

        EditorEditor ed = EditorsEnvironment.getUserAccount ().getEditor ();
        
        if (ed != null)
        {
            
            // Update.
            this.updateEditor (name,
                               about,
                               avatarImage,
                               genres,
                               wordCountLengths);
            
        } else {
            
            this.createEditor (name,
                               about,
                               avatarImage,
                               genres,
                               wordCountLengths);
            
        }
        
    }
    
    public void saveAuthor (String name,
                            String about,
                            File   avatarImage)
                            throws Exception
    {
        
        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }

        EditorAuthor a = EditorsEnvironment.getUserAccount ().getAuthor ();
        
        if (a != null)
        {
            
            // Update.
            this.updateAuthor (name,
                               about,
                               avatarImage);
            
        } else {
            
            this.createAuthor (name,
                               about,
                               avatarImage);
            
        }
        
    }
    
    private Map getAuthorData (String name,
                               String about,
                               File   avatarImage)
                               throws Exception
    {
        
        Map data = new HashMap ();
        data.put (FieldNames.name,
                  name);
        data.put (FieldNames.about,
                  about);
        
        if (avatarImage != null)
        {

            BufferedImage im = UIUtils.getScaledImage (avatarImage,
                                                       300);

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();
            
            ImageIO.write (im,
                           Utils.getFileType (avatarImage),
                           bout);
            
            String s = Base64.encodeBytes (bout.toByteArray ());
            
            if (s.length () > MAX_IMAGE_SIZE)
            {
                
                throw new GeneralException ("Unable to convert image: " + avatarImage.getPath () + " to a 300x300 image that is " + MAX_IMAGE_SIZE + " or lower in length.");
                
            }
            
            data.put (FieldNames.avatarImage,
                      s);
            
            data.put (FieldNames.avatarImageFileType,
                      Utils.getFileType (avatarImage));
        }

        return data;
        
    }
    
    private void createAuthor (String name,
                               String about,
                               File   avatarImage)
                               throws Exception
    {
        
        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }
        
        if (EditorsEnvironment.getUserAccount ().getAuthor () != null)
        {
            
            throw new IllegalStateException ("Unable to create author, already have an author.");
            
        }
        /*
        Map data = this.getAuthorData (name,
                                       about,
                                       avatarImage);
        
        Object d = this.doPost (Service.authors,
                                null,
                                data);        

        EditorAuthor au = new EditorAuthor ();
        au.setName (name);
        au.setAbout (about);                                
        au.setId (d.toString ());

        this.acc.setAuthor (au);
        
        // Save the author away.
        this.saveAuthorToLocal (avatarImage);
          */      
    }

    private void updateAuthor (String name,
                               String about,
                               File   avatarImage)
                               throws Exception
    {
        
        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }
        /*
        EditorAuthor au = this.acc.getAuthor ();
        
        if (au == null)
        {
            
            throw new IllegalStateException ("Unable to update author, no author found.");
            
        }

        if (au.getId () == null)
        {
            
            throw new IllegalStateException ("Have an author but it doesn't have an id.");
            
        }
        
        Map data = this.getAuthorData (name,
                                       about,
                                       avatarImage);
                
        Object d = this.doPut (Service.authors,
                               au.getId (),
                               data);        
        
        au.setName (name);
        au.setAbout (about);
        
        // Save the author away.
        this.saveAuthorToLocal (avatarImage);        
        */
    }
    /*
    private void saveAuthorToLocal (File  avatarImage)
                             throws       Exception
    {
                
        if (EditorsEnvironment.getUserAccount () == null)
        {

            return;
            
        }

        EditorAuthor au = EditorsEnvironment.getUserAccount ().getAuthor ();
        
        if (au == null)
        {
            
            return;
            
        }
        
        // Save the author away.
        File f = EditorsEnvironment.getEditorsAuthorFile ();

        Element root = au.getAsJDOMElement ();

        JDOMUtils.writeElementToFile (root,
                                      f,
                                      true);
        
        Environment.setUserProperty (Constants.QW_EDITORS_AUTHOR_LAST_MODIFIED_PROPERTY_NAME,
                                     "" + au.getLastModified ().getTime ());
        
        // Save the avatar image file.
        if ((avatarImage != null)
            &&
            ((EditorsEnvironment.getEditorsAuthorAvatarImageFile () == null)
             ||
             (avatarImage.getPath () != EditorsEnvironment.getEditorsAuthorAvatarImageFile ().getPath ())
            )
           )
        {
            
            String ft = Utils.getFileType (avatarImage);
            
            if (ft != null)
            {
                
                // Save the file to the right location.
                IOUtils.copyFile (avatarImage,
                                  EditorsEnvironment.getEditorsAuthorAvatarImageFile (ft),
                                  8192);            
    
                Environment.setUserProperty (Constants.EDITORS_AUTHOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME,
                                             ft);
                                             
            }
            
        }
        
        au.setAvatar (new FileDataSource (EditorsEnvironment.getEditorsAuthorAvatarImageFile ())); 

    }
    */
    private void loadAuthor ()
                             throws Exception
    {

        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to load author, no account available.");
            
        }
        
        if (EditorsEnvironment.getUserAccount ().getAuthor () != null)
        {
            
            throw new IllegalStateException ("Unable to load author, already have an author.");
            
        }

        // Hide for now.
        if (true)
        {
            
            return;
            
        }
        /*
        // Load the saved author info, if available.
        File f = Environment.getEditorsAuthorFile ();
        
        if (f.exists ())
        {
            
            try
            {
            
                Element root = JDOMUtils.getFileAsElement (f,
                                                           "gz");
                
                EditorAuthor au = new EditorAuthor (root);
                au.setAvatar (new FileDataSource (Environment.getEditorsAuthorAvatarImageFile ()));
                this.acc.setAuthor (au);

            } catch (Exception e) {
                
                Environment.logError ("Unable to load author file from: " +
                                      f,
                                      e);
                
            }
            
        }        
        
        Map data = null;
        
        String v = Environment.getProperty (Constants.QW_EDITORS_AUTHOR_LAST_MODIFIED_PROPERTY_NAME);
        
        if (v != null)
        {
                        
            if (this.acc.getAuthor () == null)
            {
                
                // Possibly the author file has been removed.
                // Get the author information again.
                this.loadAuthorFromServer ();
                                        
            } else {
            
                if (this.isModified (Service.authors,
                                     this.acc.getAuthor ().getId (),
                                     Long.parseLong (v)))
                {
                    
                    this.loadAuthorFromServer ();
                                    
                }

            }
            
        }
                                
        this.login ();
        */
    }
    
    private void loadAuthorFromServer ()
                                       throws Exception
    {

        String authorId = null;
    
        if ((EditorsEnvironment.getUserAccount () != null)
            &&
            (EditorsEnvironment.getUserAccount ().getAuthor () != null)
           )
        {
            
            authorId = EditorsEnvironment.getUserAccount ().getAuthor ().getId ();
            
        }
/*
        authorId = null;
        Object d = this.doGet (Service.authors,
                               authorId,
                               null);        

        Map objD = null;
                               
        if (authorId == null)
        {
            
            // Might have got back our author in a list.
            if (d instanceof List)
            {
                
                List l = (List) d;
                
                if (l.size () > 0)
                {
                    
                    objD = (Map) l.get (0);
                    
                }
                
            }
            
        } else {
            
            objD = (Map) d;
            
        }
        
        if (objD != null)
        {                                  
        
            // We have the author information.
            EditorAuthor au = new EditorAuthor ();        
                                    
            Number lastModN = (Number) objD.get (FieldNames.lastModified);
            
            au.setId ((String) objD.get (FieldNames.id));
            au.setName ((String) objD.get (FieldNames.name));
            au.setAbout ((String) objD.get (FieldNames.about));
            au.setLastModified (new Date (lastModN.longValue ()));
            
            this.acc.setAuthor (au);
            
            // Save the file away.
            String av = (String) objD.get (FieldNames.avatarImage);
            
            if (av != null)
            {
                
                File f = Environment.getEditorsAuthorAvatarImageFile ((String) objD.get (FieldNames.avatarImageFileType));
                
                // Base64 decode, then write bytes to file.
                byte[] fbytes = Base64.decode (av);
                
                f = new File (f.getPath ());
                
                Utils.writeBytesToFile (f,
                                        fbytes);
                
            }
                                        
            this.saveAuthorToLocal (null);
        
        } else {
            
            // Just assume user has cleaned up or something has gone setting the property.
            Environment.setUserProperty (Constants.QW_EDITORS_AUTHOR_LAST_MODIFIED_PROPERTY_NAME,
                                         Long.MAX_VALUE + "");
            
        }
  */      
    }
        
    private void loadEditor ()
                             throws Exception
    {

        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            throw new IllegalStateException ("Unable to load editor, no account available.");
            
        }
        
        if (EditorsEnvironment.getUserAccount ().getEditor () != null)
        {
            
            throw new IllegalStateException ("Unable to load editor, already have an editor.");
            
        }
/*
        // Load the saved editor info, if available.
        File f = Environment.getEditorsEditorFile ();
        
        if (f.exists ())
        {
            
            Element root = JDOMUtils.getFileAsElement (f,
                                                       "gz");
            
            EditorEditor ed = new EditorEditor (root);
            
            ed.setAvatar (new FileDataSource (Environment.getEditorsEditorAvatarImageFile ()));
            
            this.acc.setEditor (ed);
            
        }        
        
        Map data = null;
        
        String v = Environment.getProperty (Constants.QW_EDITORS_EDITOR_LAST_MODIFIED_PROPERTY_NAME);
        
        if (v != null)
        {
                        
            if (this.acc.getEditor () == null)
            {
                
                // Possibly the author file has been removed.
                // Get the author information again.
                this.loadEditorFromServer ();
                                        
            } else {
            
                if (this.isModified (Service.editors,
                                     this.acc.getEditor ().getId (),
                                     Long.parseLong (v)))
                {
                    
                    this.loadEditorFromServer ();
                                    
                }

            }
            
        }
  */                                      
    }
    
    private void loadEditorFromServer ()
                                       throws Exception
    {
/*
        String editorId = null;
    
        if ((this.acc != null)
            &&
            (this.acc.getEditor () != null)
           )
        {
            
            editorId = this.acc.getEditor ().getId ();
            
        }
        
        editorId = null;
        Object d = this.doGet (Service.editors,
                               editorId,
                               null);        

        Map objD = null;
                               
        if (editorId == null)
        {
            
            // Might have got back our author in a list.
            if (d instanceof List)
            {
                
                List l = (List) d;
                
                if (l.size () > 0)
                {
                    
                    objD = (Map) l.get (0);
                    
                }
                
            }
            
        } else {
            
            objD = (Map) d;
            
        }
        
        if (objD != null)
        {                                  
        
            // We have the author information.
            EditorEditor ed = new EditorEditor ();        
                        
            Number lastModN = (Number) objD.get (FieldNames.lastModified);
            
            ed.setId ((String) objD.get (FieldNames.id));
            ed.setName ((String) objD.get (FieldNames.name));
            ed.setAbout ((String) objD.get (FieldNames.about));
            ed.setLastModified (new Date (lastModN.longValue ()));
            
            this.acc.setEditor (ed);
            
            // Get the genres
            Object g = objD.get (FieldNames.genres);
            
            if (g != null)
            {
                
                ed.setGenres (new LinkedHashSet ((Collection<String>) g));

            }
            
            Object wcl = objD.get (FieldNames.wordCountLengths);
            
            if (wcl != null)
            {
                
                // Get the word count lengths
                ed.setWordCountLengths (EditorProject.WordCountLength.convert ((new LinkedHashSet ((Collection<String>) wcl))));
            
            }
            
            // Save the file away.
            String av = (String) objD.get (FieldNames.avatarImage);
            
            if (av != null)
            {
                
                File f = Environment.getEditorsEditorAvatarImageFile ((String) objD.get (FieldNames.avatarImageFileType));
                
                // Base64 decode, then write bytes to file.
                byte[] fbytes = Base64.decode (av);
                
                f = new File (f.getPath ());
                
                Utils.writeBytesToFile (f,
                                        fbytes);
                                
            }
                                        
            this.saveEditorToLocal (null);
        
        } else {
            
            // Just assume user has cleaned up or something has gone setting the property.
            Environment.setUserProperty (Constants.QW_EDITORS_EDITOR_LAST_MODIFIED_PROPERTY_NAME,
                                         Long.MAX_VALUE + "");
            
        }
  */      
    }

    public void deleteAccount ()
    {
        
        // Delete from server, then inform editors.
        
        // Need to deal with our project...
        
    }
    /*
    public void saveAccount (String email,
                             String password)
                             throws Exception
    {
        
        if (this.acc == null)
        {

            this.createAccount (email,
                                password);
        
        } else {

            this.updateAccount (email,
                                password);
            
        }
        
    }
    */

    private boolean shouldShowLogin ()
    {
        
        return (EditorsEnvironment.getUserAccount () == null)
                ||
               (EditorsEnvironment.getUserAccount ().getPassword () == null);
                
    }

    public void changePassword (final String         newPassword,
                                final EditorsWebServiceAction onComplete,
                                final EditorsWebServiceAction onError)
    {

        final EditorsWebServiceHandler _this = this;
        
        this.doLogin ("To update your password you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                        
                          @Override
                          public void actionPerformed (ActionEvent ev)
                          {
                                                                    
                                Map data = new HashMap ();
                                data.put (FieldNames.password,
                                          newPassword);
                                
                                _this.callService (Service.accounts,
                                                   null,
                                                   data,                                                   
                                                   "PUT",
                                                   onComplete,
                                                   onError);
                                    
                          }
                                
                      },
                      null);
        
    }
    
    private void doLogin (final String                loginReason,
                          final ActionListener        onLogin,
                          final ActionListener        onCancel)
    {
                        
        final EditorsWebServiceHandler _this = this;
                        
        if (!this.shouldShowLogin ())
        {

            _this.login (onLogin,
                         null);
            
            return;
        
        }

        EditorsEnvironment.goOnline (loginReason,
                                     onLogin,
                                     onCancel,
                                     null);
                
    }

    public void getMyInvites (final EditorsWebServiceAction onComplete,
                              final EditorsWebServiceAction onError)
    {

        final EditorsWebServiceHandler _this = this;
        
        this.doLogin ("To get your invites you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                        
                          public void actionPerformed (ActionEvent ev)
                          {
                                                                    
                                Map data = null;
                                
                                _this.callService (Service.invites,
                                                   null,
                                                   data,                                                   
                                                   "GET",
                                                   onComplete,
                                                   onError);
                                    
                          }
                                
                      },
                      null);
    
    }
    
    public void getInvite (final EditorEditor            editor,
                           final EditorsWebServiceAction onComplete,
                           final EditorsWebServiceAction onError)
    {

        final EditorsWebServiceHandler _this = this;
        
        this.doLogin ("To get the invite for <b>" + editor.getEmail () + "</b> you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                        
                          public void actionPerformed (ActionEvent ev)
                          {
                                                                    
                                Map data = null;
                                
                                if (!editor.isInvitedByMe ())
                                {
                                    
                                    data = new HashMap ();
                                    data.put (FieldNames.received,
                                              true);

                                }
                                    
                                _this.callService (Service.invites,
                                                   editor.getEmail (),
                                                   data,                                                   
                                                   "GET",
                                                   onComplete,
                                                   onError);
                                    
                          }
                                
                      },
                      null);
    
    }
    
    /** 
     * From can be either a messaging username or an email address.
     */
    public void getInvite (final String                  from,
                           final EditorsWebServiceAction onComplete,
                           final EditorsWebServiceAction onError)
    {

        final EditorsWebServiceHandler _this = this;
        
        this.doLogin ("To get the invite for <b>" + from + "</b> you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                        
                          public void actionPerformed (ActionEvent ev)
                          {
                                                                    
                                Map data = null;
                                
                                data = new HashMap ();
                                data.put (FieldNames.received,
                                          true);

                                _this.callService (Service.invites,
                                                   from,
                                                   data,                                                   
                                                   "GET",
                                                   onComplete,
                                                   onError);
                                    
                          }
                                
                      },
                      null);
    
    }

    public void getRejectedInvites (final String                  loginReason,
                                    final EditorsWebServiceAction onComplete,
                                    final EditorsWebServiceAction onError)
    {
        
        final EditorsWebServiceHandler _this = this;

        this.doLogin ((loginReason != null ? loginReason : "To check your rejected invites you must first login to the Editors service."),
                      new ActionListener ()
                      {         
                        
                          public void actionPerformed (ActionEvent ev)
                          {
                                    
                                Map data = new HashMap ();
                                data.put (FieldNames.status,
                                          "rejected");
                                    
                                _this.callService (Service.invites,
                                                   null,
                                                   data,                                                   
                                                   "GET",
                                                   onComplete,
                                                   onError);
                                    
                          }
                                
                      },
                      null);
        
    }

    public void getPendingInvites (final String                  loginReason,
                                   final EditorsWebServiceAction onComplete,
                                   final EditorsWebServiceAction onError)
    {
        
        final EditorsWebServiceHandler _this = this;

        this.doLogin ((loginReason != null ? loginReason : "To check your current invites you must first login to the Editors service."),
                      new ActionListener ()
                      {         
                        
                          public void actionPerformed (ActionEvent ev)
                          {
                                    
                                Map data = new HashMap ();
                                data.put (FieldNames.status,
                                          "pending");
                                data.put (FieldNames.beforeAccountCreation,
                                          true);
                                data.put (FieldNames.received,
                                          true);
                                                                                                         
                                _this.callService (Service.invites,
                                                   null,
                                                   data,                                                   
                                                   "GET",
                                                   onComplete,
                                                   onError);
                                    
                          }
                                
                      },
                      null);
        
    }

    public void deleteInvite (final EditorEditor            ed,
                              final EditorsWebServiceAction onComplete,
                              final EditorsWebServiceAction onError)
    {
        
        // Can only do this when logged in.
        final EditorsWebServiceHandler _this = this;

        this.doLogin ("To delete an invite you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                                      
                        public void actionPerformed (ActionEvent ev)
                        {
                                                                    
                              _this.callService (Service.invites,
                                                 ed.getEmail (),
                                                 null,                                                   
                                                 "DELETE",
                                                 onComplete,
                                                 onError);
                                  
                        }
                              
                     },
                     null);        
        
    }
    
    public void updateInvite (final String                  toEmail,
                              final Invite.Status           newStatus,
                              final EditorsWebServiceAction onComplete,
                              final EditorsWebServiceAction onError)
    {
        
        // Can only do this when logged in.
        final EditorsWebServiceHandler _this = this;

        this.doLogin ("To update an invite you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                                      
                        public void actionPerformed (ActionEvent ev)
                        {
                                  
                              Map data = new HashMap ();
                              data.put (FieldNames.status,
                                        newStatus.getType ());
                                  
                              _this.callService (Service.invites,
                                                 toEmail,
                                                 data,                                                   
                                                 "PUT",
                                                 onComplete,
                                                 onError);
                                  
                        }
                              
                     },
                     null);        
        
    }
    
    public void sendInvite (final String                  toEmail,
                            final EditorsWebServiceAction onComplete,
                            final EditorsWebServiceAction onError)
    {
        
        final EditorsWebServiceHandler _this = this;
                
        this.doLogin ("To send an invite to <b>" + toEmail + "</b> you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                             
                           public void actionPerformed (ActionEvent ev)
                           {
                                         
                               Map data = new HashMap ();
                               data.put (FieldNames.email,
                                         toEmail);
                                   
                               _this.callService (Service.invites,
                                                  null,
                                                  data,                                                   
                                                  "POST",
                                                  onComplete,
                                                  onError);
                                       
                           }
                                   
                      },
                      null);
        
    }
            
    public void deleteAccount (final EditorsWebServiceAction onComplete,
                               final EditorsWebServiceAction onError)
    {
        
        if (EditorsEnvironment.getUserAccount () == null)
        {
            
            return;
            
        }
                        
        final EditorsWebServiceHandler _this = this;

        this.doLogin ("To delete your account you must first login to the Editors service.",
                      new ActionListener ()
                      {         
                             
                           public void actionPerformed (ActionEvent ev)
                           {
                                         
                               _this.callService (Service.accounts,
                                                  null,
                                                  null,                                                   
                                                  "DELETE",
                                                  onComplete,
                                                  onError);
                                       
                           }
                                   
                      },
                      null);
                                                                                        
    }

    public void createAccount (final String                  email,
                               final String                  password,
                               final PGPPublicKey            publicKey,
                               final EditorsWebServiceAction onComplete,
                               final EditorsWebServiceAction onError)
                        throws Exception
    {
        
        if (EditorsEnvironment.getUserAccount () != null)
        {
            
            throw new IllegalStateException ("Already have an account.");
            
        }
                
        final EditorsWebServiceHandler _this = this;
        
        Map data = new HashMap ();
        data.put (FieldNames.email,
                  email);
        data.put (FieldNames.password,
                  password);
        data.put (FieldNames.publicKey,
                  EditorsUtils.getPGPPublicKeyBase64Encoded (publicKey));
        
        this.callService (Service.accounts,
                          null,
                          data,
                          "POST",
                          onComplete,
                          onError);
                                                                
    }
 
    /*
    public void logout ()
    {
        
    }
    
    public void login ()
                       throws Exception
    {
        
        if (this.acc == null)
        {
            
            throw new GeneralException ("No account available");
            
        }
        
        Map data = new HashMap ();
        data.put (FieldNames.email,
                  this.acc.getEmail ());        
        data.put (FieldNames.password,
                  this.acc.getPassword ());
            
        Object d = this.doPost (Service.login,
                                null,
                                data);                
                
        this.sessionId = d.toString ();

        Environment.logDebugMessage ("Logged in, got session id: " + this.sessionId);
        
    }
    */
}