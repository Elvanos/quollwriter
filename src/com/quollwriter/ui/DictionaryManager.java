package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

//import com.jgoodies.forms.builder.*;
//import com.jgoodies.forms.factories.*;
//import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.events.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;

public class DictionaryManager extends TypesEditor implements TypesHandler
{

    private FileWatcher watcher = null;
    private Set<PropertyChangedListener> listeners = new HashSet<> ();

    public DictionaryManager (final AbstractViewer pv)
    {

        super (pv);

    }

    @Override
    public boolean renameType (String  oldType,
                               String  newType,
                               boolean reload)
    {

        this.removeType (oldType,
                         reload);
        this.addType (newType,
                      reload);

        return true;

    }

    @Override
    public boolean removeType (String  type,
                               boolean reload)
    {

        DictionaryProvider.removeUserWord (type);

        this.viewer.fireProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                                      ProjectEvent.REMOVE_WORD,
                                      type);

        return true;

    }

    @Override
    public void addType (String  t,
                         boolean reload)
    {

        DictionaryProvider.addUserWord (t);

        this.viewer.fireProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                                      ProjectEvent.ADD_WORD,
                                      t);

    }

    @Override
    public Set<String> getTypes ()
    {

        // Get the words.
        File userDict = Environment.getUserDictionaryFile ();

        Set<String> words = new TreeSet<> ();

        String w = null;

        try
        {

            w = IOUtils.getFile (userDict);

        } catch (Exception e)
        {

            w = "";

            Environment.logError ("Unable to get user dictionary file: " +
                                  userDict,
                                  e);

        }

        StringTokenizer tt = new StringTokenizer (w,
                                                  String.valueOf ('\n'));

        while (tt.hasMoreTokens ())
        {

            words.add (tt.nextToken ());

        }

        return words;

    }

    @Override
    public void removePropertyChangedListener (PropertyChangedListener l)
    {

        this.listeners.remove (l);

    }

    @Override
    public void addPropertyChangedListener (PropertyChangedListener l)
    {

        this.listeners.add (l);

    }

    @Override
    public TypesHandler getTypesHandler ()
    {

        return this;

    }

    @Override
    public String getNewItemsTitle ()
    {

        return Environment.getUIString (LanguageStrings.dictionary,
                                        LanguageStrings.manage,
                                        LanguageStrings.newwords,
                                        LanguageStrings.title);
    }

    @Override
    public String getNewItemsHelp ()
    {

        return Environment.getUIString (LanguageStrings.dictionary,
                                        LanguageStrings.manage,
                                        LanguageStrings.newwords,
                                        LanguageStrings.text);
    }

    @Override
    public String getExistingItemsTitle ()
    {

        return Environment.getUIString (LanguageStrings.dictionary,
                                        LanguageStrings.manage,
                                        LanguageStrings.table,
                                        LanguageStrings.title);

    }

    @Override
    public void init ()
    {

        super.init ();

        final DictionaryManager _this = this;

        this.watcher = new FileWatcher ();
        this.watcher.addFile (Environment.getUserDictionaryFile ());

        this.watcher.addFileChangeListener (new FileChangeListener ()
          {

              public void fileChanged (FileChangeEvent ev,
                                       int             types)
              {

                  // Tell the listeners about the change.
                  for (PropertyChangedListener l : _this.listeners)
                  {

                      l.propertyChanged (new PropertyChangedEvent (_this,
                                                                   "changed",
                                                                   0,
                                                                   0));

                  }

              }

          },
          FileChangeEvent.MODIFIED | FileChangeEvent.EXISTS);

        this.watcher.start ();

    }

}
