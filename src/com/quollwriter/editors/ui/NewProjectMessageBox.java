package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.events.*;

public class NewProjectMessageBox extends MessageBox<NewProjectMessage> implements EditorChangedListener
{
        
    private Box responseBox = null;
    private ProjectSentReceivedViewer sentViewer = null;
    private JLabel previousLabel = null;
        
    public NewProjectMessageBox (NewProjectMessage     mess,
                                 AbstractProjectViewer viewer)
    {
        
        super (mess,
               viewer);
        
    }
    
    @Override    
    public void editorChanged (EditorChangedEvent ev)
    {
        
        if (ev.getEditor () == this.message.getEditor ())
        {
        
            this.updateForEditor ();
            
        }
        
    }
    
    @Override
    public boolean isAutoDealtWith ()
    {
        
        return false;
        
    }    

    public static JComponent getProjectMessageDetails (final AbstractProjectMessage message,
                                                       final AbstractProjectViewer  viewer)
    {
        
        String plural = "";
        
        if (message.getChapters ().size () > 1)
        {
            
            plural = "s";
            
        }
        
        ProjectVersion projVer = message.getProjectVersion ();
        Date dueDate = projVer.getDueDate ();
        
        String notes = projVer.getDescription ();        
        String verName = projVer.getName ();
        
        Project proj = null;
        
        try
        {
                        
            proj = Environment.getProjectById (message.getForProjectId (),
                                               (message.isSentByMe () ? Project.NORMAL_PROJECT_TYPE : Project.EDITOR_PROJECT_TYPE));
                                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project for id: " +
                                  message.getForProjectId (),
                                  e);
                        
        }
        
        final Project fproj = proj;        
        
        // Show:
        //   * Project 
        //   * Sent
        //   * Version (optional)
        //   * Word/chapter count
        //   * Due by 
        //   * Notes (optional)
        //   * View link 
        
        String rows = "p, 6px, p";
                
        if (verName != null)
        {
            
            rows += ", 6px, p";
            
        }
        
        // Word count, due by
        rows += ", 6px, p, 6px, p";
                        
        if (notes != null)
        {
            
            rows += ", 6px, top:p";
            
        }

        rows += ", 6px, p";
        
        FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                        rows);

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;

        builder.addLabel (Environment.replaceObjectNames ("<html><i>{Project}</i></html>"),
                          cc.xy (1,
                                 row));
        
        if (proj != null)
        {
        
            JLabel openProj = UIUtils.createClickableLabel (message.getForProjectName (),
                                                            null,
                                                            new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                
                    if (fproj != null)
                    {
                
                        try
                        {
                
                            Environment.openProject (fproj);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to open project: " +
                                                  fproj,
                                                  e);
                            
                        }
                        
                    }
                    
                }
                
            });        
            
            openProj.setToolTipText (Environment.replaceObjectNames ("Click to open the {project}"));
            
            builder.add (openProj,
                         cc.xy (3,
                                row));
            
        } else {
            
            builder.addLabel (String.format ("<html>%s</html>",
                                             message.getForProjectName ()),
                              cc.xy (3,
                                     row));
            
        }
            
        row += 2;            
        
        builder.addLabel (Environment.replaceObjectNames (String.format ("<html><i>%s</i></html>",
                                                                         message.isSentByMe () ? "Sent" : "Received")),
                          cc.xy (1,
                                 row));
        
        builder.addLabel (Environment.formatDateTime (message.getWhen ()),
                          cc.xy (3,
                                 row));
        
        row += 2;

        if (verName != null)
        {
            
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Version</i></html>"),
                              cc.xy (1,
                                     row));
            
            builder.addLabel (verName,
                              cc.xy (3,
                                     row));
            
            row += 2;
            
        }
        
        builder.addLabel (Environment.replaceObjectNames (String.format ("%s words, %s {chapter%s}",
                                                                         Environment.formatNumber (message.getWordCount ()),
                                                                         message.getChapters ().size (),
                                                                         plural)),
                          cc.xy (3,
                                 row));
        
        row += 2;
        
        builder.addLabel (Environment.replaceObjectNames ("<html><i>Due by</i></html>"),
                          cc.xy (1,
                                 row));

        builder.addLabel ("<html>" + (dueDate != null ? Environment.formatDate (dueDate) : "<i>Not specified.</i>") + "</html>",
                          cc.xy (3,
                                 row));

        row += 2;
                                 
        if (notes != null)
        {
        
            builder.addLabel (Environment.replaceObjectNames ("<html><i>Notes</i></html>"),
                              cc.xy (1,
                                     row));
    
            JComponent nc = UIUtils.createHelpTextPane (notes,
                                                        viewer);
            nc.setBorder (null);
    
            builder.add (nc,
                         cc.xy (3,
                                row));

            row += 2;
                                
        }
       
        if (message.isSentByMe ())
        {
                        
            JLabel viewProj = UIUtils.createClickableLabel ("Click to view what you sent",
                                                            Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            new ActionListener ()
                                                            {
                                                                
            public void actionPerformed (ActionEvent ev)
            {
/*
                if (_this.sentViewer != null)
                {
                    
                    _this.sentViewer.setExtendedState (JFrame.NORMAL);
                    _this.sentViewer.toFront ();
                    
                    return;
                    
                }
  */              
                // Load up the project with the specific text.
                // See if we have a project viewer for the project.
                Project proj = null;
                
                try
                {
                    
                    proj = Environment.getProjectById (message.getForProjectId (),
                                                       Project.NORMAL_PROJECT_TYPE);
                
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to get project for: " +
                                          message.getForProjectId (),
                                          e);
                    
                    UIUtils.showErrorMessage (viewer,
                                              "Unable to show the {project}, please contact Quoll Writer support for assistance.");

                    return;                        
                    
                }

                final Project _proj = proj;
                
                ActionListener open = new ActionListener ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        String pwd = ev.getActionCommand ();
                        
                        if (pwd.equals (""))
                        {
                            
                            pwd = null;
                            
                        }
                        
                        Set<Chapter> chaps = null;
                        
                        try
                        {
                            
                            chaps = Environment.getVersionedChapters (_proj,
                                                                      message.getChapters (),
                                                                      pwd);
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to get versioned chapters for project: " +
                                                  _proj,
                                                  e);
                            
                            UIUtils.showErrorMessage (viewer,
                                                      "Unable to show {comments}, please contact Quoll Writer support for assistance.");
                            
                            return;
                            
                        }
                        
                        try
                        {
                        
                            // Need to "fill up" the project with the chapters and comments.
                            // Create a new project object just to be safe (in case the getProjectById call changes in the future).
                            Project np = new Project ();
                            np.setName (_proj.getName ());
                            
                            np.setType (Project.EDITOR_PROJECT_TYPE);
                            np.setId (message.getForProjectId ());
                            np.setName (message.getForProjectName ());
                                                          
                            Book b = new Book (np,
                                               np.getName ());
                            
                            np.addBook (b);
                                                                                  
                            for (Chapter c : chaps)
                            {
                                
                                b.addChapter (c);
                                                                
                            }
                            
                            final int chapterCount = chaps.size ();
                                                    
                            ProjectSentReceivedViewer pcv = new ProjectSentReceivedViewer<AbstractProjectMessage> (np,
                                                                                                                   message)
                            {
                              
                                public ProjectSentReceivedSideBar getSideBar ()
                                {

                                    return new ProjectSentReceivedSideBar<AbstractProjectMessage, ProjectSentReceivedViewer> (this,
                                                                                                                              this.message)
                                    {
                                  
                                        @Override
                                        public void onShow ()
                                        {
                                            
                                        }
                                  
                                        @Override
                                        public void onHide ()
                                        {
                                            
                                        }
                                  
                                        @Override
                                        public String getTitle ()
                                        {
                                            
                                            return "Sent to";
                                            
                                        }
                                  
                                        @Override
                                        public String getItemsIconType ()
                                        {
                                            
                                            return Chapter.OBJECT_TYPE;
                                            
                                        }
                                        
                                        @Override
                                        public String getItemsTitle ()
                                        {
                                            
                                            return "{Chapters}";
                                            
                                        }
                                  
                                        @Override
                                        public int getItemCount ()
                                        {
                                            
                                            return chapterCount;
                                            
                                        }
                                  
                                        @Override
                                        public JComponent getMessageDetails (AbstractProjectMessage message)
                                        {

                                            final ProjectSentReceivedSideBar _this = this;
                                    
                                            String rows = "p";
                                    
                                            ProjectVersion projVer = message.getProjectVersion ();
                                    
                                            String verName = projVer.getName ();
                                            
                                            if (verName != null)
                                            {
                                                
                                                rows += ", 6px, p";
                                                
                                            }
                                    
                                            final String notes = projVer.getDescription ();
                                            
                                            if (notes != null)
                                            {
                                                
                                                rows += ", 6px, top:p";
                                                    
                                            }
                                            
                                            FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                                                            rows);
                                    
                                            fl.setHonorsVisibility (true);
                                            PanelBuilder builder = new PanelBuilder (fl);
                                    
                                            CellConstraints cc = new CellConstraints ();
                                    
                                            int row = 1;
                                                    
                                            builder.addLabel (Environment.replaceObjectNames ("<html><i>{Sent}</i></html>"),
                                                              cc.xy (1,
                                                                     row));
                                            
                                            builder.addLabel ("<html>" + Environment.formatDateTime (message.getWhen ()) + "</html>",
                                                              cc.xy (3,
                                                                     row));        
                                            
                                            row += 2;
                                            
                                            if (verName != null)
                                            {
                                                
                                                builder.addLabel (Environment.replaceObjectNames ("<html><i>{Version}</i></html>"),
                                                                  cc.xy (1,
                                                                         row));
                                                builder.addLabel (String.format ("<html>%s</html>",
                                                                                 verName),
                                                                  cc.xy (3,
                                                                         row));                                                        
                                                
                                                row += 2;
                                                
                                            }
                                            
                                            if (notes != null)
                                            {
                                            
                                                builder.addLabel (Environment.replaceObjectNames ("<html><i>{Notes}</i></html>"),
                                                                  cc.xy (1,
                                                                         row));
                                        
                                                String commText = notes;
                                                                         
                                                TextIterator ti = new TextIterator (commText);
                                                
                                                if (ti.getSentenceCount () > 1)
                                                {
                                                    
                                                    commText = ti.getNextClosestSentenceTo (-1).getText ();
                                                
                                                    commText += "<br /><a href='#'>More, click to view all.</a>";
                                                                         
                                                }
                                                
                                                JComponent mess = UIUtils.createHelpTextPane (commText,
                                                                                              this.projectViewer);
                                                
                                                mess.addMouseListener (new MouseEventHandler ()
                                                {
                                                   
                                                    @Override
                                                    public void handlePress (MouseEvent ev)
                                                    {
                                        
                                                        UIUtils.showMessage ((PopupsSupported) _this.getProjectViewer (),
                                                                             "Notes",
                                                                             notes);
                                                        
                                                    }
                                                    
                                                });
                                                
                                                mess.setBorder (null);
                                                
                                                builder.add (mess,
                                                             cc.xy (3,
                                                                    row));

                                            }
                                                                     
                                            JPanel bp = builder.getPanel ();
                                            bp.setAlignmentX (Component.LEFT_ALIGNMENT);
                                            bp.setOpaque (false);
                                    
                                            return bp;
                                            
                                        }
                                        
                                    };
                                    
                                }

                                @Override
                                public void init ()
                                           throws Exception
                                {
                            
                                    super.init ();
                                        
                                    this.viewObject (this.proj.getBook (0).getChapters ().get (0));
                                            
                                }
                                
                                @Override
                                public String getViewerIcon ()
                                {
                            
                                    return Constants.PROJECT_ICON_NAME;
                            
                                }
                            
                                @Override
                                public String getViewerTitle ()
                                {
                            
                                    return "{Project} sent: " + this.proj.getName ();
                            
                                }                                
                                
                            };
                            
                            pcv.init ();
                            
                            //_this.sentViewer = pcv;
                            
                            pcv.addWindowListener (new WindowAdapter ()
                            {
                                
                                public void windowClosed (WindowEvent ev)
                                {
                                    
                                    //_this.sentViewer = null;
                                    
                                }
                                
                            });
                            
                        } catch (Exception e) {
                            
                            Environment.logError ("Unable to view project: " +
                                                  _proj,
                                                  e);
                            
                            UIUtils.showErrorMessage (viewer,
                                                      "Unable to show {project}, please contact Quoll Writer support for assistance.");
                            
                            return;
                            
                        }
                        
                    }
                    
                };
                          
                UIUtils.openProjectAndDoAction (proj,
                                                open,
                                                viewer);
                
            }});
            
            viewProj.setBorder (UIUtils.createPadding (0, 10, 0, 0));
            
            builder.add (viewProj,
                         cc.xywh (1,
                                  row,
                                  3,
                                  1));
                                                 
        } 
        
        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        
        return bp;        
        
    }
    
    public static JComponent getNewProjectMessageDetails (final NewProjectMessage     mess,
                                                          final AbstractProjectViewer viewer)
    {
        
        return NewProjectMessageBox.getProjectMessageDetails (mess,
                                                              viewer);
        
    }
    
    @Override
    public boolean isShowAttentionBorder ()
    {
        
        if (this.message.getEditor ().isPrevious ())
        {
            
            return false;
            
        }
        
        return super.isShowAttentionBorder ();
        
    }
    
    private void updateForEditor ()
    {
        
        this.previousLabel.setVisible (false);
        
        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPrevious ())
           )
        {
            
            this.previousLabel.setText (String.format ("<b>%s</b> is a previous {contact}.  This message can no longer be acted upon.",
                                                       this.message.getEditor ().getShortName ()));        
            
            this.previousLabel.setVisible (true);
                        
        }
        
    }
    
    public void doUpdate ()
    {
        
        this.responseBox.setVisible (!this.message.isDealtWith ());
        
    }
    
    public void doInit ()
    {
        
        EditorsEnvironment.addEditorChangedListener (this);
        
        final NewProjectMessageBox _this = this;
                
        Project proj = null;
        
        try
        {
                        
            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.NORMAL_PROJECT_TYPE : Project.EDITOR_PROJECT_TYPE));
                                    
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project for id: " +
                                  this.message.getForProjectId (),
                                  e);
                        
        }
        
        final Project fproj = proj;
        
        String text = "Sent {project}";
        
        if (!this.message.isSentByMe ())
        {
            
            text = "Invitation to edit a {project}";
                            
        }
        
        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.PROJECT_ICON_NAME);
        
        this.add (h);

        JComponent bp = NewProjectMessageBox.getProjectMessageDetails (this.message,
                                                                       this.projectViewer);
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));        
        
        this.add (bp);             
        
        this.responseBox = new Box (BoxLayout.Y_AXIS);
        
        this.responseBox.setVisible (false);
                
        this.add (this.responseBox);
        
        this.previousLabel = UIUtils.createInformationLabel ("");
        
        this.add (this.previousLabel);
                
        this.updateForEditor ();
        
        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
            &&
            (!this.message.getEditor ().isPrevious ())
           )
        {
            
            this.responseBox.setVisible (true);
            
            JComponent l = UIUtils.createBoldSubHeader ("Select your response below",
                                                        null);
            
            this.responseBox.add (l);           
            
            JButton accept = UIUtils.createButton ("Accept",
                                                   null);

            accept.setToolTipText ("Click to accept the invitation");
            accept.addActionListener (new ActionListener ()
                                                   {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                            
                                                            // Show a message box.
                                                            _this.showResponseMessagePopup (true);
                                                            
                                                        }
                                                        
                                                   });
            
            JButton reject = UIUtils.createButton ("Reject",
                                                   null);

            reject.setToolTipText ("Click to reject the invitation");
            reject.addActionListener (new ActionListener ()
                                                   {
                                                        
                                                        public void actionPerformed (ActionEvent ev)
                                                        {
                                                                   
                                                            _this.showResponseMessagePopup (false); 
                                                            
                                                        }
                                                        
                                                   });

            JButton[] buts = new JButton[] { accept, reject };

            JComponent bb = UIUtils.createButtonBar2 (buts,
                                                      Component.LEFT_ALIGNMENT);
                        
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            this.responseBox.add (bb); 
            
        }
                     
    }
    
    private void showResponseMessagePopup (final boolean accepted)
    {
        
        final NewProjectMessageBox _this = this;
        
        EditorsUIUtils.handleNewProjectResponse (_this.projectViewer,
                                                 _this.message,
                                                 accepted);
        
    }
    
}