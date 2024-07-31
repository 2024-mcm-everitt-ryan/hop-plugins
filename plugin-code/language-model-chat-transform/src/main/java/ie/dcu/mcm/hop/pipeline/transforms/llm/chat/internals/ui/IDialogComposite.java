package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IDialogComposite extends PopulateInputsAdapter {

    default void updateTransformMeta() {
    }

    default boolean ok() {
        updateTransformMeta();
        return true;
    }

    default void cancel() {

    }

    default Composite composite() {
        throw new UnsupportedOperationException();
    }

    boolean validateInputs();

    default void updateLayouts() {

    }

    Control control();

}
