package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import org.eclipse.swt.widgets.Composite;

public interface PopulateInputsAdapter {
    default void populateInputs() {
    }

    default void populateInputs(Composite composite) {

    }
}
