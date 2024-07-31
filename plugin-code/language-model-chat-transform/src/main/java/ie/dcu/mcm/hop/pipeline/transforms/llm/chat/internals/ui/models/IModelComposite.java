package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.models;

import ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.IDialogComposite;

public interface IModelComposite extends IDialogComposite {

    void loadData();

    boolean isSelectedModelType();

    default void populateInputs() {
        if (isSelectedModelType()) {
            composite().setVisible(true);
            loadData();
        }
    }
}
