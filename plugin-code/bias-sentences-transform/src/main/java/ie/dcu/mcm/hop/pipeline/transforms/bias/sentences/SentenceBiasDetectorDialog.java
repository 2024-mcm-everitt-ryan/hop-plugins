/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ie.dcu.mcm.hop.pipeline.transforms.bias.sentences;

import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import static org.apache.hop.core.util.Utils.isEmpty;
import static org.apache.hop.i18n.BaseMessages.getString;
import static org.eclipse.swt.SWT.*;

public class SentenceBiasDetectorDialog extends BaseTransformDialog implements ITransformDialog {
    private static final Class<?> PKG = SentenceBiasDetectorDialog.class; // For Translator

    private boolean gotPreviousFields = false;

    private CCombo wTaxonomyFieldName;
    private CCombo wSubjectivityFieldName;
    private CCombo wCorpusFieldName;
    //private TextVar wTaxonomyBlockName;
    private Button wGroupSentences;
    private Button wSentencesOnly;
    private Button wIgnoreCase;

    private final SentenceBiasDetectorMeta input;

    public SentenceBiasDetectorDialog(
            Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
        super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
        input = (SentenceBiasDetectorMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = e -> input.setChanged();

        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(getString(PKG, "SentenceBiasDetectorDialog.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = props.getMargin();

        // TransformName line
        wlTransformName = new Label(shell, RIGHT);
        wlTransformName.setText(getString(PKG, "SentenceBiasDetectorDialog.TransformName.Label"));
        props.setLook(wlTransformName);
        fdlTransformName = new FormData();
        fdlTransformName.left = new FormAttachment(0, 0);
        fdlTransformName.right = new FormAttachment(middle, -margin);
        fdlTransformName.top = new FormAttachment(0, margin);
        wlTransformName.setLayoutData(fdlTransformName);
        wTransformName = new Text(shell, SINGLE | LEFT | BORDER);
        wTransformName.setText(transformName);
        props.setLook(wTransformName);
        wTransformName.addModifyListener(lsMod);
        fdTransformName = new FormData();
        fdTransformName.left = new FormAttachment(middle, 0);
        fdTransformName.top = new FormAttachment(0, margin);
        fdTransformName.right = new FormAttachment(100, 0);
        wTransformName.setLayoutData(fdTransformName);

        // TaxonomyFieldName field
        Label wlTaxonomyFieldName = new Label(shell, RIGHT);
        wlTaxonomyFieldName.setText(getString(PKG, "SentenceBiasDetectorDialog.TaxonomyFieldName.Label"));
        props.setLook(wlTaxonomyFieldName);
        FormData fdlTaxonomyFieldName = new FormData();
        fdlTaxonomyFieldName.left = new FormAttachment(0, 0);
        fdlTaxonomyFieldName.right = new FormAttachment(middle, -margin);
        fdlTaxonomyFieldName.top = new FormAttachment(wTransformName, margin);
        wlTaxonomyFieldName.setLayoutData(fdlTaxonomyFieldName);

        wTaxonomyFieldName = new CCombo(shell, BORDER | READ_ONLY);
        props.setLook(wTaxonomyFieldName);
        wTaxonomyFieldName.addModifyListener(lsMod);
        FormData fdTaxonomyFieldName = new FormData();
        fdTaxonomyFieldName.left = new FormAttachment(middle, 0);
        fdTaxonomyFieldName.top = new FormAttachment(wTransformName, margin);
        fdTaxonomyFieldName.right = new FormAttachment(100, -margin);
        wTaxonomyFieldName.setLayoutData(fdTaxonomyFieldName);
        wTaxonomyFieldName.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                        Cursor busy = new Cursor(shell.getDisplay(), CURSOR_WAIT);
                        shell.setCursor(busy);
                        get();
                        shell.setCursor(null);
                        busy.dispose();
                    }
                });

        // SubjectivityFieldName field
        Label wlSubjectivityFieldName = new Label(shell, RIGHT);
        wlSubjectivityFieldName.setText(getString(PKG, "SentenceBiasDetectorDialog.SubjectivityFieldName.Label"));
        props.setLook(wlSubjectivityFieldName);
        FormData fdlSubjectivityFieldName = new FormData();
        fdlSubjectivityFieldName.left = new FormAttachment(0, 0);
        fdlSubjectivityFieldName.right = new FormAttachment(middle, -margin);
        fdlSubjectivityFieldName.top = new FormAttachment(wTaxonomyFieldName, margin);
        wlSubjectivityFieldName.setLayoutData(fdlSubjectivityFieldName);

        wSubjectivityFieldName = new CCombo(shell, BORDER | READ_ONLY);
        props.setLook(wSubjectivityFieldName);
        wSubjectivityFieldName.addModifyListener(lsMod);
        FormData fdSubjectivityFieldName = new FormData();
        fdSubjectivityFieldName.left = new FormAttachment(middle, 0);
        fdSubjectivityFieldName.top = new FormAttachment(wTaxonomyFieldName, margin);
        fdSubjectivityFieldName.right = new FormAttachment(100, -margin);
        wSubjectivityFieldName.setLayoutData(fdSubjectivityFieldName);
        wSubjectivityFieldName.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                        Cursor busy = new Cursor(shell.getDisplay(), CURSOR_WAIT);
                        shell.setCursor(busy);
                        get();
                        shell.setCursor(null);
                        busy.dispose();
                    }
                });

        // CorpusFieldName field
        Label wlCorpusFieldName = new Label(shell, RIGHT);
        wlCorpusFieldName.setText(getString(PKG, "SentenceBiasDetectorDialog.CorpusFieldName.Label"));
        props.setLook(wlCorpusFieldName);
        FormData fdlCorpusFieldName = new FormData();
        fdlCorpusFieldName.left = new FormAttachment(0, 0);
        fdlCorpusFieldName.right = new FormAttachment(middle, -margin);
        fdlCorpusFieldName.top = new FormAttachment(wSubjectivityFieldName, margin);
        wlCorpusFieldName.setLayoutData(fdlCorpusFieldName);

        wCorpusFieldName = new CCombo(shell, BORDER | READ_ONLY);
        props.setLook(wCorpusFieldName);
        wCorpusFieldName.addModifyListener(lsMod);
        FormData fdCorpusFieldName = new FormData();
        fdCorpusFieldName.left = new FormAttachment(middle, 0);
        fdCorpusFieldName.top = new FormAttachment(wSubjectivityFieldName, margin);
        fdCorpusFieldName.right = new FormAttachment(100, -margin);
        wCorpusFieldName.setLayoutData(fdCorpusFieldName);
        wCorpusFieldName.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                        Cursor busy = new Cursor(shell.getDisplay(), CURSOR_WAIT);
                        shell.setCursor(busy);
                        get();
                        shell.setCursor(null);
                        busy.dispose();
                    }
                });

        /*
        Label wlTaxonomyBlockName = new Label(shell, RIGHT);
        wlTaxonomyBlockName.setText(getString(PKG, "SentenceBiasDetectorDialog.TaxonomyJsonBlockName.Label"));
        props.setLook(wlTaxonomyBlockName);
        FormData fdlResult = new FormData();
        fdlResult.left = new FormAttachment(0, 0);
        fdlResult.right = new FormAttachment(middle, -2 * margin);
        fdlResult.top = new FormAttachment(wCorpusFieldName, margin * 2);
        wlTaxonomyBlockName.setLayoutData(fdlResult);

        wTaxonomyBlockName = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wTaxonomyBlockName.setToolTipText(getString(PKG, "SentenceBiasDetectorDialog.TaxonomyJsonBlockName.Tooltip"));
        props.setLook(wTaxonomyBlockName);
        wTaxonomyBlockName.addModifyListener(lsMod);
        FormData fdResult = new FormData();
        fdResult.left = new FormAttachment(middle, -margin);
        fdResult.top = new FormAttachment(wCorpusFieldName, margin * 2);
        fdResult.right = new FormAttachment(100, 0);
        wTaxonomyBlockName.setLayoutData(fdResult);

         */

        // groupSentences
        Label wlGroupSentences = new Label(shell, RIGHT);
        wlGroupSentences.setText(getString(PKG, "SentenceBiasDetectorDialog.GroupSentences.Label"));
        props.setLook(wlGroupSentences);
        FormData fdlGroupSentences = new FormData();
        fdlGroupSentences.left = new FormAttachment(0, 0);
        fdlGroupSentences.top = new FormAttachment(wCorpusFieldName, margin);
        fdlGroupSentences.right = new FormAttachment(middle, -2 * margin);
        wlGroupSentences.setLayoutData(fdlGroupSentences);

        wGroupSentences = new Button(shell, CHECK);
        wGroupSentences.setSelection(input.isGroupSentences());
        props.setLook(wGroupSentences);
        wGroupSentences.setToolTipText(getString(PKG, "SentenceBiasDetectorDialog.GroupSentences.Tooltip"));
        FormData fdGroupSentences = new FormData();
        fdGroupSentences.left = new FormAttachment(middle, -margin);
        fdGroupSentences.top = new FormAttachment(wCorpusFieldName, margin * 2);
        fdGroupSentences.right = new FormAttachment(100, 0);
        wGroupSentences.setLayoutData(fdGroupSentences);
        wGroupSentences.addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        input.setChanged();
                    }
                });

        // sentencesOnly
        Label wlSentencesOnly = new Label(shell, RIGHT);
        wlSentencesOnly.setText(getString(PKG, "SentenceBiasDetectorDialog.SentencesOnly.Label"));
        props.setLook(wlSentencesOnly);
        FormData fdlSentencesOnly = new FormData();
        fdlSentencesOnly.left = new FormAttachment(0, 0);
        fdlSentencesOnly.top = new FormAttachment(wGroupSentences, margin);
        fdlSentencesOnly.right = new FormAttachment(middle, -2 * margin);
        wlSentencesOnly.setLayoutData(fdlSentencesOnly);

        wSentencesOnly = new Button(shell, CHECK);
        wSentencesOnly.setSelection(input.isSentencesOnly());
        props.setLook(wSentencesOnly);
        wSentencesOnly.setToolTipText(getString(PKG, "SentenceBiasDetectorDialog.SentencesOnly.Tooltip"));
        FormData fdSentencesOnly = new FormData();
        fdSentencesOnly.left = new FormAttachment(middle, -margin);
        fdSentencesOnly.top = new FormAttachment(wGroupSentences, margin * 2);
        fdSentencesOnly.right = new FormAttachment(100, 0);
        wSentencesOnly.setLayoutData(fdSentencesOnly);
        wSentencesOnly.addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        input.setChanged();
                    }
                });

        // ignoreCase
        Label wlIgnoreCase = new Label(shell, RIGHT);
        wlIgnoreCase.setText(getString(PKG, "SentenceBiasDetectorDialog.IgnoreCase.Label"));
        props.setLook(wlIgnoreCase);
        FormData fdlIgnoreCase = new FormData();
        fdlIgnoreCase.left = new FormAttachment(0, 0);
        fdlIgnoreCase.top = new FormAttachment(wSentencesOnly, margin);
        fdlIgnoreCase.right = new FormAttachment(middle, -2 * margin);
        wlIgnoreCase.setLayoutData(fdlIgnoreCase);

        wIgnoreCase = new Button(shell, CHECK);
        wIgnoreCase.setSelection(input.isIgnoreCase());
        props.setLook(wIgnoreCase);
        wIgnoreCase.setToolTipText(getString(PKG, "SentenceBiasDetectorDialog.IgnoreCase.Tooltip"));
        FormData fdIgnoreCase = new FormData();
        fdIgnoreCase.left = new FormAttachment(middle, -margin);
        fdIgnoreCase.top = new FormAttachment(wSentencesOnly, margin * 2);
        fdIgnoreCase.right = new FormAttachment(100, 0);
        wIgnoreCase.setLayoutData(fdIgnoreCase);
        wIgnoreCase.addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        input.setChanged();
                    }
                });

        // THE BUTTONS
        wOk = new Button(shell, PUSH);
        wOk.setText(getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, PUSH);
        wCancel.setText(getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[]{wOk, wCancel}, margin, wIgnoreCase);

        // Add listeners
        wOk.addListener(Selection, e -> ok());
        wCancel.addListener(Selection, e -> cancel());

        getData();
        input.setChanged(changed);

        BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

        return transformName;
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData() {
        if (input.getTaxonomyField() != null) {
            wTaxonomyFieldName.setText(input.getTaxonomyField());
        }
        if (input.getSubjectivityField() != null) {
            wSubjectivityFieldName.setText(input.getSubjectivityField());
        }
        if (input.getCorpusField() != null) {
            wCorpusFieldName.setText(input.getCorpusField());
        }

       // if (input.getTaxonomyJsonBlockName() != null) {
        //    wTaxonomyBlockName.setText(input.getTaxonomyJsonBlockName());
        //}

        if (input.isGroupSentences()) {
            wGroupSentences.setEnabled(input.isGroupSentences());
        }
        if (input.isSentencesOnly()) {
            wSentencesOnly.setEnabled(input.isSentencesOnly());
        }
        if (input.isIgnoreCase()) {
            wIgnoreCase.setEnabled(input.isIgnoreCase());
        }

        wTransformName.selectAll();
        wTransformName.setFocus();
    }

    private void cancel() {
        transformName = null;
        input.setChanged(changed);
        dispose();
    }

    private void ok() {
        if (isEmpty(wTransformName.getText())) {
            return;
        }


        input.setTaxonomyField(wTaxonomyFieldName.getText());
        input.setSubjectivityField(wSubjectivityFieldName.getText());
        input.setCorpusField(wCorpusFieldName.getText());
       // input.setTaxonomyJsonBlockName(wTaxonomyBlockName.getText());
        input.setGroupSentences(wGroupSentences.getSelection());
        input.setSentencesOnly(wSentencesOnly.getSelection());
        input.setIgnoreCase(wIgnoreCase.getSelection());

        transformName = wTransformName.getText(); // return value

        dispose();
    }

    private void get() {
        if (!gotPreviousFields) {
            try {
                String taxonomyField = null;
                String SubjectivityField = null;
                String corpusField = null;


                if (wTaxonomyFieldName.getText() != null) {
                    taxonomyField = wTaxonomyFieldName.getText();
                }
                if (wSubjectivityFieldName.getText() != null) {
                    SubjectivityField = wSubjectivityFieldName.getText();
                }
                if (wCorpusFieldName.getText() != null) {
                    corpusField = wCorpusFieldName.getText();
                }

                wTaxonomyFieldName.removeAll();
                wSubjectivityFieldName.removeAll();
                wCorpusFieldName.removeAll();

                IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
                if (r != null) {
                    wTaxonomyFieldName.setItems(r.getFieldNames());
                    wSubjectivityFieldName.setItems(r.getFieldNames());
                    wCorpusFieldName.setItems(r.getFieldNames());
                }
                if (taxonomyField != null) {
                    wTaxonomyFieldName.setText(taxonomyField);
                }
                if (SubjectivityField != null) {
                    wSubjectivityFieldName.setText(SubjectivityField);
                }
                if (corpusField != null) {
                    wCorpusFieldName.setText(corpusField);
                }
                gotPreviousFields = true;
            } catch (HopException ke) {
                new ErrorDialog(
                        shell,
                        getString(PKG, "SentenceBiasDetectorDialog.FailedToGetFields.DialogTitle"),
                        getString(PKG, "SentenceBiasDetectorDialog.FailedToGetFields.DialogMessage"),
                        ke);
            }
        }
    }
}
