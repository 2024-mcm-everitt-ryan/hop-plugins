package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.LanguageModelChatDialog;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class CompositeParameters {

    private final Shell shell;
    private final Control control;
    private final Composite parent;
    private final IVariables variables;
    private final ITransformMeta meta;

    private final int middlePct;
    private final int margin;
    private final String transformName;
    private final PopulateInputsAdapter populateInputsAdapter;
    private final PipelineMeta pipelineMeta;
    private final LanguageModelChatDialog dialog;

    private CompositeParameters(Shell shell, PopulateInputsAdapter populateInputsAdapter, Control control, LanguageModelChatDialog dialog, Composite parent, IVariables variables, ITransformMeta meta, int middlePct, int margin, String transformName, PipelineMeta pipelineMeta) {

        this.shell = shell;
        this.control = control;
        this.dialog = dialog;
        this.parent = parent == null && parent instanceof Composite ? (Composite) control : parent;
        this.populateInputsAdapter = populateInputsAdapter;
        this.variables = variables;
        this.meta = meta;
        this.middlePct = middlePct;
        this.margin = margin;
        this.transformName = transformName;
        this.pipelineMeta = pipelineMeta;
    }

    public Shell shell() {
        return shell;
    }

    public Control control() {
        return control;
    }

    public IVariables variables() {
        return variables;
    }

    public ITransformMeta meta() {
        return meta;
    }


    public int middlePct() {
        return middlePct;
    }

    public int margin() {
        return margin;
    }

    public String transformName() {
        return transformName;
    }

    public PopulateInputsAdapter getPopulateInputsAdapter() {
        return populateInputsAdapter;
    }

    public Composite parent() {
        return parent;
    }

    public PipelineMeta pipelineMeta() {
        return pipelineMeta;
    }

    public LanguageModelChatDialog dialog() {
        return dialog;
    }


    public static final class Builder {
        private Shell shell;
        private Control control;
        private Composite parent;
        private IVariables variables;
        private ITransformMeta meta;
        private int middlePct;
        private int margin;
        private String transformName;
        private PopulateInputsAdapter populateInputsAdapter;
        private PipelineMeta pipelineMeta;
        private LanguageModelChatDialog dialog;

        private Builder() {
        }

        public static Builder buildCompositeParameters() {
            return new Builder();
        }

        public Builder shell(Shell shell) {
            this.shell = shell;
            return this;
        }

        public Builder control(Control control) {
            this.control = control;
            return this;
        }

        public Builder parent(Composite parent) {
            this.parent = parent;
            return this;
        }

        public Builder dialog(LanguageModelChatDialog dialog) {
            this.dialog = dialog;
            return this;
        }

        public Builder populateInputsAdapter(PopulateInputsAdapter populateInputsAdapter) {
            this.populateInputsAdapter = populateInputsAdapter;
            return this;
        }

        public Builder variables(IVariables variables) {
            this.variables = variables;
            return this;
        }

        public Builder meta(ITransformMeta meta) {
            this.meta = meta;
            return this;
        }

        public Builder middlePct(int middlePct) {
            this.middlePct = middlePct;
            return this;
        }

        public Builder margin(int margin) {
            this.margin = margin;
            return this;
        }

        public Builder transformName(String transformName) {
            this.transformName = transformName;
            return this;
        }

        public CompositeParameters build() {
            return new CompositeParameters(
                    shell,
                    populateInputsAdapter,
                    control,
                    dialog,
                    parent,
                    variables,
                    meta,
                    middlePct,
                    margin,
                    transformName,
                    pipelineMeta
            );
        }

        public Builder pipelineMeta(PipelineMeta pipelineMeta) {
            this.pipelineMeta = pipelineMeta;
            return this;
        }
    }
}
