/*
 * Created on Sep 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2005 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.EditorPart;

import java.io.IOException;

import com.bluejungle.destiny.policymanager.Activator;
import com.bluejungle.destiny.policymanager.util.LoggingUtil;
import com.bluejungle.destiny.policymanager.util.ResourceManager;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionexception.InvalidVersionException;
import com.bluejungle.versionfactory.VersionFactory;

/**
 * @author aweber
 */
public class AboutPart extends EditorPart {

    private IEditorSite site;

    public AboutPart() {
    }

    public void init(IEditorSite site, IEditorInput input) {
        this.site = site;
        setInput(input);
    }

    public IWorkbenchPartSite getSite() {
        return (IWorkbenchPartSite) site;
    }

    public IEditorSite getEditorSite() {
        return site;
    }

    public void createPartControl(Composite parent) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        parent.setLayout(layout);
        parent.setBackground(ColorBundle.WHITE);

        Composite titleBar = new Composite(parent, SWT.NONE);
        titleBar.setBackground(ColorBundle.CE_BLUE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        titleBar.setLayoutData(data);
        titleBar.setLayout(new FormLayout());

        FormData fData = new FormData();

        Label applicationImage = new Label(titleBar, SWT.LEFT);
        applicationImage.setImage(ImageBundle.POLICY_AUTHOR_IMAGE);
        applicationImage.setBackground(ColorBundle.CE_BLUE);
        fData.left = new FormAttachment(0, 20);
        fData.top = new FormAttachment(0, 5);
        applicationImage.setLayoutData(fData);

        Label policyManagerTitle = new Label(titleBar, SWT.LEFT);
        policyManagerTitle.setText(ApplicationMessages.ABOUTPART_TITLE);
        policyManagerTitle.setBackground(ColorBundle.CE_BLUE);
        policyManagerTitle.setForeground(ColorBundle.WHITE);
        policyManagerTitle.setFont(FontBundle.BIGGEST_ARIAL);
        fData = new FormData();
        fData.left = new FormAttachment(0, 77);
        fData.top = new FormAttachment(0, 5);
        policyManagerTitle.setLayoutData(fData);

        CLabel logo = new CLabel(titleBar, SWT.RIGHT);
        logo.setBackground(ColorBundle.CE_BLUE);
        logo.setImage(ImageBundle.LOGO);
        fData = new FormData();
        fData.left = new FormAttachment(policyManagerTitle, 0);
        fData.right = new FormAttachment(100, -15);
        fData.top = new FormAttachment(0, 12);
        logo.setLayoutData(fData);

        new Label(parent, SWT.NONE);

        Composite applicationNameAndVersionComposite = new Composite(parent, SWT.NONE);
        applicationNameAndVersionComposite.setBackground(ColorBundle.WHITE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        applicationNameAndVersionComposite.setLayoutData(data);
        
        GridLayout applicationNameAndVersionLayout = new GridLayout();
        applicationNameAndVersionLayout.marginWidth = 0;
        applicationNameAndVersionLayout.marginHeight = 0;
        applicationNameAndVersionLayout.numColumns = 2;
        applicationNameAndVersionComposite.setLayout(applicationNameAndVersionLayout);
        
        
        Label applicationNameLabel = new Label(applicationNameAndVersionComposite, SWT.LEFT | SWT.WRAP);
        applicationNameLabel.setText(ApplicationMessages.ABOUTPART_APPLICATION_NAME);
        applicationNameLabel.setFont(FontBundle.NINE_POINT_ARIAL);
        applicationNameLabel.setForeground(ColorBundle.CE_BLUE);
        applicationNameLabel.setBackground(ColorBundle.WHITE);
        data = new GridData();
        applicationNameLabel.setLayoutData(data);

        Label versionLabel = new Label(applicationNameAndVersionComposite, SWT.LEFT | SWT.WRAP);
        versionLabel.setFont(FontBundle.NINE_POINT_ARIAL);
        versionLabel.setForeground(ColorBundle.CE_BLUE);
        versionLabel.setBackground(ColorBundle.WHITE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        versionLabel.setLayoutData(data);
        VersionFactory versionFactory = new VersionFactory();
        try {
            IVersion version = versionFactory.getVersion();
            StringBuffer versionString = new StringBuffer();
            versionString.append(version.getMajor());
            versionString.append(".");
            versionString.append(version.getMinor());
            versionString.append(".");
            versionString.append(version.getMaintenance());
            versionString.append(" (");
            versionString.append(version.getBuild());
            versionString.append(")");
            versionLabel.setText(versionString.toString());
        } catch (InvalidVersionException exception) {
            // Shouldn't happen
            LoggingUtil.logWarning(Activator.ID, "Failed to find Policy Author version.", exception);
            versionLabel.setText(String.valueOf(ApplicationMessages.ABOUTPART_UNKNOWN_VERSION));
        } catch (IOException exception) {
            LoggingUtil.logWarning(Activator.ID, "Failed to find Policy Author version.", exception);
            versionLabel.setText(String.valueOf(ApplicationMessages.ABOUTPART_UNKNOWN_VERSION));
        }

        new Label(parent, SWT.NONE);
        
        Label infoLabel = new Label(parent, SWT.WRAP);
        infoLabel.setFont(FontBundle.ATOM_FONT);
        infoLabel.setForeground(ColorBundle.CE_DK_BLUE);
        infoLabel.setBackground(ColorBundle.WHITE);
        infoLabel.setText(ApplicationMessages.ABOUTPART_LOGGED_IN + GlobalState.server + ApplicationMessages.ABOUTPART_AS + GlobalState.user);
        data = new GridData(GridData.FILL_HORIZONTAL);
        infoLabel.setLayoutData(data);

        Composite space = new Composite(parent, SWT.NONE);
        space.setBackground(ResourceManager.getColor(SWT.COLOR_WHITE));
        data = new GridData(GridData.FILL_BOTH);
        space.setLayoutData(data);

        Label copyrightLabel = new Label(parent, SWT.LEFT | SWT.WRAP);
        copyrightLabel.setText(ApplicationMessages.ABOUTPART_COPYRIGHT);
        copyrightLabel.setFont(FontBundle.NINE_POINT_ARIAL);
        copyrightLabel.setBackground(ColorBundle.WHITE);
        copyrightLabel.setForeground(ColorBundle.CE_BLUE);
        data = new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL);
        copyrightLabel.setLayoutData(data);
    }

    public void setFocus() {
    }

    public void doSave(IProgressMonitor monitor) {
    }

    public void doSaveAs() {
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    static class AboutDomainObjectInput extends PlatformObject implements IEditorInput {

        public boolean exists() {
            return true;
        }

        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        public String getName() {
            return ApplicationMessages.ABOUTPART_ABOUT;
        }

        public IPersistableElement getPersistable() {
            return null;
        }

        public String getToolTipText() {
            return this.getName();
        }

        public boolean equals(Object o) {
            if (o instanceof AboutDomainObjectInput)
                return true;
            else
                return super.equals(o);
        }
    }

    public static IEditorInput getAboutEditorInput() {
        return new AboutDomainObjectInput();
    }
}
