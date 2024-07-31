package ie.dcu.mcm.hop.pipeline.transforms.extractor;

import ie.dcu.mcm.hop.pipeline.transforms.extractor.TaxonomyContextExtractorMeta.ContextMeasurementType;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
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

import static ie.dcu.mcm.hop.pipeline.transforms.extractor.TaxonomyContextExtractorMeta.ContextMeasurementType.getTypeFromDescription;
import static org.apache.hop.core.Const.FORM_MARGIN;
import static org.apache.hop.core.Const.toInt;
import static org.apache.hop.core.util.Utils.isEmpty;
import static org.apache.hop.i18n.BaseMessages.getString;
import static org.eclipse.swt.SWT.*;

public class TaxonomyContextExtractorDialog extends BaseTransformDialog implements ITransformDialog {
    private static final Class<?> PKG = TaxonomyContextExtractorDialog.class; // For Translator
    private final TaxonomyContextExtractorMeta input;
    private boolean gotPreviousFields = false;
    private CCombo wCorpusFieldName;
    private CCombo wTaxonomyFieldName;
    private CCombo wDocumentPartitionFieldName;
    private CCombo wContextMeasurement;

    private TextVar wLeftContextMaxSize;
    private TextVar wLeftContextTargetSize;
    private TextVar wRightContextMaxSize;
    private TextVar wRightContextTargetSize;

    private Button wIgnoreCase;

    public TaxonomyContextExtractorDialog(
            Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
        super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
        input = (TaxonomyContextExtractorMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();

        shell = new Shell(parent, DIALOG_TRIM | RESIZE | MAX | MIN);
        PropsUi.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = e -> input.setChanged();

        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = FORM_MARGIN;
        formLayout.marginHeight = FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(getString(PKG, "TaxonomyContextExtractorDialog.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = PropsUi.getMargin();

        // TransformName line
        wlTransformName = new Label(shell, RIGHT);
        wlTransformName.setText(getString(PKG, "TaxonomyContextExtractorDialog.TransformName.Label"));
        PropsUi.setLook(wlTransformName);
        fdlTransformName = new FormData();
        fdlTransformName.left = new FormAttachment(0, 0);
        fdlTransformName.right = new FormAttachment(middle, -margin);
        fdlTransformName.top = new FormAttachment(0, margin);
        wlTransformName.setLayoutData(fdlTransformName);
        wTransformName = new Text(shell, SINGLE | LEFT | BORDER);
        wTransformName.setText(transformName);
        PropsUi.setLook(wTransformName);
        wTransformName.addModifyListener(lsMod);
        fdTransformName = new FormData();
        fdTransformName.left = new FormAttachment(middle, 0);
        fdTransformName.top = new FormAttachment(0, margin);
        fdTransformName.right = new FormAttachment(100, 0);
        wTransformName.setLayoutData(fdTransformName);


        // CorpusFieldName field
        Label wlCorpusFieldName = new Label(shell, RIGHT);
        wlCorpusFieldName.setText(getString(PKG, "TaxonomyContextExtractorDialog.CorpusFieldName.Label"));
        PropsUi.setLook(wlCorpusFieldName);
        FormData fdlCorpusFieldName = new FormData();
        fdlCorpusFieldName.left = new FormAttachment(0, 0);
        fdlCorpusFieldName.right = new FormAttachment(middle, -margin);
        fdlCorpusFieldName.top = new FormAttachment(wTransformName, margin);
        wlCorpusFieldName.setLayoutData(fdlCorpusFieldName);
        wCorpusFieldName = new CCombo(shell, BORDER | READ_ONLY);
        PropsUi.setLook(wCorpusFieldName);
        wCorpusFieldName.addModifyListener(lsMod);
        FormData fdCorpusFieldName = new FormData();
        fdCorpusFieldName.left = new FormAttachment(middle, 0);
        fdCorpusFieldName.top = new FormAttachment(wTransformName, margin);
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


        // TaxonomyFieldName field
        Label wlTaxonomyFieldName = new Label(shell, RIGHT);
        wlTaxonomyFieldName.setText(getString(PKG, "TaxonomyContextExtractorDialog.TaxonomyFieldName.Label"));
        PropsUi.setLook(wlTaxonomyFieldName);
        FormData fdlTaxonomyFieldName = new FormData();
        fdlTaxonomyFieldName.left = new FormAttachment(0, 0);
        fdlTaxonomyFieldName.right = new FormAttachment(middle, -margin);
        fdlTaxonomyFieldName.top = new FormAttachment(wCorpusFieldName, margin);
        wlTaxonomyFieldName.setLayoutData(fdlTaxonomyFieldName);
        wTaxonomyFieldName = new CCombo(shell, BORDER | READ_ONLY);
        PropsUi.setLook(wTaxonomyFieldName);
        wTaxonomyFieldName.addModifyListener(lsMod);
        FormData fdTaxonomyFieldName = new FormData();
        fdTaxonomyFieldName.left = new FormAttachment(middle, 0);
        fdTaxonomyFieldName.top = new FormAttachment(wCorpusFieldName, margin);
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



        // DocumentPartitionFieldName field
        Label wlDocumentPartitionFieldName = new Label(shell, RIGHT);
        wlDocumentPartitionFieldName.setText(getString(PKG, "TaxonomyContextExtractorDialog.DocumentPartitionFieldName.Label"));
        PropsUi.setLook(wlDocumentPartitionFieldName);
        FormData fdlDocumentPartitionFieldName = new FormData();
        fdlDocumentPartitionFieldName.left = new FormAttachment(0, 0);
        fdlDocumentPartitionFieldName.right = new FormAttachment(middle, -margin);
        fdlDocumentPartitionFieldName.top = new FormAttachment(wTaxonomyFieldName, margin);
        wlDocumentPartitionFieldName.setLayoutData(fdlDocumentPartitionFieldName);
        wDocumentPartitionFieldName = new CCombo(shell, BORDER | READ_ONLY);
        PropsUi.setLook(wDocumentPartitionFieldName);
        wDocumentPartitionFieldName.addModifyListener(lsMod);
        FormData fdDocumentPartitionFieldName = new FormData();
        fdDocumentPartitionFieldName.left = new FormAttachment(middle, 0);
        fdDocumentPartitionFieldName.top = new FormAttachment(wTaxonomyFieldName, margin);
        fdDocumentPartitionFieldName.right = new FormAttachment(100, -margin);
        wDocumentPartitionFieldName.setLayoutData(fdDocumentPartitionFieldName);
        wDocumentPartitionFieldName.addFocusListener(
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


        // ContextMeasurement
        Label wlContextMeasurement = new Label(shell, RIGHT);
        wlContextMeasurement.setText(getString(PKG, "TaxonomyContextExtractorDialog.ContextMeasurement.Label"));
        PropsUi.setLook(wlContextMeasurement);
        FormData fdContextMeasurementType = new FormData();
        fdContextMeasurementType.left = new FormAttachment(0, 0);
        fdContextMeasurementType.right = new FormAttachment(middle, -margin);
        fdContextMeasurementType.top = new FormAttachment(wDocumentPartitionFieldName, margin);
        wlContextMeasurement.setLayoutData(fdContextMeasurementType);
        wContextMeasurement = new CCombo(shell, SINGLE | READ_ONLY | BORDER);
        wContextMeasurement.setItems(ContextMeasurementType.getDescriptions());
        wContextMeasurement.select(0);
        PropsUi.setLook(wContextMeasurement);
        FormData fdType = new FormData();
        fdType.left = new FormAttachment(middle, 0);
        fdType.top = new FormAttachment(wDocumentPartitionFieldName, margin);
        fdType.right = new FormAttachment(100, 0);
        wContextMeasurement.setLayoutData(fdType);
        wContextMeasurement.addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        input.setChanged();
                    }
                });

        // LeftContextTargetSize
        Label wlLeftContextTargetSize = new Label(shell, RIGHT);
        wlLeftContextTargetSize.setText(getString(PKG, "TaxonomyContextExtractorDialog.LeftContextTargetSize.Label"));
        PropsUi.setLook(wlLeftContextTargetSize);
        FormData fdlLeftContextTargetSize = new FormData();
        fdlLeftContextTargetSize.left = new FormAttachment(0, 0);
        fdlLeftContextTargetSize.right = new FormAttachment(middle, -margin);
        fdlLeftContextTargetSize.top = new FormAttachment(wContextMeasurement, margin);
        wlLeftContextTargetSize.setLayoutData(fdlLeftContextTargetSize);
        wLeftContextTargetSize = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wLeftContextTargetSize.setText("");
        PropsUi.setLook(wLeftContextTargetSize);
        wLeftContextTargetSize.addModifyListener(lsMod);
        FormData fdLeftContextTargetSize = new FormData();
        fdLeftContextTargetSize.left = new FormAttachment(middle, 0);
        fdLeftContextTargetSize.top = new FormAttachment(wContextMeasurement, margin);
        fdLeftContextTargetSize.right = new FormAttachment(100, 0);
        wLeftContextTargetSize.setLayoutData(fdLeftContextTargetSize);


        // RightContextTargetSize
        Label wlRightContextTargetSize = new Label(shell, RIGHT);
        wlRightContextTargetSize.setText(getString(PKG, "TaxonomyContextExtractorDialog.RightContextTargetSize.Label"));
        PropsUi.setLook(wlRightContextTargetSize);
        FormData fdlRightContextTargetSize = new FormData();
        fdlRightContextTargetSize.left = new FormAttachment(0, 0);
        fdlRightContextTargetSize.right = new FormAttachment(middle, -margin);
        fdlRightContextTargetSize.top = new FormAttachment(wLeftContextTargetSize, margin);
        wlRightContextTargetSize.setLayoutData(fdlRightContextTargetSize);
        wRightContextTargetSize = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wRightContextTargetSize.setText("");
        PropsUi.setLook(wRightContextTargetSize);
        wRightContextTargetSize.addModifyListener(lsMod);
        FormData fdRightContextTargetSize = new FormData();
        fdRightContextTargetSize.left = new FormAttachment(middle, 0);
        fdRightContextTargetSize.top = new FormAttachment(wLeftContextTargetSize, margin);
        fdRightContextTargetSize.right = new FormAttachment(100, 0);
        wRightContextTargetSize.setLayoutData(fdRightContextTargetSize);


        // LeftContextMaxSize
        Label wlLeftContextMaxSize = new Label(shell, RIGHT);
        wlLeftContextMaxSize.setText(getString(PKG, "TaxonomyContextExtractorDialog.LeftContextMaxSize.Label"));
        PropsUi.setLook(wlLeftContextMaxSize);
        FormData fdlLeftContextMaxSize = new FormData();
        fdlLeftContextMaxSize.left = new FormAttachment(0, 0);
        fdlLeftContextMaxSize.right = new FormAttachment(middle, -margin);
        fdlLeftContextMaxSize.top = new FormAttachment(wRightContextTargetSize, margin);
        wlLeftContextMaxSize.setLayoutData(fdlLeftContextMaxSize);
        wLeftContextMaxSize = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wLeftContextMaxSize.setText("");
        PropsUi.setLook(wLeftContextMaxSize);
        wLeftContextMaxSize.addModifyListener(lsMod);
        FormData fdLeftContextMaxSize = new FormData();
        fdLeftContextMaxSize.left = new FormAttachment(middle, 0);
        fdLeftContextMaxSize.top = new FormAttachment(wRightContextTargetSize, margin);
        fdLeftContextMaxSize.right = new FormAttachment(100, 0);
        wLeftContextMaxSize.setLayoutData(fdLeftContextMaxSize);


        // RightContextMaxSize
        Label wlRightContextMaxSize = new Label(shell, RIGHT);
        wlRightContextMaxSize.setText(getString(PKG, "TaxonomyContextExtractorDialog.RightContextMaxSize.Label"));
        PropsUi.setLook(wlRightContextMaxSize);
        FormData fdlRightContextMaxSize = new FormData();
        fdlRightContextMaxSize.left = new FormAttachment(0, 0);
        fdlRightContextMaxSize.right = new FormAttachment(middle, -margin);
        fdlRightContextMaxSize.top = new FormAttachment(wLeftContextMaxSize, margin);
        wlRightContextMaxSize.setLayoutData(fdlRightContextMaxSize);
        wRightContextMaxSize = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wRightContextMaxSize.setText("");
        PropsUi.setLook(wRightContextMaxSize);
        wRightContextMaxSize.addModifyListener(lsMod);
        FormData fdRightContextMaxSize = new FormData();
        fdRightContextMaxSize.left = new FormAttachment(middle, 0);
        fdRightContextMaxSize.top = new FormAttachment(wLeftContextMaxSize, margin);
        fdRightContextMaxSize.right = new FormAttachment(100, 0);
        wRightContextMaxSize.setLayoutData(fdRightContextMaxSize);


        // IgnoreCase
        Label wlIgnoreCase = new Label(shell, RIGHT);
        wlIgnoreCase.setText(getString(PKG, "TaxonomyContextExtractorDialog.IgnoreCase.Label"));
        PropsUi.setLook(wlIgnoreCase);
        FormData fdlIgnoreCase = new FormData();
        fdlIgnoreCase.left = new FormAttachment(0, 0);
        fdlIgnoreCase.top = new FormAttachment(wRightContextMaxSize, margin);
        fdlIgnoreCase.right = new FormAttachment(middle, -2 * margin);
        wlIgnoreCase.setLayoutData(fdlIgnoreCase);

        wIgnoreCase = new Button(shell, CHECK);
        wIgnoreCase.setSelection(input.isIgnoreCase());
        PropsUi.setLook(wIgnoreCase);
        wIgnoreCase.setToolTipText(getString(PKG, "TaxonomyContextExtractorDialog.IgnoreCase.Tooltip"));
        FormData fdIgnoreCase = new FormData();
        fdIgnoreCase.left = new FormAttachment(middle, -margin);
        fdIgnoreCase.top = new FormAttachment(wRightContextMaxSize, margin * 2);
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
        if (input.getCorpusField() != null) {
            wCorpusFieldName.setText(input.getCorpusField());
        }
        if (input.getTaxonomyField() != null) {
            wTaxonomyFieldName.setText(input.getTaxonomyField());
        }

        if (input.getDocumentPartitionField() != null) {
            wDocumentPartitionFieldName.setText(input.getDocumentPartitionField());
        }

        if (input.isIgnoreCase()) {
            wIgnoreCase.setEnabled(input.isIgnoreCase());
        }

        if (input.getContextMeasurement() != null) {
            String d = ContextMeasurementType.valueOf(input.getContextMeasurement()).getDescription();
            wContextMeasurement.setText(d);
        }

        wLeftContextMaxSize.setText(String.valueOf(input.getLeftContextMaxSize()));
        wLeftContextTargetSize.setText(String.valueOf(input.getLeftContextTargetSize()));
        wRightContextMaxSize.setText(String.valueOf(input.getRightContextMaxSize()));
        wRightContextTargetSize.setText(String.valueOf(input.getRightContextTargetSize()));

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

        input.setCorpusField(wCorpusFieldName.getText());
        input.setTaxonomyField(wTaxonomyFieldName.getText());
        input.setDocumentPartitionField(wDocumentPartitionFieldName.getText());

        input.setContextMeasurement(getTypeFromDescription(wContextMeasurement.getText()).getCode());

        input.setLeftContextMaxSize(toInt(wLeftContextMaxSize.getText(), 500));
        input.setLeftContextTargetSize(toInt(wLeftContextTargetSize.getText(), 250));

        input.setRightContextMaxSize(toInt(wRightContextMaxSize.getText(), 500));
        input.setLeftContextTargetSize(toInt(wRightContextTargetSize.getText(), 250));

        input.setIgnoreCase(wIgnoreCase.getSelection());

        transformName = wTransformName.getText(); // return value

        dispose();
    }

    private void get() {
        if (!gotPreviousFields) {
            try {
                String corpusField = null;
                if (wCorpusFieldName.getText() != null) {
                    corpusField = wCorpusFieldName.getText();
                }
                wCorpusFieldName.removeAll();

                String taxonomyField = null;
                if (wTaxonomyFieldName.getText() != null) {
                    taxonomyField = wTaxonomyFieldName.getText();
                }

                wTaxonomyFieldName.removeAll();

                String documentPartitionField = null;
                if (wDocumentPartitionFieldName.getText() != null) {
                    documentPartitionField = wDocumentPartitionFieldName.getText();
                }

                wDocumentPartitionFieldName.removeAll();

                IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
                if (r != null) {
                    wCorpusFieldName.setItems(r.getFieldNames());
                    wTaxonomyFieldName.setItems(r.getFieldNames());
                    wDocumentPartitionFieldName.setItems(r.getFieldNames());
                }
                if (corpusField != null) {
                    wCorpusFieldName.setText(corpusField);
                }
                if (taxonomyField != null) {
                    wTaxonomyFieldName.setText(taxonomyField);
                }
                if (documentPartitionField != null) {
                    wDocumentPartitionFieldName.setText(documentPartitionField);
                }
                gotPreviousFields = true;
            } catch (HopException ke) {
                new ErrorDialog(
                        shell,
                        getString(PKG, "TaxonomyContextExtractorDialog.FailedToGetFields.DialogTitle"),
                        getString(PKG, "TaxonomyContextExtractorDialog.FailedToGetFields.DialogMessage"),
                        ke);
            }
        }
    }
}
