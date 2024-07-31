package ie.dcu.mcm.hop.pipeline.transforms.taxonomy;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.hop.core.Const.FORM_MARGIN;
import static org.apache.hop.core.Const.toInt;
import static org.apache.hop.core.util.Utils.isEmpty;
import static org.apache.hop.i18n.BaseMessages.getString;
import static org.apache.hop.ui.core.PropsUi.getMargin;
import static org.apache.hop.ui.core.PropsUi.setLook;
import static org.eclipse.swt.SWT.*;

public class TaxonomyLabellerDialog extends BaseTransformDialog implements ITransformDialog {
    private static final Class<?> PKG = TaxonomyLabellerDialog.class; // For Translator
    private final TaxonomyLabellerMeta input;
    private boolean gotPreviousFields = false;
    private CCombo wCorpusFieldName;
    private CCombo wTaxonomyFieldName;

    private TextVar wTaxonomyCategories;
    private TextVar wTaxonomyCategoriesDelimiter;
    private TextVar wOutputPhraseFieldNamePrefix;
    private TextVar wOutputLabelFieldNamePrefix;
    private TextVar wOutputMaxPhraseWordCount;
    private TextVar wParallelism;

    private Button wIgnoreCase;


    public TaxonomyLabellerDialog(
            Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
        super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
        input = (TaxonomyLabellerMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();

        shell = new Shell(parent, DIALOG_TRIM | RESIZE | MAX | MIN);
        setLook(shell);
        setShellImage(shell, input);

        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = FORM_MARGIN;
        formLayout.marginHeight = FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(getString(PKG, "TaxonomyLabellerDialog.Shell.Title"));

        Control control = transformerName(); // First item
        control = corpusFieldName(control);
        control = taxonomyFieldName(control);
        control = taxonomyCategories(control);
        control = taxonomyCategoriesDelimiter(control);
        control = outputPhraseFieldNamePrefix(control);
        control = outputLabelFieldNamePrefix(control);
        control = outputMaxPhraseWordCount(control);
        control = ignoreCase(control);
        control = parallelism(control);

        // THE BUTTONS
        wOk = new Button(shell, PUSH);
        wOk.setText(getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, PUSH);
        wCancel.setText(getString(PKG, "System.Button.Cancel"));

        setButtonPositions(new Button[]{wOk, wCancel}, getMargin(), control);

        // Add listeners
        wOk.addListener(Selection, e -> ok());
        wCancel.addListener(Selection, e -> cancel());

        getData();
        input.setChanged(changed);

        BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

        return transformName;
    }

    private Control transformerName() {
        int middle = props.getMiddlePct();
        int margin = getMargin();

        wlTransformName = new Label(shell, RIGHT);
        wlTransformName.setText(getString(PKG, "TaxonomyLabellerDialog.TransformName.Label"));
        setLook(wlTransformName);
        fdlTransformName = new FormData();
        fdlTransformName.left = new FormAttachment(0, 0);
        fdlTransformName.right = new FormAttachment(middle, -margin);
        fdlTransformName.top = new FormAttachment(0, margin);
        wlTransformName.setLayoutData(fdlTransformName);
        wTransformName = new Text(shell, SINGLE | LEFT | BORDER);
        wTransformName.setText(transformName);
        setLook(wTransformName);
        wTransformName.addModifyListener(e -> input.setChanged());
        fdTransformName = new FormData();
        fdTransformName.left = new FormAttachment(middle, 0);
        fdTransformName.top = new FormAttachment(0, margin);
        fdTransformName.right = new FormAttachment(100, 0);
        wTransformName.setLayoutData(fdTransformName);

        return wTransformName;
    }

    private Control corpusFieldName(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();

        Label wlCorpusFieldName = new Label(shell, RIGHT);
        wlCorpusFieldName.setText(getString(PKG, "TaxonomyLabellerDialog.CorpusFieldName.Label"));
        setLook(wlCorpusFieldName);
        FormData fdlCorpusFieldName = new FormData();
        fdlCorpusFieldName.left = new FormAttachment(0, 0);
        fdlCorpusFieldName.right = new FormAttachment(middle, -margin);
        fdlCorpusFieldName.top = new FormAttachment(control, margin);
        wlCorpusFieldName.setLayoutData(fdlCorpusFieldName);
        wCorpusFieldName = new CCombo(shell, BORDER | READ_ONLY);
        setLook(wCorpusFieldName);
        wCorpusFieldName.addModifyListener(e -> input.setChanged());
        FormData fdCorpusFieldName = new FormData();
        fdCorpusFieldName.left = new FormAttachment(middle, 0);
        fdCorpusFieldName.top = new FormAttachment(control, margin);
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

        return wCorpusFieldName;
    }

    private Control taxonomyFieldName(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlTaxonomyFieldName = new Label(shell, RIGHT);
        wlTaxonomyFieldName.setText(getString(PKG, "TaxonomyLabellerDialog.TaxonomyFieldName.Label"));
        setLook(wlTaxonomyFieldName);
        FormData fdlTaxonomyFieldName = new FormData();
        fdlTaxonomyFieldName.left = new FormAttachment(0, 0);
        fdlTaxonomyFieldName.right = new FormAttachment(middle, -margin);
        fdlTaxonomyFieldName.top = new FormAttachment(control, margin);
        wlTaxonomyFieldName.setLayoutData(fdlTaxonomyFieldName);
        wTaxonomyFieldName = new CCombo(shell, BORDER | READ_ONLY);
        setLook(wTaxonomyFieldName);
        wTaxonomyFieldName.addModifyListener(e -> input.setChanged());
        FormData fdTaxonomyFieldName = new FormData();
        fdTaxonomyFieldName.left = new FormAttachment(middle, 0);
        fdTaxonomyFieldName.top = new FormAttachment(control, margin);
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

        return wTaxonomyFieldName;
    }

    private Control taxonomyCategories(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlTaxonomyCategories = new Label(shell, RIGHT);
        wlTaxonomyCategories.setText(getString(PKG, "TaxonomyLabellerDialog.TaxonomyCategories.Label"));
        setLook(wlTaxonomyCategories);
        FormData fdlTaxonomyCategories = new FormData();
        fdlTaxonomyCategories.left = new FormAttachment(0, 0);
        fdlTaxonomyCategories.right = new FormAttachment(middle, -margin);
        fdlTaxonomyCategories.top = new FormAttachment(control, margin);
        wlTaxonomyCategories.setLayoutData(fdlTaxonomyCategories);
        wTaxonomyCategories = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wTaxonomyCategories.setText("");
        setLook(wTaxonomyCategories);
        wTaxonomyCategories.addModifyListener(e -> input.setChanged());
        FormData fdTaxonomyCategories = new FormData();
        fdTaxonomyCategories.left = new FormAttachment(middle, 0);
        fdTaxonomyCategories.top = new FormAttachment(control, margin);
        fdTaxonomyCategories.right = new FormAttachment(100, 0);
        wTaxonomyCategories.setLayoutData(fdTaxonomyCategories);
        return wTaxonomyCategories;
    }

    private Control taxonomyCategoriesDelimiter(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlTaxonomyCategoriesDelimiter = new Label(shell, RIGHT);
        wlTaxonomyCategoriesDelimiter.setText(getString(PKG, "TaxonomyLabellerDialog.TaxonomyCategoriesDelimiter.Label"));
        setLook(wlTaxonomyCategoriesDelimiter);
        FormData fdlTaxonomyCategoriesDelimiter = new FormData();
        fdlTaxonomyCategoriesDelimiter.left = new FormAttachment(0, 0);
        fdlTaxonomyCategoriesDelimiter.right = new FormAttachment(middle, -margin);
        fdlTaxonomyCategoriesDelimiter.top = new FormAttachment(control, margin);
        wlTaxonomyCategoriesDelimiter.setLayoutData(fdlTaxonomyCategoriesDelimiter);
        wTaxonomyCategoriesDelimiter = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wTaxonomyCategoriesDelimiter.setText("");
        setLook(wTaxonomyCategoriesDelimiter);
        wTaxonomyCategoriesDelimiter.addModifyListener(e -> input.setChanged());
        FormData fdTaxonomyCategoriesDelimiter = new FormData();
        fdTaxonomyCategoriesDelimiter.left = new FormAttachment(middle, 0);
        fdTaxonomyCategoriesDelimiter.top = new FormAttachment(control, margin);
        fdTaxonomyCategoriesDelimiter.right = new FormAttachment(100, 0);
        wTaxonomyCategoriesDelimiter.setLayoutData(fdTaxonomyCategoriesDelimiter);
        return wTaxonomyCategoriesDelimiter;
    }

    private Control outputPhraseFieldNamePrefix(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlOutputPhraseFieldNamePrefix = new Label(shell, RIGHT);
        wlOutputPhraseFieldNamePrefix.setText(getString(PKG, "TaxonomyLabellerDialog.OutputPhraseFieldNamePrefix.Label"));
        setLook(wlOutputPhraseFieldNamePrefix);
        FormData fdlOutputPhraseFieldNamePrefix = new FormData();
        fdlOutputPhraseFieldNamePrefix.left = new FormAttachment(0, 0);
        fdlOutputPhraseFieldNamePrefix.right = new FormAttachment(middle, -margin);
        fdlOutputPhraseFieldNamePrefix.top = new FormAttachment(control, margin);
        wlOutputPhraseFieldNamePrefix.setLayoutData(fdlOutputPhraseFieldNamePrefix);
        wOutputPhraseFieldNamePrefix = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wOutputPhraseFieldNamePrefix.setText("");
        setLook(wOutputPhraseFieldNamePrefix);
        wOutputPhraseFieldNamePrefix.addModifyListener(e -> input.setChanged());
        FormData fdOutputPhraseFieldNamePrefix = new FormData();
        fdOutputPhraseFieldNamePrefix.left = new FormAttachment(middle, 0);
        fdOutputPhraseFieldNamePrefix.top = new FormAttachment(control, margin);
        fdOutputPhraseFieldNamePrefix.right = new FormAttachment(100, 0);
        wOutputPhraseFieldNamePrefix.setLayoutData(fdOutputPhraseFieldNamePrefix);
        return wOutputPhraseFieldNamePrefix;
    }

    private Control outputLabelFieldNamePrefix(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlOutputLabelFieldNamePrefix = new Label(shell, RIGHT);
        wlOutputLabelFieldNamePrefix.setText(getString(PKG, "TaxonomyLabellerDialog.OutputLabelFieldNamePrefix.Label"));
        setLook(wlOutputLabelFieldNamePrefix);
        FormData fdlOutputLabelFieldNamePrefix = new FormData();
        fdlOutputLabelFieldNamePrefix.left = new FormAttachment(0, 0);
        fdlOutputLabelFieldNamePrefix.right = new FormAttachment(middle, -margin);
        fdlOutputLabelFieldNamePrefix.top = new FormAttachment(control, margin);
        wlOutputLabelFieldNamePrefix.setLayoutData(fdlOutputLabelFieldNamePrefix);
        wOutputLabelFieldNamePrefix = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wOutputLabelFieldNamePrefix.setText("");
        setLook(wOutputLabelFieldNamePrefix);
        wOutputLabelFieldNamePrefix.addModifyListener(e -> input.setChanged());
        FormData fdOutputLabelFieldNamePrefix = new FormData();
        fdOutputLabelFieldNamePrefix.left = new FormAttachment(middle, 0);
        fdOutputLabelFieldNamePrefix.top = new FormAttachment(control, margin);
        fdOutputLabelFieldNamePrefix.right = new FormAttachment(100, 0);
        wOutputLabelFieldNamePrefix.setLayoutData(fdOutputLabelFieldNamePrefix);
        return wOutputLabelFieldNamePrefix;
    }

    private Control outputMaxPhraseWordCount(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlOutputMaxPhraseWordCount = new Label(shell, RIGHT);
        wlOutputMaxPhraseWordCount.setText(getString(PKG, "TaxonomyLabellerDialog.OutputMaxPhraseWordCount.Label"));
        setLook(wlOutputMaxPhraseWordCount);
        FormData fdlOutputMaxPhraseWordCount = new FormData();
        fdlOutputMaxPhraseWordCount.left = new FormAttachment(0, 0);
        fdlOutputMaxPhraseWordCount.right = new FormAttachment(middle, -margin);
        fdlOutputMaxPhraseWordCount.top = new FormAttachment(control, margin);
        wlOutputMaxPhraseWordCount.setLayoutData(fdlOutputMaxPhraseWordCount);
        wOutputMaxPhraseWordCount = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wOutputMaxPhraseWordCount.setText("");
        setLook(wOutputMaxPhraseWordCount);
        wOutputMaxPhraseWordCount.addModifyListener(e -> input.setChanged());
        FormData fdOutputMaxPhraseWordCount = new FormData();
        fdOutputMaxPhraseWordCount.left = new FormAttachment(middle, 0);
        fdOutputMaxPhraseWordCount.top = new FormAttachment(control, margin);
        fdOutputMaxPhraseWordCount.right = new FormAttachment(100, 0);
        wOutputMaxPhraseWordCount.setLayoutData(fdOutputMaxPhraseWordCount);
        return wOutputMaxPhraseWordCount;
    }

    private Control ignoreCase(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlIgnoreCase = new Label(shell, RIGHT);
        wlIgnoreCase.setText(getString(PKG, "TaxonomyLabellerDialog.IgnoreCase.Label"));
        setLook(wlIgnoreCase);
        FormData fdlIgnoreCase = new FormData();
        fdlIgnoreCase.left = new FormAttachment(0, 0);
        fdlIgnoreCase.top = new FormAttachment(control, margin);
        fdlIgnoreCase.right = new FormAttachment(middle, -2 * margin);
        wlIgnoreCase.setLayoutData(fdlIgnoreCase);

        wIgnoreCase = new Button(shell, CHECK);
        wIgnoreCase.setSelection(input.isIgnoreCase());
        setLook(wIgnoreCase);
        wIgnoreCase.setToolTipText(getString(PKG, "TaxonomyLabellerDialog.IgnoreCase.Tooltip"));
        FormData fdIgnoreCase = new FormData();
        fdIgnoreCase.left = new FormAttachment(middle, -margin);
        fdIgnoreCase.top = new FormAttachment(control, margin * 2);
        fdIgnoreCase.right = new FormAttachment(100, 0);
        wIgnoreCase.setLayoutData(fdIgnoreCase);
        wIgnoreCase.addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        input.setChanged();
                    }
                });

        return wlIgnoreCase;
    }

    private Control parallelism(Control control) {
        int middle = props.getMiddlePct();
        int margin = getMargin();
        Label wlParallelism = new Label(shell, RIGHT);
        wlParallelism.setText(getString(PKG, "TaxonomyLabellerDialog.Parallelism.Label"));
        setLook(wlParallelism);
        FormData fdlParallelism = new FormData();
        fdlParallelism.left = new FormAttachment(0, 0);
        fdlParallelism.right = new FormAttachment(middle, -margin);
        fdlParallelism.top = new FormAttachment(control, margin);
        wlParallelism.setLayoutData(fdlParallelism);
        wParallelism = new TextVar(variables, shell, SINGLE | LEFT | BORDER);
        wParallelism.setText("");
        setLook(wParallelism);
        wParallelism.addModifyListener(e -> input.setChanged());
        FormData fdParallelism = new FormData();
        fdParallelism.left = new FormAttachment(middle, 0);
        fdParallelism.top = new FormAttachment(control, margin);
        fdParallelism.right = new FormAttachment(100, 0);
        wParallelism.setLayoutData(fdParallelism);
        return wlParallelism;
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData() {
        if (input.getCorpusField() != null) {
            wCorpusFieldName.setText(input.getCorpusField());
        }
        if (input.getTaxonomyField() != null) {
            wTaxonomyFieldName.setText(trimToEmpty(input.getTaxonomyField()));
        }

        if (input.isIgnoreCase()) {
            wIgnoreCase.setEnabled(input.isIgnoreCase());
        }

        wOutputPhraseFieldNamePrefix.setText(trimToEmpty(input.getOutputPhraseFieldNamePrefix()));
        wTaxonomyCategories.setText(trimToEmpty(input.getTaxonomyCategories()));
        wOutputLabelFieldNamePrefix.setText(trimToEmpty(input.getOutputLabelFieldNamePrefix()));
        wTaxonomyCategoriesDelimiter.setText(trimToEmpty(input.getTaxonomyCategoriesDelimiter()));
        wOutputMaxPhraseWordCount.setText(String.valueOf(input.getOutputMaxPhraseWordCount()));
        wParallelism.setText(String.valueOf(input.getParallelism()));

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

        transformName = wTransformName.getText();
        input.setCorpusField(wCorpusFieldName.getText());
        input.setTaxonomyField(wTaxonomyFieldName.getText());

        input.setTaxonomyCategories(wTaxonomyCategories.getText());
        input.setTaxonomyCategoriesDelimiter(wTaxonomyCategoriesDelimiter.getText());
        input.setOutputPhraseFieldNamePrefix(wOutputPhraseFieldNamePrefix.getText());
        input.setOutputLabelFieldNamePrefix(wOutputLabelFieldNamePrefix.getText());
        input.setOutputMaxPhraseWordCount(toInt(wOutputMaxPhraseWordCount.getText(), MAX_VALUE));
        input.setParallelism(toInt(wParallelism.getText(), 1));

        input.setIgnoreCase(wIgnoreCase.getSelection());


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

                IRowMeta r = pipelineMeta.getPrevTransformFields(variables, transformName);
                if (r != null) {
                    wCorpusFieldName.setItems(r.getFieldNames());
                    wTaxonomyFieldName.setItems(r.getFieldNames());
                }
                if (corpusField != null) {
                    wCorpusFieldName.setText(corpusField);
                }
                if (taxonomyField != null) {
                    wTaxonomyFieldName.setText(taxonomyField);
                }
                gotPreviousFields = true;
            } catch (HopException ke) {
                new ErrorDialog(
                        shell,
                        getString(PKG, "TaxonomyLabellerDialog.FailedToGetFields.DialogTitle"),
                        getString(PKG, "TaxonomyLabellerDialog.FailedToGetFields.DialogMessage"),
                        ke);
            }
        }
    }
}
