package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import org.apache.commons.lang3.StringUtils;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import static ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.i18nUtil.i18n;
import static org.apache.hop.ui.core.PropsUi.setLook;
import static org.eclipse.swt.SWT.*;

public class TransformNameComposite implements IDialogComposite {
    private final Label label;
    private final Text inputField;
    private final ITransformMeta meta;
    private final Control control;
    private final CompositeParameters parameters;

    public TransformNameComposite(CompositeParameters parameters) {
        this.parameters = parameters;
        this.meta = parameters.meta();

        // Transform Name label
        this.label = new Label(parameters.shell(), RIGHT);
        this.inputField = new Text(parameters.shell(), SINGLE | LEFT | BORDER);

        setLook(label);
        setLook(inputField);

        label.setText(i18n("System.Label.TransformName"));
        inputField.setText(parameters.transformName());

        inputField.addModifyListener(e -> meta.setChanged());

        FormData fdlTransformName = new FormData();
        fdlTransformName.left = new FormAttachment(0, 0);
        fdlTransformName.right = new FormAttachment(parameters.middlePct(), -parameters.margin());
        fdlTransformName.top = new FormAttachment(0, parameters.margin());
        FormData fdTransformName = new FormData();
        fdTransformName.left = new FormAttachment(parameters.middlePct(), 0);
        fdTransformName.top = new FormAttachment(0, parameters.margin());
        fdTransformName.right = new FormAttachment(100, 0);

        label.setLayoutData(fdlTransformName);
        inputField.setLayoutData(fdTransformName);

        this.control = inputField;
    }

    public Label getLabel() {
        return label;
    }

    public Text getInputField() {
        return inputField;
    }

    @Override
    public boolean validateInputs() {
        return true; // TODO implement
    }

    @Override
    public Control control() {
        return control;
    }

    @Override
    public void populateInputs() {

        inputField.selectAll();
        inputField.setFocus();
    }

    @Override
    public boolean ok() {
        if (StringUtils.isBlank(inputField.getText())) {
            return false;
        }

        parameters.dialog().setTransformName(inputField.getText());
        return true;
    }
}
