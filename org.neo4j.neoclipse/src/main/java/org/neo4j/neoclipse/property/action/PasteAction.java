/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.property.action;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.PropertyContainer;
import org.neo4j.api.core.Transaction;
import org.neo4j.neoclipse.Activator;
import org.neo4j.neoclipse.NeoIcons;
import org.neo4j.neoclipse.property.NeoPropertySheetPage;

/**
 * Action to add a new property to a PropertyContainer.
 * @author Anders Nawroth
 */
public class PasteAction extends PropertyAction
{
    private Shell shell;
    private static final TextTransfer TRANSFER_TYPE = TextTransfer
        .getInstance();

    public PasteAction( final Composite parent,
        final NeoPropertySheetPage propertySheet )
    {
        super( "Paste", NeoIcons.PASTE.getDescriptor(), parent, propertySheet );
        shell = propertySheet.getControl().getShell();
    }

    @Override
    public void run()
    {
        PropertyContainer propertyContainer = getPropertyContainer();
        if ( propertyContainer == null )
        {
            return;
        }
        performOperation( propertyContainer );
    }

    /**
     * @param entry
     * @param parFirstElement
     */
    protected void performOperation( PropertyContainer container )
    {
        NeoService ns = Activator.getDefault().getNeoServiceSafely();
        if ( ns == null )
        {
            return;
        }
        Clipboard clipboard = new Clipboard( shell.getDisplay() );
        Object data = clipboard.getContents( TRANSFER_TYPE );
        clipboard.dispose();
        if ( !(data instanceof String) )
        {
            MessageDialog.openError( shell, "Error",
                "Could not paste content from the clipboard." );
            return;
        }
        // parse the string
        ClipboardUtil cu = new ClipboardUtil( (String) data );
        if ( cu.getValue() == null )
        {
            MessageDialog
                .openError( shell, "Error",
                    "The clipboard content doesn't seem to be a neo4j property value." );
            return;
        }
        Transaction tx = ns.beginTx();
        try
        {
            container.setProperty( cu.getKey(), cu.getValue() );
            tx.success();
        }
        finally
        {
            tx.finish();
        }
        propertySheet.fireChangeEvent( container, cu.getKey() );
        propertySheet.refresh();
    }
}