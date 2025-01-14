package ie.dcu.mcm.hop.pipeline.transforms.llm.chat;

import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.*;
import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.CompositeParameters.Builder;
import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.models.*;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.util.Collection;
import java.util.List;

import static ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.CompositeParameters.Builder.buildCompositeParameters;
import static ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.i18nUtil.i18n;
import static org.apache.hop.ui.core.PropsUi.*;
import static org.apache.hop.ui.core.dialog.BaseDialog.defaultShellHandling;
import static org.eclipse.swt.SWT.*;

public class LanguageModelChatDialog extends BaseTransformDialog implements ITransformDialog, PopulateInputsAdapter {

    private final LanguageModelChatMeta input;
    private Collection<IDialogComposite> composites;

    public LanguageModelChatDialog(
            Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
        super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
        input = (LanguageModelChatMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();
        shell = new Shell(parent, DIALOG_TRIM | RESIZE | MAX | MIN);
        setLook(shell);
        setShellImage(shell, input);

        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = getFormMargin();
        formLayout.marginHeight = getFormMargin();
        shell.setLayout(formLayout);
        shell.setText(i18n("LanguageModelChatDialog.Shell.Title"));

        int margin = getMargin();

        // Model Specific Composite
        Composite  modelComposite = new Composite(shell, NONE);

        PopulateInputsAdapter modelCompositeInputsAdapter = new PopulateInputsAdapter() {
            @Override
            public void populateInputs() {
                LanguageModelChatDialog.this.populateInputs(modelComposite);
            }
        };

        Builder params = buildCompositeParameters()
                .dialog(this)
                .populateInputsAdapter(modelCompositeInputsAdapter)
                .middlePct(props.getMiddlePct())
                .margin(margin)
                .shell(shell)
                .parent(shell)
                .meta(input)
                .variables(variables)
                .transformName(transformName)
                .pipelineMeta(pipelineMeta);


        // Transform Name Row
        IDialogComposite tnc = new TransformNameComposite(params.build());

        IDialogComposite gsc = new GeneralSettingsComposite(params.control(tnc.control()).build());

        // Model Specific Composite
        modelComposite.setLayout(new FormLayout());
        //modelComposite.setLayoutData(buildFormData().control(gsc.getControl()).margin(margin).left(0,0).right(100,0).build());
        FormData fdModelSpecificComp = new FormData();
        fdModelSpecificComp.left = new FormAttachment(0, 0);
        fdModelSpecificComp.top = new FormAttachment(gsc.control(), margin);
        fdModelSpecificComp.right = new FormAttachment(100, 0);
        fdModelSpecificComp.bottom = new FormAttachment(100, -50); // Leave space for OK/Cancel
        modelComposite.setLayoutData(fdModelSpecificComp);
        setLook(modelComposite);
        params.parent(modelComposite);

        IDialogComposite anthropic = new AnthropicComposite(params.control(modelComposite).build());
        IDialogComposite huggingface = new HuggingFaceComposite(params.control(modelComposite).build());
        IDialogComposite mistral = new MistralComposite(params.control(modelComposite).build());
        IDialogComposite ollama = new OllamaComposite(params.control(modelComposite).build());
        IDialogComposite openai = new OpenAiComposite(params.control(modelComposite).build());

        composites = List.of(
                tnc,
                gsc,
                anthropic,
                huggingface,
                mistral,
                ollama,
                openai
        );

        // OK and Cancel buttons
        setButtonPositions(new Button[]{
                new OkButton(shell, e -> ok()).delegate(),
                new CancelButton(shell, e -> cancel()).delegate()
        }, margin, modelComposite);

        populateInputs(modelComposite);

        // Open dialog and return the transformed name
        defaultShellHandling(shell, c -> ok(), c -> cancel());

        return transformName;
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public void populateInputs(Composite composite) {
        // Hide all model-specific composites initially
        for (IDialogComposite c : composites) {
            if (c instanceof IModelComposite) {
                c.composite().setVisible(false);
                c.updateLayouts();
            }
        }

        // Show the relevant composite based on the modelType selection
        composites.forEach(IDialogComposite::populateInputs);

        // Show the selected model composite
        for (IDialogComposite c : composites) {
            if (c instanceof IModelComposite) {
                IModelComposite m = (IModelComposite) c;
                if(m.isSelectedModelType()) {
                    c.composite().setVisible(true);
                    c.updateLayouts();
                }
            }

            c.updateLayouts();
        }

        composite.layout(true, true);
        shell.layout(true, true); // Ensure the dialog is correctly laid out after setting the data

        input.setChanged(changed);
    }


    private void ok() {
        int validated = (int) composites.stream().filter(IDialogComposite::validateInputs).count();
        if (validated != composites.size()) {
            // TODO implement
            return;
        }

        if(composites.stream().anyMatch(c -> !c.ok())) {
            return;
        }

        // Mark the transform as changed
        input.setChanged();

        // Close the dialog
        dispose();
    }


    private void cancel() {

        composites.forEach(IDialogComposite::cancel);

        // Set the transformName to null indicates that no changes should be applied
        transformName = null;
        // Mark the transform as unchanged (since we are cancelling)
        input.setChanged(false);
        // Close the dialog
        dispose();
    }
}
